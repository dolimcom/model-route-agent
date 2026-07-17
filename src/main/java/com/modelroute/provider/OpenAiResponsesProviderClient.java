package com.modelroute.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modelroute.config.ModelRouteProperties;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class OpenAiResponsesProviderClient implements ModelProviderClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OpenAiResponsesProviderClient(WebClient.Builder builder, ObjectMapper objectMapper) {
        this.webClient = builder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerType() {
        return "openai-responses";
    }

    @Override
    public ProviderResponse complete(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        validate(model, messages);
        try {
            JsonNode response = webClient.post()
                    .uri(responsesUrl(model.getBaseUrl()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(model.getApiKey()))
                    .bodyValue(request(model, messages, false))
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> clientResponse.bodyToMono(String.class)
                            .defaultIfEmpty("empty provider error response")
                            .map(body -> new ModelProviderException(
                                    "OpenAI request failed with status " + clientResponse.statusCode() + ": " + body)))
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofMillis(model.getTimeoutMs()));
            String text = outputText(response);
            if (!StringUtils.hasText(text)) {
                throw new ModelProviderException("OpenAI response did not contain output text");
            }
            return new ProviderResponse(text);
        } catch (ModelProviderException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ModelProviderException("OpenAI Responses request failed", exception);
        }
    }

    @Override
    public Flux<String> stream(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        validate(model, messages);
        return webClient.post()
                .uri(responsesUrl(model.getBaseUrl()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .headers(headers -> headers.setBearerAuth(model.getApiKey()))
                .bodyValue(request(model, messages, true))
                .exchangeToFlux(response -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("empty provider error response")
                                .flatMapMany(body -> Flux.error(new ModelProviderException(
                                        "OpenAI request failed with status " + response.statusCode() + ": " + body)));
                    }
                    return response.bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
                    }).mapNotNull(ServerSentEvent::data);
                })
                .mapNotNull(this::delta)
                .filter(StringUtils::hasLength)
                .timeout(Duration.ofMillis(model.getTimeoutMs()))
                .onErrorMap(exception -> exception instanceof ModelProviderException
                        ? exception
                        : new ModelProviderException("OpenAI Responses stream failed", exception));
    }

    private Map<String, Object> request(
            ModelRouteProperties.ModelDefinition model,
            List<ChatMessage> messages,
            boolean stream) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", model.getModelName());
        request.put("input", messages);
        request.put("temperature", model.getTemperature());
        request.put("max_output_tokens", model.getMaxTokens());
        request.put("stream", stream);
        return request;
    }

    private String outputText(JsonNode response) {
        if (response == null) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        for (JsonNode output : response.path("output")) {
            for (JsonNode content : output.path("content")) {
                if ("output_text".equals(content.path("type").asText()) && content.path("text").isTextual()) {
                    text.append(content.path("text").asText());
                }
            }
        }
        return text.toString();
    }

    private String delta(String data) {
        if (!StringUtils.hasText(data) || "[DONE]".equals(data)) {
            return null;
        }
        try {
            JsonNode event = objectMapper.readTree(data);
            return "response.output_text.delta".equals(event.path("type").asText())
                    ? event.path("delta").asText(null)
                    : null;
        } catch (Exception exception) {
            throw new ModelProviderException("Unable to parse OpenAI stream event", exception);
        }
    }

    private void validate(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        if (!StringUtils.hasText(model.getBaseUrl()) || !StringUtils.hasText(model.getApiKey())
                || !StringUtils.hasText(model.getModelName())) {
            throw new ModelProviderException("OpenAI model configuration is incomplete: " + model.getId());
        }
        if (messages == null || messages.isEmpty()) {
            throw new ModelProviderException("At least one chat message is required");
        }
    }

    private String responsesUrl(String baseUrl) {
        return baseUrl.replaceAll("/+$", "") + "/responses";
    }
}
