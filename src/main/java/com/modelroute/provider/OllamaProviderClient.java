package com.modelroute.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modelroute.config.ModelRouteProperties;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class OllamaProviderClient implements ModelProviderClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OllamaProviderClient(WebClient.Builder builder, ObjectMapper objectMapper) {
        this.webClient = builder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerType() {
        return "ollama";
    }

    @Override
    public ProviderResponse complete(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        validate(model, messages);
        try {
            JsonNode response = webClient.post()
                    .uri(chatUrl(model))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request(model, messages, false))
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> clientResponse.bodyToMono(String.class)
                            .defaultIfEmpty("empty provider error response")
                            .map(body -> new ModelProviderException(
                                    "Ollama request failed with status " + clientResponse.statusCode() + ": " + body)))
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofMillis(model.getTimeoutMs()));
            String text = response == null ? null : response.path("message").path("content").asText(null);
            if (!StringUtils.hasText(text)) {
                throw new ModelProviderException("Ollama response did not contain text");
            }
            return new ProviderResponse(text);
        } catch (ModelProviderException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ModelProviderException("Ollama request failed", exception);
        }
    }

    @Override
    public Flux<String> stream(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        validate(model, messages);
        return webClient.post()
                .uri(chatUrl(model))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_NDJSON)
                .bodyValue(request(model, messages, true))
                .exchangeToFlux(response -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("empty provider error response")
                                .flatMapMany(body -> Flux.error(new ModelProviderException(
                                        "Ollama request failed with status " + response.statusCode() + ": " + body)));
                    }
                    return response.bodyToFlux(String.class);
                })
                .flatMapIterable(this::lines)
                .mapNotNull(this::delta)
                .filter(StringUtils::hasLength)
                .timeout(Duration.ofMillis(model.getTimeoutMs()))
                .onErrorMap(exception -> exception instanceof ModelProviderException
                        ? exception
                        : new ModelProviderException("Ollama stream failed", exception));
    }

    private Map<String, Object> request(
            ModelRouteProperties.ModelDefinition model,
            List<ChatMessage> messages,
            boolean stream) {
        return Map.of(
                "model", model.getModelName(),
                "messages", messages,
                "stream", stream,
                "options", Map.of(
                        "temperature", model.getTemperature(),
                        "num_predict", model.getMaxTokens()));
    }

    private List<String> lines(String chunk) {
        return chunk.lines().filter(StringUtils::hasText).toList();
    }

    private String delta(String data) {
        try {
            return objectMapper.readTree(data).path("message").path("content").asText(null);
        } catch (Exception exception) {
            throw new ModelProviderException("Unable to parse Ollama stream event", exception);
        }
    }

    private String chatUrl(ModelRouteProperties.ModelDefinition model) {
        return model.getBaseUrl().replaceAll("/+$", "") + "/api/chat";
    }

    private void validate(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        if (!StringUtils.hasText(model.getBaseUrl()) || !StringUtils.hasText(model.getModelName())) {
            throw new ModelProviderException("Ollama model configuration is incomplete: " + model.getId());
        }
        if (messages == null || messages.isEmpty()) {
            throw new ModelProviderException("At least one chat message is required");
        }
    }
}
