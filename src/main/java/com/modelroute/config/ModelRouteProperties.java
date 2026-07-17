package com.modelroute.config;

import com.modelroute.domain.TaskType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
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

        private boolean enabled = true;
        private String baseUrl;

        @JsonIgnore
        private String apiKey;

        private String modelName;
        private int timeoutMs = 10000;
        private int maxTokens = 1024;
        private double temperature = 0.7;

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

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public int getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        public int getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
    }

    public static class Router {

        @NotBlank(message = "model-route.router.fallback-model-id must not be blank")
        private String fallbackModelId;

        @NotEmpty(message = "model-route.router.keywords must not be empty")
        private Map<TaskType, List<String>> keywords = new EnumMap<>(TaskType.class);

        @NotNull(message = "model-route.router.scoring must be configured")
        @Valid
        private Scoring scoring = new Scoring();

        @NotNull(message = "model-route.router.context must be configured")
        @Valid
        private ContextRouting context = new ContextRouting();

        @NotEmpty(message = "model-route.router.semantic-mappings must not be empty")
        @Valid
        private Map<String, SemanticRouteMapping> semanticMappings = new LinkedHashMap<>();

        @NotNull(message = "model-route.router.focus must be configured")
        @Valid
        private FocusRouting focus = new FocusRouting();

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

        public Scoring getScoring() {
            return scoring;
        }

        public void setScoring(Scoring scoring) {
            this.scoring = scoring;
        }

        public ContextRouting getContext() {
            return context;
        }

        public void setContext(ContextRouting context) {
            this.context = context;
        }

        public Map<String, SemanticRouteMapping> getSemanticMappings() {
            return semanticMappings;
        }

        public void setSemanticMappings(Map<String, SemanticRouteMapping> semanticMappings) {
            this.semanticMappings = semanticMappings;
        }

        public FocusRouting getFocus() {
            return focus;
        }

        public void setFocus(FocusRouting focus) {
            this.focus = focus;
        }
    }

    public static class SemanticRouteMapping {

        @NotNull(message = "semantic mapping task-type must be configured")
        private TaskType taskType;

        @NotBlank(message = "semantic mapping model-id must not be blank")
        private String modelId;

        public TaskType getTaskType() {
            return taskType;
        }

        public void setTaskType(TaskType taskType) {
            this.taskType = taskType;
        }

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }
    }

    public static class FocusRouting {

        private boolean enabled;

        @Min(value = 1, message = "minimum-focused-length must be at least 1")
        private int minimumFocusedLength = 2;
        private List<String> switchMarkers = new ArrayList<>();
        private List<String> focusMarkers = new ArrayList<>();
        private List<String> trailingConstraints = new ArrayList<>();
        private List<String> acknowledgements = new ArrayList<>();
        private List<String> requestMarkers = new ArrayList<>();
        private List<String> ignoredRequestPrefixes = new ArrayList<>();
        private List<String> boundaryDelimiters = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMinimumFocusedLength() {
            return minimumFocusedLength;
        }

        public void setMinimumFocusedLength(int minimumFocusedLength) {
            this.minimumFocusedLength = minimumFocusedLength;
        }

        public List<String> getSwitchMarkers() {
            return switchMarkers;
        }

        public void setSwitchMarkers(List<String> switchMarkers) {
            this.switchMarkers = switchMarkers;
        }

        public List<String> getFocusMarkers() {
            return focusMarkers;
        }

        public void setFocusMarkers(List<String> focusMarkers) {
            this.focusMarkers = focusMarkers;
        }

        public List<String> getTrailingConstraints() {
            return trailingConstraints;
        }

        public void setTrailingConstraints(List<String> trailingConstraints) {
            this.trailingConstraints = trailingConstraints;
        }

        public List<String> getAcknowledgements() {
            return acknowledgements;
        }

        public void setAcknowledgements(List<String> acknowledgements) {
            this.acknowledgements = acknowledgements;
        }

        public List<String> getRequestMarkers() {
            return requestMarkers;
        }

        public void setRequestMarkers(List<String> requestMarkers) {
            this.requestMarkers = requestMarkers;
        }

        public List<String> getIgnoredRequestPrefixes() {
            return ignoredRequestPrefixes;
        }

        public void setIgnoredRequestPrefixes(List<String> ignoredRequestPrefixes) {
            this.ignoredRequestPrefixes = ignoredRequestPrefixes;
        }

        public List<String> getBoundaryDelimiters() {
            return boundaryDelimiters;
        }

        public void setBoundaryDelimiters(List<String> boundaryDelimiters) {
            this.boundaryDelimiters = boundaryDelimiters;
        }
    }

    public static class ContextRouting {

        private boolean enabled = true;

        @NotBlank(message = "followup-route-id must not be blank")
        private String followupRouteId = "followup";

        @NotBlank(message = "general-route-id must not be blank")
        private String generalRouteId = "general";

        @DecimalMin(value = "0.0", message = "minimum-followup-score must be at least 0")
        @DecimalMax(value = "1.0", message = "minimum-followup-score must not exceed 1")
        private double minimumFollowupScore = 0.50d;

        @DecimalMin(value = "0.0", message = "maximum-general-score-gap must be at least 0")
        @DecimalMax(value = "1.0", message = "maximum-general-score-gap must not exceed 1")
        private double maximumGeneralScoreGap = 0.12d;

        @DecimalMin(value = "0.0", message = "maximum-specialist-score-gap must be at least 0")
        @DecimalMax(value = "1.0", message = "maximum-specialist-score-gap must not exceed 1")
        private double maximumSpecialistScoreGap = 0.02d;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getFollowupRouteId() {
            return followupRouteId;
        }

        public void setFollowupRouteId(String followupRouteId) {
            this.followupRouteId = followupRouteId;
        }

        public String getGeneralRouteId() {
            return generalRouteId;
        }

        public void setGeneralRouteId(String generalRouteId) {
            this.generalRouteId = generalRouteId;
        }

        public double getMinimumFollowupScore() {
            return minimumFollowupScore;
        }

        public void setMinimumFollowupScore(double minimumFollowupScore) {
            this.minimumFollowupScore = minimumFollowupScore;
        }

        public double getMaximumGeneralScoreGap() {
            return maximumGeneralScoreGap;
        }

        public void setMaximumGeneralScoreGap(double maximumGeneralScoreGap) {
            this.maximumGeneralScoreGap = maximumGeneralScoreGap;
        }

        public double getMaximumSpecialistScoreGap() {
            return maximumSpecialistScoreGap;
        }

        public void setMaximumSpecialistScoreGap(double maximumSpecialistScoreGap) {
            this.maximumSpecialistScoreGap = maximumSpecialistScoreGap;
        }
    }

    public static class Scoring {

        @Min(value = 1, message = "keyword-weight must be at least 1")
        private int keywordWeight = 2;

        @Min(value = 1, message = "code-syntax-weight must be at least 1")
        private int codeSyntaxWeight = 1;

        @Min(value = 1, message = "strong-signal-weight must be at least 1")
        private int strongSignalWeight = 4;

        @Min(value = 1, message = "minimum-score must be at least 1")
        private int minimumScore = 2;

        @Min(value = 1, message = "minimum-score-gap must be at least 1")
        private int minimumScoreGap = 1;

        public int getKeywordWeight() {
            return keywordWeight;
        }

        public void setKeywordWeight(int keywordWeight) {
            this.keywordWeight = keywordWeight;
        }

        public int getCodeSyntaxWeight() {
            return codeSyntaxWeight;
        }

        public void setCodeSyntaxWeight(int codeSyntaxWeight) {
            this.codeSyntaxWeight = codeSyntaxWeight;
        }

        public int getStrongSignalWeight() {
            return strongSignalWeight;
        }

        public void setStrongSignalWeight(int strongSignalWeight) {
            this.strongSignalWeight = strongSignalWeight;
        }

        public int getMinimumScore() {
            return minimumScore;
        }

        public void setMinimumScore(int minimumScore) {
            this.minimumScore = minimumScore;
        }

        public int getMinimumScoreGap() {
            return minimumScoreGap;
        }

        public void setMinimumScoreGap(int minimumScoreGap) {
            this.minimumScoreGap = minimumScoreGap;
        }
    }
}
