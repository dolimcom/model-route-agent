package com.modelroute.service;

import com.modelroute.config.ModelRouteProperties;
import com.modelroute.domain.TaskType;

import java.util.*;

import org.springframework.beans.factory.InitializingBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Validates model configuration once at startup and provides immutable lookup access at runtime.
 */
@Service
public class ModelRegistry implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ModelRegistry.class);

    private final ModelRouteProperties properties;
    private Map<String, ModelRouteProperties.ModelDefinition> modelsById = Map.of();

    public ModelRegistry(ModelRouteProperties properties) {
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() {
        replaceModels(properties.getModels());
    }

    public synchronized void replaceModels(Collection<ModelRouteProperties.ModelDefinition> models) {
        Map<String, ModelRouteProperties.ModelDefinition> registeredModels = new LinkedHashMap<>();

        if (models == null || models.isEmpty()) {
            throw new IllegalStateException("At least one model must be configured");
        }
        for (ModelRouteProperties.ModelDefinition model : models) {
            validateModel(model);
            ModelRouteProperties.ModelDefinition previous = registeredModels.putIfAbsent(model.getId(), model);
            if (previous != null) {
                throw new IllegalStateException("Duplicate model id configured: " + model.getId());
            }
        }

        resolveCanonicalBootstrapMappings(registeredModels);
        validateRouter(registeredModels);
        modelsById = Collections.unmodifiableMap(new LinkedHashMap<>(registeredModels));
        properties.setModels(new ArrayList<>(registeredModels.values()));
    }

    public ModelRouteProperties.ModelDefinition getRequiredModel(String modelId) {
        ModelRouteProperties.ModelDefinition model = modelsById.get(modelId);
        if (model == null) {
            throw new IllegalArgumentException("Unknown model id: " + modelId);
        }
        return model;
    }

    public ModelRouteProperties.ModelDefinition getFallbackModel() {
        ModelRouteProperties.ModelDefinition fallbackModel = getRequiredModel(properties.getRouter().getFallbackModelId());
        if (!fallbackModel.isEnabled()) {
            throw new IllegalStateException("Configured fallback model is disabled: " + fallbackModel.getId());
        }
        return fallbackModel;
    }

    public Optional<ModelRouteProperties.ModelDefinition> findFirstSupporting(TaskType taskType) {
        return modelsById.values().stream()
                .filter(ModelRouteProperties.ModelDefinition::isEnabled)
                .filter(model -> model.getSupportedTasks().contains(taskType))
                .findFirst();
    }

    public Collection<ModelRouteProperties.ModelDefinition> getRegisteredModels() {
        return List.copyOf(modelsById.values());
    }

    private void validateModel(ModelRouteProperties.ModelDefinition model) {
        if (model == null) {
            throw new IllegalStateException("Model configuration must not contain null entries");
        }
        if (!StringUtils.hasText(model.getId())) {
            throw new IllegalStateException("Model id must not be blank");
        }
        if (!model.getId().equals(model.getId().trim())) {
            throw new IllegalStateException("Model id must not contain leading or trailing whitespace: " + model.getId());
        }
        if (!StringUtils.hasText(model.getDisplayName())) {
            throw new IllegalStateException("Model display-name must not be blank for " + model.getId());
        }
        if (!StringUtils.hasText(model.getProvider())) {
            throw new IllegalStateException("Model provider must not be blank for " + model.getId());
        }
        if (model.getSupportedTasks() == null || model.getSupportedTasks().isEmpty()) {
            throw new IllegalStateException("Model supported-tasks must not be empty for " + model.getId());
        }
        if (model.getSupportedTasks().stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("Model supported-tasks must not contain null for " + model.getId());
        }
        if (model.isEnabled() && !"mock".equalsIgnoreCase(model.getProvider())) {
            if (!StringUtils.hasText(model.getBaseUrl())) {
                throw new IllegalStateException("Model base-url must not be blank for " + model.getId());
            }
            if (!"ollama".equalsIgnoreCase(model.getProvider())
                    && !isLocalEndpoint(model.getBaseUrl())
                    && !StringUtils.hasText(model.getApiKey())) {
                throw new IllegalStateException("Model api-key must not be blank for " + model.getId());
            }
            if (!StringUtils.hasText(model.getModelName())) {
                throw new IllegalStateException("Model model-name must not be blank for " + model.getId());
            }
        }
    }

    private boolean isLocalEndpoint(String baseUrl) {
        String normalized = baseUrl.toLowerCase(Locale.ROOT);
        return normalized.contains("localhost") || normalized.contains("127.0.0.1") || normalized.contains("[::1]");
    }

    private void validateRouter(Map<String, ModelRouteProperties.ModelDefinition> registeredModels) {
        ModelRouteProperties.Router router = properties.getRouter();
        if (router == null || !StringUtils.hasText(router.getFallbackModelId())) {
            throw new IllegalStateException("Router fallback-model-id must be configured");
        }
        if (!registeredModels.containsKey(router.getFallbackModelId())) {
            throw new IllegalStateException(
                    "Configured fallback model does not exist: " + router.getFallbackModelId());
        }
        if (router.getKeywords() == null || router.getKeywords().isEmpty()) {
            throw new IllegalStateException("Router keywords must contain at least one task type");
        }

        List<TaskType> emptyKeywordTypes = new ArrayList<>();
        for (Map.Entry<TaskType, List<String>> entry : router.getKeywords().entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue().isEmpty()
                    || entry.getValue().stream().anyMatch(keyword -> !StringUtils.hasText(keyword))) {
                emptyKeywordTypes.add(entry.getKey());
            }
        }
        if (!emptyKeywordTypes.isEmpty()) {
            throw new IllegalStateException("Router keywords must not be empty for task types: " + emptyKeywordTypes);
        }

        validateSemanticMappings(router, registeredModels);
    }

    private void resolveCanonicalBootstrapMappings(
            Map<String, ModelRouteProperties.ModelDefinition> registeredModels) {
        ModelRouteProperties.Router router = properties.getRouter();
        if (router == null || router.getSemanticMappings() == null) {
            return;
        }
        for (Map.Entry<String, ModelRouteProperties.SemanticRouteMapping> entry
                : router.getSemanticMappings().entrySet()) {
            ModelRouteProperties.SemanticRouteMapping mapping = entry.getValue();
            if (mapping == null || mapping.getTaskType() == null
                    || registeredModels.containsKey(mapping.getModelId())) {
                continue;
            }
            String canonicalId = mapping.getTaskType().name().toLowerCase(Locale.ROOT);
            if (!canonicalId.equals(mapping.getModelId())) {
                continue;
            }
            registeredModels.values().stream()
                    .filter(ModelRouteProperties.ModelDefinition::isEnabled)
                    .filter(model -> model.getSupportedTasks().contains(mapping.getTaskType()))
                    .findFirst()
                    .ifPresent(model -> {
                        log.info("Resolved bootstrap semantic mapping: route={}, canonicalModel={}, sourceModel={}",
                                entry.getKey(), canonicalId, model.getId());
                        mapping.setModelId(model.getId());
                    });
        }
    }

    private void validateSemanticMappings(
            ModelRouteProperties.Router router,
            Map<String, ModelRouteProperties.ModelDefinition> registeredModels) {
        Map<String, ModelRouteProperties.SemanticRouteMapping> mappings = router.getSemanticMappings();
        if (mappings == null || mappings.isEmpty()) {
            throw new IllegalStateException("Router semantic-mappings must contain at least one route");
        }

        for (Map.Entry<String, ModelRouteProperties.SemanticRouteMapping> entry : mappings.entrySet()) {
            String routeId = entry.getKey();
            ModelRouteProperties.SemanticRouteMapping mapping = entry.getValue();
            if (!StringUtils.hasText(routeId) || !routeId.equals(routeId.trim())) {
                throw new IllegalStateException("Semantic route id must not be blank or padded: " + routeId);
            }
            if (mapping == null || mapping.getTaskType() == null || !StringUtils.hasText(mapping.getModelId())) {
                throw new IllegalStateException("Semantic route mapping is incomplete: " + routeId);
            }

            ModelRouteProperties.ModelDefinition model = registeredModels.get(mapping.getModelId());
            if (model == null) {
                throw new IllegalStateException(
                        "Semantic route " + routeId + " references unknown model: " + mapping.getModelId());
            }
            if (!model.isEnabled()) {
                throw new IllegalStateException(
                        "Semantic route " + routeId + " references disabled model: " + mapping.getModelId());
            }
            if (!model.getSupportedTasks().contains(mapping.getTaskType())) {
                throw new IllegalStateException("Semantic route " + routeId + " maps task "
                        + mapping.getTaskType() + " to unsupported model: " + mapping.getModelId());
            }
        }
    }
}
