package com.modelroute.config;

import com.modelroute.domain.TaskType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "model-route")
public class ModelRouteProperties {

    private List<ModelDefinition> models = new ArrayList<>();
    private Router router = new Router();

    public List<ModelDefinition> getModels() {
        return models;
    }

    public void setModels(List<ModelDefinition> models) {
        this.models = models;
    }

    public Router getRouter() {
        return router;
    }

    public void setRouter(Router router) {
        this.router = router;
    }

    public static class ModelDefinition {
        private String id;
        private String displayName;
        private String provider;
        private List<TaskType> supportedTasks = new ArrayList<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public List<TaskType> getSupportedTasks() {
            return supportedTasks;
        }

        public void setSupportedTasks(List<TaskType> supportedTasks) {
            this.supportedTasks = supportedTasks;
        }
    }

    public static class Router {
        private String fallbackModelId;
        private Map<TaskType, List<String>> keywords = new EnumMap<>(TaskType.class);

        public String getFallbackModelId() {
            return fallbackModelId;
        }

        public void setFallbackModelId(String fallbackModelId) {
            this.fallbackModelId = fallbackModelId;
        }

        public Map<TaskType, List<String>> getKeywords() {
            return keywords;
        }

        public void setKeywords(Map<TaskType, List<String>> keywords) {
            this.keywords = keywords;
        }
    }
}
