package com.modelroute.provider;

import com.modelroute.config.ModelRouteProperties;
import java.time.Duration;
import java.util.List;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Calls providers exposing the OpenAI-compatible chat completions protocol.
 */
@Component
public class OpenAiCompatibleProviderClient implements ModelProviderClient {

    private final WebClient webClient;

    public OpenAiCompatibleProviderClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
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
                    .headers(headers -> headers.setBearerAuth(model.getApiKey()))
                    .bodyValue(new ChatCompletionRequest(
                            model.getModelName(), messages, model.getTemperature(), model.getMaxTokens()))
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

    private void validateRequest(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        if (!StringUtils.hasText(model.getBaseUrl()) || !StringUtils.hasText(model.getApiKey())
                || !StringUtils.hasText(model.getModelName())) {
            throw new ModelProviderException("OpenAI-compatible model configuration is incomplete: " + model.getId());
        }
        if (messages == null || messages.isEmpty()) {
            throw new ModelProviderException("At least one chat message is required");
        }
    }

    private String chatCompletionsUrl(String baseUrl) {
        return baseUrl.replaceAll("/+$", "") + "/chat/completions";
    }

    private record ChatCompletionRequest(
            String model,
            List<ChatMessage> messages,
            double temperature,
            int max_tokens) {
    }

    private record ChatCompletionResponse(List<Choice> choices) {
    }

    private record Choice(ResponseMessage message) {
    }

    private record ResponseMessage(String content) {
    }
}
