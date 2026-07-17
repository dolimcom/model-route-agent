package com.dolimcom.semanticrouter.encoder;

import com.dolimcom.semanticrouter.exception.SemanticRouterException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class OpenAiCompatibleDiscoveryClient implements LocalModelDiscoveryClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final URI baseUri;
    private final Duration timeout;
    private final String provider;
    private final String apiKey;

    public OpenAiCompatibleDiscoveryClient(String provider, URI baseUri, String apiKey, Duration timeout) {
        this.provider = provider;
        this.baseUri = baseUri;
        this.apiKey = apiKey;
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
    }

    @Override
    public List<LocalModelDescriptor> discover() {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(baseUri.resolve("/v1/models")).GET().timeout(timeout);
            if (apiKey != null && !apiKey.isBlank()) {
                builder.header("Authorization", "Bearer " + apiKey);
            }
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new SemanticRouterException("Model discovery failed: " + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            List<LocalModelDescriptor> models = new ArrayList<>();
            JsonNode data = root.get("data");
            if (data != null && data.isArray()) {
                for (JsonNode item : data) {
                    models.add(new LocalModelDescriptor(provider, baseUri.toString(), item.path("id").asText(), true));
                }
            }
            return models;
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SemanticRouterException("Model discovery failed", ex);
        }
    }
}
