package com.modelroute.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modelroute.config.ModelRouteProperties;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
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
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Flux;

@Component
public class GeminiProviderClient implements ModelProviderClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiProviderClient(WebClient.Builder builder, ObjectMapper objectMapper) {
        this.webClient = builder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerType() {
        return "gemini";
    }

    @Override
    public ProviderResponse complete(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        validate(model, messages);
        try {
            JsonNode response = webClient.post()
                    .uri(url(model, false))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request(model, messages))
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> clientResponse.bodyToMono(String.class)
                            .defaultIfEmpty("empty provider error response")
                            .map(body -> new ModelProviderException(
                                    "Gemini request failed with status " + clientResponse.statusCode() + ": " + body)))
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofMillis(model.getTimeoutMs()));
            String text = candidateText(response);
            if (!StringUtils.hasText(text)) {
                throw new ModelProviderException("Gemini response did not contain text");
            }
            return new ProviderResponse(text);
        } catch (ModelProviderException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ModelProviderException("Gemini request failed", exception);
        }
    }

    @Override
    public Flux<String> stream(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        validate(model, messages);
        return webClient.post()
                .uri(url(model, true))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(request(model, messages))
                .exchangeToFlux(response -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("empty provider error response")
                                .flatMapMany(body -> Flux.error(new ModelProviderException(
                                        "Gemini request failed with status " + response.statusCode() + ": " + body)));
                    }
                    return response.bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
                    }).mapNotNull(ServerSentEvent::data);
                })
                .mapNotNull(data -> {
                    try {
                        return candidateText(objectMapper.readTree(data));
                    } catch (Exception exception) {
                        throw new ModelProviderException("Unable to parse Gemini stream event", exception);
                    }
                })
                .filter(StringUtils::hasLength)
                .timeout(Duration.ofMillis(model.getTimeoutMs()))
                .onErrorMap(exception -> exception instanceof ModelProviderException
                        ? exception
                        : new ModelProviderException("Gemini stream failed", exception));
    }

    private Map<String, Object> request(
            ModelRouteProperties.ModelDefinition model,
            List<ChatMessage> messages) {
        List<Map<String, Object>> contents = new ArrayList<>();
        List<String> systems = new ArrayList<>();
        for (ChatMessage message : messages) {
            if ("system".equals(message.role())) {
                systems.add(message.content());
                continue;
            }
            contents.add(Map.of(
                    "role", "assistant".equals(message.role()) ? "model" : "user",
                    "parts", List.of(Map.of("text", message.content()))));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contents", contents);
        if (!systems.isEmpty()) {
            body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", String.join("\n", systems)))));
        }
        body.put("generationConfig", Map.of(
                "temperature", model.getTemperature(),
                "maxOutputTokens", model.getMaxTokens()));
        return body;
    }

    private String candidateText(JsonNode response) {
        if (response == null) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        for (JsonNode part : response.path("candidates").path(0).path("content").path("parts")) {
            if (part.path("text").isTextual()) {
                text.append(part.path("text").asText());
            }
        }
        return text.toString();
    }

    private String url(ModelRouteProperties.ModelDefinition model, boolean stream) {
        String operation = stream ? ":streamGenerateContent?alt=sse&key=" : ":generateContent?key=";
        return model.getBaseUrl().replaceAll("/+$", "")
                + "/v1beta/models/"
                + UriUtils.encodePathSegment(model.getModelName(), StandardCharsets.UTF_8)
                + operation
                + UriUtils.encodeQueryParam(model.getApiKey(), StandardCharsets.UTF_8);
    }

    private void validate(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        if (!StringUtils.hasText(model.getBaseUrl()) || !StringUtils.hasText(model.getApiKey())
                || !StringUtils.hasText(model.getModelName())) {
            throw new ModelProviderException("Gemini model configuration is incomplete: " + model.getId());
        }
        if (messages == null || messages.isEmpty()) {
            throw new ModelProviderException("At least one chat message is required");
        }
    }
}
