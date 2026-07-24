package com.dolimcom.semanticrouter.encoder;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenAiCompatibleEmbeddingEncoder extends AbstractHttpEmbeddingEncoder {

    private final String apiKey;

    public OpenAiCompatibleEmbeddingEncoder(URI baseUri, String model, String apiKey, Duration timeout) {
        super(baseUri, model, timeout);
        this.apiKey = apiKey;
    }

    public OpenAiCompatibleEmbeddingEncoder(
            URI baseUri,
            String model,
            String apiKey,
            Duration connectTimeout,
            Duration requestTimeout) {
        super(baseUri, model, connectTimeout, requestTimeout);
        this.apiKey = apiKey;
    }

    @Override
    public List<double[]> encodeAll(List<String> texts) {
        JsonNode response = postJson(
                baseUri.resolve("/v1/embeddings"),
                Map.of("model", model, "input", texts),
                apiKey
        );
        List<double[]> embeddings = new ArrayList<>();
        JsonNode data = response.get("data");
        if (data != null && data.isArray()) {
            for (JsonNode item : data) {
                embeddings.add(toVector(item.get("embedding")));
            }
        }
        return embeddings;
    }
}
