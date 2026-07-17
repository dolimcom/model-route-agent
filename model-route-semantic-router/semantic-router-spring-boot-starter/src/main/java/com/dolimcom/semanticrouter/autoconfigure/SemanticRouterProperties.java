package com.dolimcom.semanticrouter.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("semantic.router")
public class SemanticRouterProperties {

    private boolean enabled = true;
    private String routesLocation = "classpath:semantic-router/routes.yml";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(20);
    private int inputPreviewLength = 96;
    private LocalModelProperties localModel = new LocalModelProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRoutesLocation() {
        return routesLocation;
    }

    public void setRoutesLocation(String routesLocation) {
        this.routesLocation = routesLocation;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getInputPreviewLength() {
        return inputPreviewLength;
    }

    public void setInputPreviewLength(int inputPreviewLength) {
        this.inputPreviewLength = inputPreviewLength;
    }

    public LocalModelProperties getLocalModel() {
        return localModel;
    }

    public void setLocalModel(LocalModelProperties localModel) {
        this.localModel = localModel;
    }

    public static class LocalModelProperties {

        private Provider provider = Provider.OLLAMA;
        private String baseUrl = "http://127.0.0.1:11434";
        private String model = "bge-m3:latest";
        private String apiKey = "EMPTY";

        public Provider getProvider() {
            return provider;
        }

        public void setProvider(Provider provider) {
            this.provider = provider;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    public enum Provider {
        OLLAMA,
        LM_STUDIO,
        LOCAL_AI,
        OPENAI_COMPATIBLE
    }
}
