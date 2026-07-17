package com.modelroute.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modelroute.config.ModelRouteProperties;
import java.time.Duration;
import java.util.ArrayList;
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
public class AnthropicProviderClient implements ModelProviderClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AnthropicProviderClient(WebClient.Builder builder, ObjectMapper objectMapper) {
        this.webClient = builder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerType() {
        return "anthropic";
    }

    @Override
    public ProviderResponse complete(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        validate(model, messages);
        try {
            JsonNode response = baseRequest(model)
                    .bodyValue(request(model, messages, false))
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> clientResponse.bodyToMono(String.class)
                            .defaultIfEmpty("empty provider error response")
                            .map(body -> new ModelProviderException(
                                    "Anthropic request failed with status " + clientResponse.statusCode() + ": " + body)))
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofMillis(model.getTimeoutMs()));
            String text = contentText(response);
            if (!StringUtils.hasText(text)) {
                throw new ModelProviderException("Anthropic response did not contain text");
            }
            return new ProviderResponse(text);
        } catch (ModelProviderException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ModelProviderException("Anthropic request failed", exception);
        }
    }

    @Override
    public Flux<String> stream(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        validate(model, messages);
        return baseRequest(model)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(request(model, messages, true))
                .exchangeToFlux(response -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("empty provider error response")
                                .flatMapMany(body -> Flux.error(new ModelProviderException(
                                        "Anthropic request failed with status " + response.statusCode() + ": " + body)));
                    }
                    return response.bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
                    }).mapNotNull(ServerSentEvent::data);
                })
                .mapNotNull(this::delta)
                .filter(StringUtils::hasLength)
                .timeout(Duration.ofMillis(model.getTimeoutMs()))
                .onErrorMap(exception -> exception instanceof ModelProviderException
                        ? exception
                        : new ModelProviderException("Anthropic stream failed", exception));
    }

    private WebClient.RequestBodySpec baseRequest(ModelRouteProperties.ModelDefinition model) {
        return webClient.post()
                .uri(model.getBaseUrl().replaceAll("/+$", "") + "/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-api-key", model.getApiKey())
                .header("anthropic-version", "2023-06-01");
    }

    private Map<String, Object> request(
            ModelRouteProperties.ModelDefinition model,
            List<ChatMessage> messages,
            boolean stream) {
        List<Map<String, String>> providerMessages = new ArrayList<>();
        List<String> systems = new ArrayList<>();
        for (ChatMessage message : messages) {
            if ("system".equals(message.role())) {
                systems.add(message.content());
            } else {
                providerMessages.add(Map.of(
                        "role", "assistant".equals(message.role()) ? "assistant" : "user",
                        "content", message.content()));
            }
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model.getModelName());
        body.put("messages", providerMessages);
        body.put("max_tokens", model.getMaxTokens());
        body.put("temperature", model.getTemperature());
        body.put("stream", stream);
        if (!systems.isEmpty()) {
            body.put("system", String.join("\n", systems));
        }
        return body;
    }

    private String contentText(JsonNode response) {
        if (response == null) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        for (JsonNode block : response.path("content")) {
            if ("text".equals(block.path("type").asText()) && block.path("text").isTextual()) {
                text.append(block.path("text").asText());
            }
        }
        return text.toString();
    }

    private String delta(String data) {
        try {
            JsonNode event = objectMapper.readTree(data);
            return "content_block_delta".equals(event.path("type").asText())
                    ? event.path("delta").path("text").asText(null)
                    : null;
        } catch (Exception exception) {
            throw new ModelProviderException("Unable to parse Anthropic stream event", exception);
        }
    }

    private void validate(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        if (!StringUtils.hasText(model.getBaseUrl()) || !StringUtils.hasText(model.getApiKey())
                || !StringUtils.hasText(model.getModelName())) {
            throw new ModelProviderException("Anthropic model configuration is incomplete: " + model.getId());
        }
        if (messages == null || messages.isEmpty()) {
            throw new ModelProviderException("At least one chat message is required");
        }
    }
}
