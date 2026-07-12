package com.modelroute.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private ModelRouteProperties validProperties() {
        ModelRouteProperties properties = new ModelRouteProperties();
        properties.setModels(List.of(model("general-mock", TaskType.GENERAL)));

        ModelRouteProperties.Router router = new ModelRouteProperties.Router();
        router.setFallbackModelId("general-mock");
        router.setKeywords(Map.of(TaskType.DAILY, List.of("plan")));
        properties.setRouter(router);
        return properties;
    }

    private ModelRouteProperties.ModelDefinition model(String id, TaskType taskType) {
        ModelRouteProperties.ModelDefinition model = new ModelRouteProperties.ModelDefinition();
        model.setId(id);
        model.setDisplayName(id);
        model.setProvider("mock");
        model.setSupportedTasks(List.of(taskType));
        return model;
    }
}
