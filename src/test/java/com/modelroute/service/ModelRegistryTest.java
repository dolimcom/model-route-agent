package com.modelroute.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import com.modelroute.config.ModelRouteProperties;
import com.modelroute.domain.TaskType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ModelRegistryTest {

    @Test
    void rejectsFallbackModelThatIsNotRegistered() {
        ModelRouteProperties properties = validProperties();
        properties.getRouter().setFallbackModelId("missing-model");

        ModelRegistry registry = new ModelRegistry(properties);

        assertThatThrownBy(registry::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Configured fallback model does not exist: missing-model");
    }

    @Test
    void rejectsDuplicateModelIds() {
        ModelRouteProperties properties = validProperties();
        properties.setModels(List.of(model("general-mock", TaskType.GENERAL), model("general-mock", TaskType.DAILY)));

        ModelRegistry registry = new ModelRegistry(properties);

        assertThatThrownBy(registry::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate model id configured: general-mock");
    }

    @Test
    void rejectsSemanticMappingThatReferencesUnknownModel() {
        ModelRouteProperties properties = validProperties();
        properties.getRouter().setSemanticMappings(Map.of(
                "general", mapping(TaskType.GENERAL, "missing-model")));

        ModelRegistry registry = new ModelRegistry(properties);

        assertThatThrownBy(registry::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Semantic route general references unknown model: missing-model");
    }

    @Test
    void rejectsSemanticMappingToModelThatDoesNotSupportTask() {
        ModelRouteProperties properties = validProperties();
        properties.getRouter().setSemanticMappings(Map.of(
                "coding", mapping(TaskType.CODING, "general-mock")));

        ModelRegistry registry = new ModelRegistry(properties);

        assertThatThrownBy(registry::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Semantic route coding maps task CODING to unsupported model: general-mock");
    }

    @Test
    void resolvesCanonicalRuntimeSlotFromLegacyMultiTaskModelDuringBootstrap() {
        ModelRouteProperties properties = validProperties();
        properties.setModels(List.of(model("general", TaskType.GENERAL, TaskType.DAILY)));
        properties.getRouter().setFallbackModelId("general");
        properties.getRouter().setSemanticMappings(Map.of(
                "general", mapping(TaskType.GENERAL, "general"),
                "daily", mapping(TaskType.DAILY, "daily")));

        ModelRegistry registry = new ModelRegistry(properties);
        registry.afterPropertiesSet();

        assertThat(properties.getRouter().getSemanticMappings().get("daily").getModelId())
                .isEqualTo("general");
    }

    private ModelRouteProperties validProperties() {
        ModelRouteProperties properties = new ModelRouteProperties();
        properties.setModels(List.of(model("general-mock", TaskType.GENERAL)));

        ModelRouteProperties.Router router = new ModelRouteProperties.Router();
        router.setFallbackModelId("general-mock");
        router.setKeywords(Map.of(TaskType.DAILY, List.of("plan")));
        router.setSemanticMappings(Map.of("general", mapping(TaskType.GENERAL, "general-mock")));
        properties.setRouter(router);
        return properties;
    }

    private ModelRouteProperties.SemanticRouteMapping mapping(TaskType taskType, String modelId) {
        ModelRouteProperties.SemanticRouteMapping mapping = new ModelRouteProperties.SemanticRouteMapping();
        mapping.setTaskType(taskType);
        mapping.setModelId(modelId);
        return mapping;
    }

    private ModelRouteProperties.ModelDefinition model(String id, TaskType taskType) {
        return model(id, new TaskType[] {taskType});
    }

    private ModelRouteProperties.ModelDefinition model(String id, TaskType... taskTypes) {
        ModelRouteProperties.ModelDefinition model = new ModelRouteProperties.ModelDefinition();
        model.setId(id);
        model.setDisplayName(id);
        model.setProvider("mock");
        model.setSupportedTasks(List.of(taskTypes));
        return model;
    }
}
