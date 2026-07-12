package com.modelroute.config;

import com.modelroute.domain.TaskType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "model-route")
public class ModelRouteProperties {

    @NotEmpty(message = "model-route.models must contain at least one model")
    @Valid
    private List<ModelDefinition> models = new ArrayList<>();

    @NotNull(message = "model-route.router must be configured")
    @Valid
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

        @NotBlank(message = "model id must not be blank")
        private String id;

        @NotBlank(message = "model display-name must not be blank")
        private String displayName;

        @NotBlank(message = "model provider must not be blank")
        private String provider;

        @NotEmpty(message = "model supported-tasks must not be empty")
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

        @NotBlank(message = "model-route.router.fallback-model-id must not be blank")
        private String fallbackModelId;

        @NotEmpty(message = "model-route.router.keywords must not be empty")
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
