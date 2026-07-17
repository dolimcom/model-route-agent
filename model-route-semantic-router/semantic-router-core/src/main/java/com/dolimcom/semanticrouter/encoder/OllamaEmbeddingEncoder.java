package com.dolimcom.semanticrouter.encoder;

import com.dolimcom.semanticrouter.exception.SemanticRouterException;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class OllamaEmbeddingEncoder extends AbstractHttpEmbeddingEncoder {

    public OllamaEmbeddingEncoder(URI baseUri, String model, Duration timeout) {
        super(baseUri, model, timeout);
    }

    @Override
    public List<double[]> encodeAll(List<String> texts) {
        try {
            JsonNode response = postJson(baseUri.resolve("/api/embed"), Map.of("model", model, "input", texts), null);
            List<double[]> embeddings = toEmbeddings(response, "embeddings");
            if (!embeddings.isEmpty()) {
                return embeddings;
            }
        } catch (RuntimeException ignored) {
        }

        if (texts.size() == 1) {
            JsonNode response = postJson(baseUri.resolve("/api/embeddings"), Map.of("model", model, "prompt", texts.get(0)), null);
            JsonNode embeddingNode = response.get("embedding");
            if (embeddingNode != null && embeddingNode.isArray()) {
                return List.of(toVector(embeddingNode));
            }
        }

        throw new SemanticRouterException("Ollama embedding endpoint returned an unsupported payload");
    }
}
