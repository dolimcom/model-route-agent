package com.modelroute.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modelroute.config.ModelRouteProperties;
import java.time.Duration;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * Calls providers exposing the OpenAI-compatible chat completions protocol.
 */
@Component
public class OpenAiCompatibleProviderClient implements ModelProviderClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleProviderClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerType() {
        return "openai-compatible";
    }

    @Override
    public ProviderResponse complete(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        validateRequest(model, messages);

        ChatCompletionResponse response;
        try {
            response = webClient.post()
                    .uri(chatCompletionsUrl(model.getBaseUrl()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> setAuthorization(headers, model.getApiKey()))
                    .bodyValue(new ChatCompletionRequest(
                            model.getModelName(), messages, model.getTemperature(), model.getMaxTokens(), false))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse.bodyToMono(String.class)
                            .defaultIfEmpty("empty provider error response")
                            .map(body -> new ModelProviderException(
                                    "Provider request failed with status " + clientResponse.statusCode() + ": " + body)))
                    .bodyToMono(ChatCompletionResponse.class)
                    .block(Duration.ofMillis(model.getTimeoutMs()));
        } catch (ModelProviderException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ModelProviderException("OpenAI-compatible provider request failed", exception);
        }

        if (response == null || response.choices() == null || response.choices().isEmpty()
                || response.choices().get(0).message() == null
                || !StringUtils.hasText(response.choices().get(0).message().content())) {
            throw new ModelProviderException("Provider response did not contain a chat completion message");
        }
        return new ProviderResponse(response.choices().get(0).message().content());
    }

    @Override
    public Flux<String> stream(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        validateRequest(model, messages);
        return webClient.post()
                .uri(chatCompletionsUrl(model.getBaseUrl()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .headers(headers -> setAuthorization(headers, model.getApiKey()))
                .bodyValue(new ChatCompletionRequest(
                        model.getModelName(), messages, model.getTemperature(), model.getMaxTokens(), true))
                .exchangeToFlux(response -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("empty provider error response")
                                .flatMapMany(body -> Flux.error(new ModelProviderException(
                                        "Provider request failed with status " + response.statusCode() + ": " + body)));
                    }
                    return response.bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
                    }).mapNotNull(ServerSentEvent::data);
                })
                .filter(data -> !"[DONE]".equals(data))
                .mapNotNull(this::extractDelta)
                .filter(StringUtils::hasLength)
                .timeout(Duration.ofMillis(model.getTimeoutMs()))
                .onErrorMap(exception -> exception instanceof ModelProviderException
                        ? exception
                        : new ModelProviderException("OpenAI-compatible provider stream failed", exception));
    }

    private String extractDelta(String data) {
        try {
            JsonNode root = objectMapper.readTree(data);
            JsonNode content = root.path("choices").path(0).path("delta").path("content");
            return content.isTextual() ? content.asText() : null;
        } catch (Exception exception) {
            throw new ModelProviderException("Unable to parse provider stream event", exception);
        }
    }

    private void validateRequest(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        if (!StringUtils.hasText(model.getBaseUrl()) || !StringUtils.hasText(model.getModelName())) {
            throw new ModelProviderException("OpenAI-compatible model configuration is incomplete: " + model.getId());
        }
        if (messages == null || messages.isEmpty()) {
            throw new ModelProviderException("At least one chat message is required");
        }
    }

    private void setAuthorization(org.springframework.http.HttpHeaders headers, String apiKey) {
        if (StringUtils.hasText(apiKey)) {
            headers.setBearerAuth(apiKey);
        }
    }

    private String chatCompletionsUrl(String baseUrl) {
        return baseUrl.replaceAll("/+$", "") + "/chat/completions";
    }

    private record ChatCompletionRequest(
            String model,
            List<ChatMessage> messages,
            double temperature,
            int max_tokens,
            boolean stream) {
    }

    private record ChatCompletionResponse(List<Choice> choices) {
    }

    private record Choice(ResponseMessage message) {
    }

    private record ResponseMessage(String content) {
    }
}
