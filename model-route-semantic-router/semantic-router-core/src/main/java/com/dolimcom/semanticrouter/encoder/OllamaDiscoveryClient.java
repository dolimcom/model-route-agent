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

public class OllamaDiscoveryClient implements LocalModelDiscoveryClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final URI baseUri;
    private final Duration timeout;

    public OllamaDiscoveryClient(URI baseUri, Duration timeout) {
        this(baseUri, timeout, timeout);
    }

    public OllamaDiscoveryClient(URI baseUri, Duration connectTimeout, Duration requestTimeout) {
        this.baseUri = baseUri;
        this.timeout = requestTimeout;
        this.httpClient = HttpClient.newBuilder().connectTimeout(connectTimeout).build();
    }

    @Override
    public List<LocalModelDescriptor> discover() {
        try {
            HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("/api/tags")).GET().timeout(timeout).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new SemanticRouterException("Ollama discovery failed: " + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            List<LocalModelDescriptor> models = new ArrayList<>();
            JsonNode modelNodes = root.get("models");
            if (modelNodes != null && modelNodes.isArray()) {
                for (JsonNode modelNode : modelNodes) {
                    boolean supportsEmbeddings = false;
                    JsonNode capabilities = modelNode.get("capabilities");
                    if (capabilities != null && capabilities.isArray()) {
                        for (JsonNode capability : capabilities) {
                            if ("embedding".equalsIgnoreCase(capability.asText())) {
                                supportsEmbeddings = true;
                                break;
                            }
                        }
                    }
                    models.add(new LocalModelDescriptor("OLLAMA", baseUri.toString(), modelNode.path("name").asText(), supportsEmbeddings));
                }
            }
            return models;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SemanticRouterException("Ollama discovery failed", ex);
        } catch (IOException ex) {
            throw new SemanticRouterException("Ollama discovery failed", ex);
        }
    }
}
