package com.modelroute.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.modelroute.config.ModelRouteProperties;
import com.modelroute.config.RuntimeConfigProperties;
import com.modelroute.domain.TaskType;
import com.modelroute.dto.TaskModelConfigRequest;
import com.modelroute.dto.TaskModelConfigResponse;
import com.modelroute.dto.ModelConfigurationStatusResponse;
import com.modelroute.provider.ProviderTypeResolver;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RuntimeModelConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(RuntimeModelConfigurationService.class);

    private final ModelRouteProperties properties;
    private final RuntimeConfigProperties runtimeProperties;
    private final ModelRegistry modelRegistry;
    private final ProviderTypeResolver providerTypeResolver;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public RuntimeModelConfigurationService(
            ModelRouteProperties properties,
            RuntimeConfigProperties runtimeProperties,
            ModelRegistry modelRegistry,
            ProviderTypeResolver providerTypeResolver) {
        this.properties = properties;
        this.runtimeProperties = runtimeProperties;
        this.modelRegistry = modelRegistry;
        this.providerTypeResolver = providerTypeResolver;
    }

    @PostConstruct
    public void initialize() {
        if (!runtimeProperties.isEnabled()) {
            return;
        }
        List<ModelRouteProperties.ModelDefinition> bootstrapModels =
                new ArrayList<>(modelRegistry.getRegisteredModels());
        Optional<List<ModelRouteProperties.ModelDefinition>> persisted = loadPersisted();
        List<ModelRouteProperties.ModelDefinition> source = persisted
                .map(models -> restoreMissingCredentials(models, bootstrapModels))
                .orElse(bootstrapModels);
        List<ModelRouteProperties.ModelDefinition> normalized = normalizeTaskModels(source);
        updateMappings(normalized);
        modelRegistry.replaceModels(normalized);
        if (persisted.isPresent()) {
            // Rewrite legacy runtime files whose secrets were omitted by the previous serializer.
            persist(normalized);
        }
        log.info("Runtime model configuration initialized with {} task models", normalized.size());
    }

    public List<TaskModelConfigResponse> list() {
        return modelRegistry.getRegisteredModels().stream()
                .flatMap(model -> model.getSupportedTasks().stream()
                        .map(taskType -> toResponse(taskType, model)))
                .sorted(Comparator.comparing(response -> response.taskType().ordinal()))
                .toList();
    }

    public ModelConfigurationStatusResponse status() {
        List<TaskType> mockTasks = modelRegistry.getRegisteredModels().stream()
                .filter(model -> "mock".equalsIgnoreCase(model.getProvider()))
                .flatMap(model -> model.getSupportedTasks().stream())
                .distinct()
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .toList();
        int totalSlots = TaskType.values().length;
        int configuredSlots = totalSlots - mockTasks.size();
        return new ModelConfigurationStatusResponse(
                Files.isRegularFile(configPath()),
                configuredSlots,
                totalSlots,
                configuredSlots == totalSlots,
                mockTasks);
    }

    public synchronized TaskModelConfigResponse update(TaskType taskType, TaskModelConfigRequest request) {
        ensureEnabled();
        String id = taskType.name().toLowerCase(Locale.ROOT);
        ModelRouteProperties.ModelDefinition existing = findByTask(taskType)
                .orElseThrow(() -> new IllegalStateException("No model slot exists for " + taskType));
        String modelName = request.modelName().trim();
        String baseUrl = request.baseUrl().trim().replaceAll("/+$", "");
        String provider = providerTypeResolver.resolve(modelName, baseUrl);
        boolean sameCredentialScope = provider.equalsIgnoreCase(existing.getProvider())
                && baseUrl.equalsIgnoreCase(existing.getBaseUrl());
        String apiKey = StringUtils.hasText(request.apiKey())
                ? request.apiKey().trim()
                : (sameCredentialScope ? existing.getApiKey() : null);

        ModelRouteProperties.ModelDefinition replacement = new ModelRouteProperties.ModelDefinition();
        replacement.setId(id);
        replacement.setDisplayName(StringUtils.hasText(request.displayName())
                ? request.displayName().trim()
                : modelName);
        replacement.setProvider(provider);
        replacement.setEnabled(true);
        replacement.setBaseUrl(baseUrl);
        replacement.setApiKey(apiKey);
        replacement.setModelName(modelName.startsWith("ollama/") ? modelName.substring("ollama/".length()) : modelName);
        replacement.setTimeoutMs(request.timeoutMs());
        replacement.setMaxTokens(request.maxTokens());
        replacement.setTemperature(request.temperature());
        replacement.setSupportedTasks(List.of(taskType));

        List<ModelRouteProperties.ModelDefinition> previous = new ArrayList<>(modelRegistry.getRegisteredModels());
        List<ModelRouteProperties.ModelDefinition> updated = previous.stream()
                .filter(model -> !model.getSupportedTasks().contains(taskType))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        updated.add(replacement);
        updated.sort(Comparator.comparing(model -> model.getSupportedTasks().get(0).ordinal()));

        updateMappings(updated);
        try {
            modelRegistry.replaceModels(updated);
            persist(updated);
        } catch (RuntimeException exception) {
            updateMappings(previous);
            modelRegistry.replaceModels(previous);
            throw exception;
        }
        log.info("Updated model configuration: task={}, model={}, provider={}",
                taskType, replacement.getModelName(), replacement.getProvider());
        return toResponse(taskType, replacement);
    }

    private List<ModelRouteProperties.ModelDefinition> normalizeTaskModels(
            Collection<ModelRouteProperties.ModelDefinition> source) {
        List<ModelRouteProperties.ModelDefinition> result = new ArrayList<>();
        for (TaskType taskType : TaskType.values()) {
            ModelRouteProperties.ModelDefinition sourceModel = source.stream()
                    .filter(model -> model.getSupportedTasks().contains(taskType))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No configured model supports " + taskType));
            result.add(copyForTask(sourceModel, taskType));
        }
        return result;
    }

    private ModelRouteProperties.ModelDefinition copyForTask(
            ModelRouteProperties.ModelDefinition source,
            TaskType taskType) {
        ModelRouteProperties.ModelDefinition copy = new ModelRouteProperties.ModelDefinition();
        copy.setId(taskType.name().toLowerCase(Locale.ROOT));
        copy.setDisplayName(source.getDisplayName());
        copy.setProvider(source.getProvider());
        copy.setEnabled(source.isEnabled());
        copy.setBaseUrl(source.getBaseUrl());
        copy.setApiKey(source.getApiKey());
        copy.setModelName(source.getModelName());
        copy.setTimeoutMs(source.getTimeoutMs());
        copy.setMaxTokens(source.getMaxTokens());
        copy.setTemperature(source.getTemperature());
        copy.setSupportedTasks(List.of(taskType));
        return copy;
    }

    private Optional<ModelRouteProperties.ModelDefinition> findByTask(TaskType taskType) {
        return modelRegistry.getRegisteredModels().stream()
                .filter(model -> model.getSupportedTasks().contains(taskType))
                .findFirst();
    }

    private void updateMappings(Collection<ModelRouteProperties.ModelDefinition> models) {
        for (ModelRouteProperties.SemanticRouteMapping mapping
                : properties.getRouter().getSemanticMappings().values()) {
            String modelId = models.stream()
                    .filter(model -> model.getSupportedTasks().contains(mapping.getTaskType()))
                    .map(ModelRouteProperties.ModelDefinition::getId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No runtime model supports " + mapping.getTaskType()));
            mapping.setModelId(modelId);
        }
        properties.getRouter().setFallbackModelId("general");
    }

    private Optional<List<ModelRouteProperties.ModelDefinition>> loadPersisted() {
        Path path = configPath();
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        try {
            RuntimeModelFile file = yamlMapper.readValue(path.toFile(), RuntimeModelFile.class);
            return file.models() == null || file.models().isEmpty()
                    ? Optional.empty()
                    : Optional.of(file.models().stream()
                            .map(PersistedModel::toDefinition)
                            .toList());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read runtime model configuration: " + path, exception);
        }
    }

    private List<ModelRouteProperties.ModelDefinition> restoreMissingCredentials(
            List<ModelRouteProperties.ModelDefinition> persisted,
            Collection<ModelRouteProperties.ModelDefinition> bootstrapModels) {
        for (ModelRouteProperties.ModelDefinition model : persisted) {
            if (StringUtils.hasText(model.getApiKey()) || !requiresApiKey(model)) {
                continue;
            }
            bootstrapModels.stream()
                    .filter(candidate -> sharesTask(model, candidate))
                    .filter(candidate -> sameCredentialScope(model, candidate))
                    .map(ModelRouteProperties.ModelDefinition::getApiKey)
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .ifPresent(apiKey -> {
                        model.setApiKey(apiKey);
                        log.info("Restored omitted credential for runtime model {} from bootstrap configuration",
                                model.getId());
                    });
        }
        return persisted;
    }

    private boolean requiresApiKey(ModelRouteProperties.ModelDefinition model) {
        if (!model.isEnabled() || "mock".equalsIgnoreCase(model.getProvider())
                || "ollama".equalsIgnoreCase(model.getProvider())) {
            return false;
        }
        String baseUrl = normalizeBaseUrl(model.getBaseUrl());
        return !(baseUrl.contains("localhost") || baseUrl.contains("127.0.0.1") || baseUrl.contains("[::1]"));
    }

    private boolean sharesTask(
            ModelRouteProperties.ModelDefinition left,
            ModelRouteProperties.ModelDefinition right) {
        return left.getSupportedTasks().stream().anyMatch(right.getSupportedTasks()::contains);
    }

    private boolean sameCredentialScope(
            ModelRouteProperties.ModelDefinition left,
            ModelRouteProperties.ModelDefinition right) {
        return left.getProvider().equalsIgnoreCase(right.getProvider())
                && normalizeBaseUrl(left.getBaseUrl()).equals(normalizeBaseUrl(right.getBaseUrl()));
    }

    private String normalizeBaseUrl(String value) {
        return value == null ? "" : value.trim().replaceAll("/+$", "").toLowerCase(Locale.ROOT);
    }

    private void persist(List<ModelRouteProperties.ModelDefinition> models) {
        Path path = configPath();
        Path temporary = null;
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            temporary = Files.createTempFile(path.getParent(), "models-", ".tmp");
            yamlMapper.writeValue(temporary.toFile(), new RuntimeModelFile(models.stream()
                    .map(PersistedModel::fromDefinition)
                    .toList()));
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to persist runtime model configuration: " + path, exception);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // Best-effort cleanup after a failed atomic replacement.
                }
            }
        }
    }

    private TaskModelConfigResponse toResponse(
            TaskType taskType,
            ModelRouteProperties.ModelDefinition model) {
        return new TaskModelConfigResponse(
                taskType,
                model.getId(),
                model.getDisplayName(),
                model.getProvider(),
                model.getBaseUrl(),
                StringUtils.hasText(model.getApiKey()),
                model.getModelName(),
                model.getTimeoutMs(),
                model.getMaxTokens(),
                model.getTemperature());
    }

    private Path configPath() {
        return Path.of(runtimeProperties.getModelsFile()).toAbsolutePath().normalize();
    }

    private void ensureEnabled() {
        if (!runtimeProperties.isEnabled()) {
            throw new IllegalStateException("Runtime configuration is disabled");
        }
    }

    private record RuntimeModelFile(List<PersistedModel> models) {
    }

    private record PersistedModel(
            String id,
            String displayName,
            String provider,
            List<TaskType> supportedTasks,
            boolean enabled,
            String baseUrl,
            String apiKey,
            String modelName,
            int timeoutMs,
            int maxTokens,
            double temperature) {

        private static PersistedModel fromDefinition(ModelRouteProperties.ModelDefinition model) {
            return new PersistedModel(
                    model.getId(),
                    model.getDisplayName(),
                    model.getProvider(),
                    model.getSupportedTasks(),
                    model.isEnabled(),
                    model.getBaseUrl(),
                    model.getApiKey(),
                    model.getModelName(),
                    model.getTimeoutMs(),
                    model.getMaxTokens(),
                    model.getTemperature());
        }

        private ModelRouteProperties.ModelDefinition toDefinition() {
            ModelRouteProperties.ModelDefinition model = new ModelRouteProperties.ModelDefinition();
            model.setId(id);
            model.setDisplayName(displayName);
            model.setProvider(provider);
            model.setSupportedTasks(supportedTasks == null ? List.of() : supportedTasks);
            model.setEnabled(enabled);
            model.setBaseUrl(baseUrl);
            model.setApiKey(apiKey);
            model.setModelName(modelName);
            model.setTimeoutMs(timeoutMs);
            model.setMaxTokens(maxTokens);
            model.setTemperature(temperature);
            return model;
        }
    }
}
