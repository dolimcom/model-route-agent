package com.modelroute.router;

import static org.assertj.core.api.Assertions.assertThat;

import com.modelroute.config.ModelRouteProperties;
import com.modelroute.domain.TaskType;
import com.modelroute.dto.RouteDecision;
import com.modelroute.service.ModelRegistry;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TaskRouterTest {

    @Test
    void routesStrongCodingSignalWithHighConfidence() throws Exception {
        TaskRouter taskRouter = taskRouter();

        RouteDecision decision = taskRouter.route("java.lang.NullPointerException\\n at com.example.Demo.main(Demo.java:10)");

        assertThat(decision.taskType()).isEqualTo(TaskType.CODING);
        assertThat(decision.modelId()).isEqualTo("coding-mock");
        assertThat(decision.fallbackUsed()).isFalse();
        assertThat(decision.confidence()).isEqualTo(0.86);
        assertThat(decision.scores()).containsEntry(TaskType.CODING, 6);
    }

    @Test
    void usesFallbackForAmbiguousKeywordScores() throws Exception {
        TaskRouter taskRouter = taskRouter();

        RouteDecision decision = taskRouter.route("请帮我润色明天的学习计划");

        assertThat(decision.taskType()).isEqualTo(TaskType.GENERAL);
        assertThat(decision.modelId()).isEqualTo("general-mock");
        assertThat(decision.fallbackUsed()).isTrue();
        assertThat(decision.scores()).containsEntry(TaskType.DAILY, 2);
        assertThat(decision.scores()).containsEntry(TaskType.LITERARY, 2);
    }

    @Test
    void usesFallbackWhenNoSignalExists() throws Exception {
        TaskRouter taskRouter = taskRouter();

        RouteDecision decision = taskRouter.route("你好，请介绍一下你自己");

        assertThat(decision.taskType()).isEqualTo(TaskType.GENERAL);
        assertThat(decision.fallbackUsed()).isTrue();
        assertThat(decision.confidence()).isZero();
        assertThat(decision.reason()).contains("Semantic router is unavailable");
    }

    private TaskRouter taskRouter() throws Exception {
        ModelRouteProperties properties = properties();
        ModelRegistry modelRegistry = new ModelRegistry(properties);
        modelRegistry.afterPropertiesSet();
        SemanticRouteAdapter semanticRouteAdapter = new SemanticRouteAdapter(
                Optional.empty(), modelRegistry, properties, new RoutingTextFocusExtractor(properties));
        return new TaskRouter(
                new RuleEngine(properties),
                new ScoreCalculator(properties),
                modelRegistry,
                properties,
                semanticRouteAdapter);
    }

    private ModelRouteProperties properties() {
        ModelRouteProperties properties = new ModelRouteProperties();
        properties.setModels(List.of(
                model("general-mock", List.of(TaskType.GENERAL, TaskType.DAILY)),
                model("literary-mock", List.of(TaskType.LITERARY)),
                model("coding-mock", List.of(TaskType.CODING)),
                model("math-mock", List.of(TaskType.MATH))));

        ModelRouteProperties.Router router = new ModelRouteProperties.Router();
        router.setFallbackModelId("general-mock");
        router.setKeywords(Map.of(
                TaskType.DAILY, List.of("计划"),
                TaskType.LITERARY, List.of("润色"),
                TaskType.CODING, List.of("java"),
                TaskType.MATH, List.of("数学")));
        router.setSemanticMappings(Map.of(
                "general", mapping(TaskType.GENERAL, "general-mock"),
                "daily", mapping(TaskType.DAILY, "general-mock"),
                "literary", mapping(TaskType.LITERARY, "literary-mock"),
                "coding", mapping(TaskType.CODING, "coding-mock"),
                "math", mapping(TaskType.MATH, "math-mock")));
        properties.setRouter(router);
        return properties;
    }

    private ModelRouteProperties.SemanticRouteMapping mapping(TaskType taskType, String modelId) {
        ModelRouteProperties.SemanticRouteMapping mapping = new ModelRouteProperties.SemanticRouteMapping();
        mapping.setTaskType(taskType);
        mapping.setModelId(modelId);
        return mapping;
    }

    private ModelRouteProperties.ModelDefinition model(String id, List<TaskType> supportedTasks) {
        ModelRouteProperties.ModelDefinition model = new ModelRouteProperties.ModelDefinition();
        model.setId(id);
        model.setDisplayName(id);
        model.setProvider("mock");
        model.setSupportedTasks(supportedTasks);
        return model;
    }
}
