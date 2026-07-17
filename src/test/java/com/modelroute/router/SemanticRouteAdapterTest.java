package com.modelroute.router;

import static org.assertj.core.api.Assertions.assertThat;

import com.dolimcom.semanticrouter.api.SemanticRouter;
import com.dolimcom.semanticrouter.model.ReasonCode;
import com.dolimcom.semanticrouter.model.RouteScore;
import com.dolimcom.semanticrouter.model.RouteTrace;
import com.dolimcom.semanticrouter.model.RoutingRequest;
import com.dolimcom.semanticrouter.model.RoutingResult;
import com.dolimcom.semanticrouter.model.RoutingStatus;
import com.modelroute.config.ModelRouteProperties;
import com.modelroute.domain.TaskType;
import com.modelroute.dto.RouteDecision;
import com.modelroute.service.ModelRegistry;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SemanticRouteAdapterTest {

    @Test
    void mapsAcceptedSemanticRouteToAgentDecision() throws Exception {
        RouteScore codingScore = new RouteScore("coding", "coding-mock", 0.82, 0.25, 0.76);
        SemanticRouter router = request -> result(
                RoutingStatus.ROUTED,
                "coding",
                "coding-mock",
                0.76,
                0.21,
                0.88,
                List.of(codingScore),
                ReasonCode.ACCEPTED);
        SemanticRouteAdapter adapter = adapter(router);

        Optional<RouteDecision> decision = adapter.route("服务启动后立即退出应该怎样定位", null);

        assertThat(decision).isPresent();
        assertThat(decision.orElseThrow().taskType()).isEqualTo(TaskType.CODING);
        assertThat(decision.orElseThrow().modelId()).isEqualTo("coding-mock");
        assertThat(decision.orElseThrow().confidence()).isEqualTo(0.88);
        assertThat(decision.orElseThrow().scores()).containsEntry(TaskType.CODING, 76);
        assertThat(decision.orElseThrow().reason()).contains("Semantic router selected CODING");
    }

    @Test
    void passesConversationTaskWithoutBiasingNewSemanticDecision() throws Exception {
        AtomicReference<RoutingRequest> capturedRequest = new AtomicReference<>();
        SemanticRouter router = request -> {
            capturedRequest.set(request);
            return result(
                    RoutingStatus.FALLBACK,
                    "literary",
                    "literary-mock",
                    0.0,
                    0.0,
                    0.0,
                    List.of(),
                    ReasonCode.LAST_KNOWN_GOOD);
        };
        SemanticRouteAdapter adapter = adapter(router);

        Optional<RouteDecision> decision = adapter.route("继续调整上面的内容", TaskType.LITERARY);

        assertThat(capturedRequest.get().lastKnownRouteId()).isEqualTo("literary");
        assertThat(capturedRequest.get().hints()).isEmpty();
        assertThat(decision).isPresent();
        assertThat(decision.orElseThrow().fallbackUsed()).isTrue();
    }

    @Test
    void rejectsSemanticRouteWithoutBusinessMapping() throws Exception {
        SemanticRouter router = request -> result(
                RoutingStatus.ROUTED,
                "unmapped-route",
                "missing-model",
                0.8,
                0.2,
                0.9,
                List.of(),
                ReasonCode.ACCEPTED);
        SemanticRouteAdapter adapter = adapter(router);

        assertThat(adapter.route("实现一个缓存", null)).isEmpty();
    }

    @Test
    void mapsRouteIdWithoutDependingOnTaskEnumOrSemanticTargetName() throws Exception {
        ModelRouteProperties properties = properties();
        properties.getRouter().getSemanticMappings().put(
                "software-engineering",
                mapping(TaskType.CODING, "coding-mock"));
        RouteScore score = new RouteScore("software-engineering", "arbitrary-target", 0.84, 0.0, 0.80);
        SemanticRouter router = request -> result(
                RoutingStatus.ROUTED,
                "software-engineering",
                "arbitrary-target",
                0.80,
                0.22,
                0.90,
                List.of(score),
                ReasonCode.ACCEPTED);

        RouteDecision decision = adapter(router, properties)
                .route("检查服务实现", null)
                .orElseThrow();

        assertThat(decision.taskType()).isEqualTo(TaskType.CODING);
        assertThat(decision.modelId()).isEqualTo("coding-mock");
        assertThat(decision.scores()).containsEntry(TaskType.CODING, 80);
    }

    @Test
    void returnsEmptyWhenSemanticRouterRejectsInput() throws Exception {
        SemanticRouter router = request -> result(
                RoutingStatus.REJECTED,
                null,
                null,
                0.2,
                0.01,
                0.0,
                List.of(),
                ReasonCode.OUT_OF_DOMAIN);
        SemanticRouteAdapter adapter = adapter(router);

        assertThat(adapter.route("无法归类的输入", null)).isEmpty();
    }

    @Test
    void preservesRejectedSemanticDiagnosticsForRuleFallback() throws Exception {
        RouteScore candidate = new RouteScore("coding", "coding-mock", 0.48, 0.0, 0.48);
        SemanticRouter router = request -> result(
                RoutingStatus.REJECTED,
                null,
                null,
                0.48,
                0.01,
                0.0,
                List.of(candidate),
                ReasonCode.LOW_CONFIDENCE);

        SemanticRouteAttempt attempt = adapter(router).attempt("定位服务异常", null);

        assertThat(attempt.decision()).isEmpty();
        assertThat(attempt.diagnostic())
                .contains("reason=LOW_CONFIDENCE")
                .contains("top=coding:0.48")
                .contains("margin=0.01");
    }

    @Test
    void inheritsPreviousTaskWhenSemanticRouterSelectsFollowup() throws Exception {
        List<RouteScore> candidates = List.of(
                new RouteScore("followup", "last-known", 0.74, 0.13, 0.68),
                new RouteScore("general", "general-mock", 0.62, 0.0, 0.56));
        SemanticRouter router = request -> result(
                RoutingStatus.ROUTED,
                "followup",
                "last-known",
                0.68,
                0.12,
                0.79,
                candidates,
                ReasonCode.ACCEPTED);

        RouteDecision decision = adapter(router)
                .route("所以你有什么规划", TaskType.CODING)
                .orElseThrow();

        assertThat(decision.taskType()).isEqualTo(TaskType.CODING);
        assertThat(decision.modelId()).isEqualTo("coding-mock");
        assertThat(decision.confidence()).isEqualTo(0.68);
        assertThat(decision.reason()).contains("inherited CODING");
    }

    @Test
    void closeNewSpecialistRouteOverridesPreviousContext() throws Exception {
        List<RouteScore> candidates = List.of(
                new RouteScore("followup", "last-known", 0.72, 0.0, 0.716),
                new RouteScore("daily", "general-mock", 0.715, 0.0, 0.715),
                new RouteScore("general", "general-mock", 0.62, 0.0, 0.62),
                new RouteScore("math", "math-mock", 0.59, 0.0, 0.59));
        SemanticRouter router = request -> result(
                RoutingStatus.ROUTED,
                "followup",
                "last-known",
                0.716,
                0.001,
                0.72,
                candidates,
                ReasonCode.ACCEPTED);

        RouteDecision decision = adapter(router)
                .route("好的，按这个结果安排每天阅读时间。", TaskType.MATH)
                .orElseThrow();

        assertThat(decision.taskType()).isEqualTo(TaskType.DAILY);
        assertThat(decision.modelId()).isEqualTo("general-mock");
        assertThat(decision.reason()).contains("overrode previous MATH");
    }

    @Test
    void inheritsPreviousTaskWhenGeneralAndFollowupScoresAreClose() throws Exception {
        List<RouteScore> candidates = List.of(
                new RouteScore("general", "general-mock", 0.72, 0.0, 0.68),
                new RouteScore("followup", "last-known", 0.66, 0.13, 0.60),
                new RouteScore("coding", "coding-mock", 0.34, 0.0, 0.31));
        SemanticRouter router = request -> result(
                RoutingStatus.ROUTED,
                "general",
                "general-mock",
                0.68,
                0.08,
                0.76,
                candidates,
                ReasonCode.ACCEPTED);

        RouteDecision decision = adapter(router)
                .route("精简描述", TaskType.LITERARY)
                .orElseThrow();

        assertThat(decision.taskType()).isEqualTo(TaskType.LITERARY);
        assertThat(decision.modelId()).isEqualTo("literary-mock");
        assertThat(decision.reason()).contains("followupScore=0.6");
    }

    @Test
    void keepsStandaloneGeneralQuestionWhenGeneralClearlyLeads() throws Exception {
        List<RouteScore> candidates = List.of(
                new RouteScore("general", "general-mock", 0.86, 0.16, 0.80),
                new RouteScore("followup", "last-known", 0.57, 0.0, 0.51));
        SemanticRouter router = request -> result(
                RoutingStatus.ROUTED,
                "general",
                "general-mock",
                0.80,
                0.29,
                0.91,
                candidates,
                ReasonCode.ACCEPTED);

        RouteDecision decision = adapter(router)
                .route("顺便解释一下区块链是什么", TaskType.CODING)
                .orElseThrow();

        assertThat(decision.taskType()).isEqualTo(TaskType.GENERAL);
        assertThat(decision.modelId()).isEqualTo("general-mock");
        assertThat(decision.reason()).contains("Semantic router selected GENERAL");
    }

    @Test
    void explicitNewSpecialistTaskOverridesConversationContext() throws Exception {
        List<RouteScore> candidates = List.of(
                new RouteScore("literary", "literary-mock", 0.84, 0.25, 0.78),
                new RouteScore("followup", "last-known", 0.60, 0.0, 0.54));
        SemanticRouter router = request -> result(
                RoutingStatus.ROUTED,
                "literary",
                "literary-mock",
                0.78,
                0.24,
                0.88,
                candidates,
                ReasonCode.ACCEPTED);

        RouteDecision decision = adapter(router)
                .route("把上面的代码改写成一首诗", TaskType.CODING)
                .orElseThrow();

        assertThat(decision.taskType()).isEqualTo(TaskType.LITERARY);
        assertThat(decision.modelId()).isEqualTo("literary-mock");
    }

    @Test
    void explicitContextSwitchRemovesPreviousRouteFromSemanticRequest() throws Exception {
        AtomicReference<RoutingRequest> capturedRequest = new AtomicReference<>();
        RouteScore general = new RouteScore("general", "general-mock", 0.78, 0.0, 0.78);
        SemanticRouter router = request -> {
            capturedRequest.set(request);
            return result(
                    RoutingStatus.ROUTED,
                    "general",
                    "general-mock",
                    0.78,
                    0.2,
                    0.85,
                    List.of(general),
                    ReasonCode.ACCEPTED);
        };

        RouteDecision decision = adapter(router)
                .route("不继续技术问题了，为什么人容易拖延？", TaskType.CODING)
                .orElseThrow();

        assertThat(capturedRequest.get().input()).isEqualTo("为什么人容易拖延？");
        assertThat(capturedRequest.get().lastKnownRouteId()).isNull();
        assertThat(decision.taskType()).isEqualTo(TaskType.GENERAL);
        assertThat(decision.reason()).contains("Explicit context switch detected");
    }

    private RoutingResult result(
            RoutingStatus status,
            String routeId,
            String target,
            double score,
            double margin,
            double confidence,
            List<RouteScore> candidates,
            ReasonCode reasonCode) {
        RouteTrace trace = new RouteTrace(
                "hash", "preview", "v1", "d1", "stub", reasonCode, null, null, candidates);
        return new RoutingResult(status, routeId, target, score, margin, confidence, candidates, trace);
    }

    private SemanticRouteAdapter adapter(SemanticRouter router) throws Exception {
        ModelRouteProperties properties = properties();
        return adapter(router, properties);
    }

    private SemanticRouteAdapter adapter(SemanticRouter router, ModelRouteProperties properties) throws Exception {
        return new SemanticRouteAdapter(
                Optional.of(router),
                modelRegistry(properties),
                properties,
                new RoutingTextFocusExtractor(properties));
    }

    private ModelRegistry modelRegistry(ModelRouteProperties properties) throws Exception {
        ModelRegistry registry = new ModelRegistry(properties);
        registry.afterPropertiesSet();
        return registry;
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
        router.setKeywords(Map.of(TaskType.CODING, List.of("java")));
        router.setSemanticMappings(new java.util.LinkedHashMap<>(Map.of(
                "general", mapping(TaskType.GENERAL, "general-mock"),
                "daily", mapping(TaskType.DAILY, "general-mock"),
                "literary", mapping(TaskType.LITERARY, "literary-mock"),
                "coding", mapping(TaskType.CODING, "coding-mock"),
                "math", mapping(TaskType.MATH, "math-mock"))));
        ModelRouteProperties.FocusRouting focus = new ModelRouteProperties.FocusRouting();
        focus.setEnabled(true);
        focus.setSwitchMarkers(List.of("不继续", "不再", "不要"));
        focus.setFocusMarkers(List.of("而是", "只需要"));
        focus.setTrailingConstraints(List.of("，不要"));
        focus.setAcknowledgements(List.of("好的，"));
        focus.setRequestMarkers(List.of("请"));
        focus.setIgnoredRequestPrefixes(List.of("请勿", "请不要"));
        focus.setBoundaryDelimiters(List.of("，", "："));
        router.setFocus(focus);
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
