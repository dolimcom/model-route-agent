package com.dolimcom.semanticrouter.encoder;

import com.dolimcom.semanticrouter.api.SemanticEncoder;
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

abstract class AbstractHttpEmbeddingEncoder implements SemanticEncoder {

    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;
    protected final URI baseUri;
    protected final String model;
    protected final Duration timeout;

    protected AbstractHttpEmbeddingEncoder(URI baseUri, String model, Duration timeout) {
        this.httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
        this.objectMapper = new ObjectMapper();
        this.baseUri = baseUri;
        this.model = model;
        this.timeout = timeout;
    }

    protected JsonNode postJson(URI uri, Object body, String bearerToken) {
        try {
            String payload = objectMapper.writeValueAsString(body);
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload));
            if (bearerToken != null && !bearerToken.isBlank()) {
                builder.header("Authorization", "Bearer " + bearerToken);
            }
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new SemanticRouterException("Embedding request failed: " + response.statusCode() + " " + response.body());
            }
            return objectMapper.readTree(response.body());
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SemanticRouterException("Embedding request failed", ex);
        }
    }

    protected List<double[]> toEmbeddings(JsonNode node, String fieldName) {
        List<double[]> embeddings = new ArrayList<>();
        JsonNode field = node.get(fieldName);
        if (field == null || !field.isArray()) {
            return embeddings;
        }
        for (JsonNode embeddingNode : field) {
            embeddings.add(toVector(embeddingNode));
        }
        return embeddings;
    }

    protected double[] toVector(JsonNode node) {
        double[] vector = new double[node.size()];
        for (int i = 0; i < node.size(); i++) {
            vector[i] = node.get(i).asDouble();
        }
        return vector;
    }

    @Override
    public String version() {
        return model;
    }
}
