package com.modelroute.router;

import com.dolimcom.semanticrouter.api.SemanticRouter;
import com.dolimcom.semanticrouter.model.RouteScore;
import com.dolimcom.semanticrouter.model.RoutingRequest;
import com.dolimcom.semanticrouter.model.RoutingResult;
import com.dolimcom.semanticrouter.model.RoutingStatus;
import com.modelroute.config.ModelRouteProperties;
import com.modelroute.domain.TaskType;
import com.modelroute.dto.RouteDecision;
import com.modelroute.service.ModelRegistry;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Maps the generic semantic-router result to the agent's task and model domain.
 */
@Component
public class SemanticRouteAdapter {

    private static final Logger log = LoggerFactory.getLogger(SemanticRouteAdapter.class);

    private final Optional<SemanticRouter> semanticRouter;
    private final ModelRegistry modelRegistry;
    private final ModelRouteProperties properties;
    private final RoutingTextFocusExtractor focusExtractor;

    public SemanticRouteAdapter(
            Optional<SemanticRouter> semanticRouter,
            ModelRegistry modelRegistry,
            ModelRouteProperties properties,
            RoutingTextFocusExtractor focusExtractor) {
        this.semanticRouter = semanticRouter;
        this.modelRegistry = modelRegistry;
        this.properties = properties;
        this.focusExtractor = focusExtractor;
    }

    public Optional<RouteDecision> route(String question, TaskType lastKnownTaskType) {
        return attempt(question, lastKnownTaskType).decision();
    }

    public SemanticRouteAttempt attempt(String question, TaskType lastKnownTaskType) {
        RoutingTextFocusExtractor.FocusedRoutingText focused = focusExtractor.extract(question);
        TaskType effectiveLastKnown = focused.contextSwitch() ? null : lastKnownTaskType;
        Optional<RoutingResult> evaluation = evaluateFocused(focused.text(), effectiveLastKnown);
        if (evaluation.isEmpty()) {
            return SemanticRouteAttempt.unavailable();
        }
        RoutingResult result = evaluation.get();
        Optional<RouteDecision> decision = Optional.empty();
        if (result != null) {
            Optional<RouteDecision> specialistDecision = closeSpecialistDecision(result, effectiveLastKnown);
            Optional<RouteDecision> contextualDecision = specialistDecision.isPresent()
                    ? specialistDecision
                    : contextualDecision(result, effectiveLastKnown);
            decision = contextualDecision.isPresent() ? contextualDecision : toDecision(result);
        }
        decision = decision.map(routeDecision -> withFocusDiagnostic(routeDecision, focused));
        return new SemanticRouteAttempt(decision, diagnostic(result));
    }

    private String diagnostic(RoutingResult result) {
        if (result == null) {
            return "Semantic router returned no result.";
        }
        String reasonCode = result.trace() == null || result.trace().reasonCode() == null
                ? "UNKNOWN"
                : result.trace().reasonCode().name();
        String topCandidate = result.topCandidates() == null || result.topCandidates().isEmpty()
                ? "none"
                : result.topCandidates().get(0).routeId() + ":" + round(result.topCandidates().get(0).finalScore());
        return "Semantic router did not produce a usable business route: status=" + result.status()
                + ", reason=" + reasonCode
                + ", top=" + topCandidate
                + ", margin=" + round(result.margin()) + ".";
    }

    public Optional<RoutingResult> evaluate(String question, TaskType lastKnownTaskType) {
        RoutingTextFocusExtractor.FocusedRoutingText focused = focusExtractor.extract(question);
        TaskType effectiveLastKnown = focused.contextSwitch() ? null : lastKnownTaskType;
        return evaluateFocused(focused.text(), effectiveLastKnown);
    }

    private Optional<RoutingResult> evaluateFocused(String question, TaskType lastKnownTaskType) {
        if (semanticRouter.isEmpty()) {
            return Optional.empty();
        }

        RoutingRequest request = request(question, lastKnownTaskType);
        try {
            return Optional.ofNullable(semanticRouter.get().route(request));
        } catch (RuntimeException exception) {
            log.warn("Semantic routing failed; falling back to rule routing: {}", exception.getMessage());
            return Optional.empty();
        }
    }

    private RouteDecision withFocusDiagnostic(
            RouteDecision decision,
            RoutingTextFocusExtractor.FocusedRoutingText focused) {
        if (!focused.changed()) {
            return decision;
        }
        String focusReason = focused.contextSwitch()
                ? " Explicit context switch detected; previous route inheritance was disabled."
                : " Instruction focus extraction was applied.";
        return new RouteDecision(
                decision.taskType(),
                decision.modelId(),
                decision.confidence(),
                decision.fallbackUsed(),
                decision.scores(),
                decision.reason() + focusReason);
    }

    private Optional<RouteDecision> contextualDecision(RoutingResult result, TaskType lastKnownTaskType) {
        ModelRouteProperties.ContextRouting context = properties.getRouter().getContext();
        if (!context.isEnabled() || lastKnownTaskType == null || result == null) {
            return Optional.empty();
        }

        Optional<RouteScore> followupCandidate = candidate(result, context.getFollowupRouteId());
        if (followupCandidate.isEmpty()
                || followupCandidate.get().finalScore() < context.getMinimumFollowupScore()
                || !canInheritContext(result, followupCandidate.get(), context)) {
            return Optional.empty();
        }

        Optional<ModelRouteProperties.ModelDefinition> model = configuredModel(lastKnownTaskType);
        if (model.isEmpty()) {
            return Optional.empty();
        }

        Map<TaskType, Integer> scores = semanticScores(result.topCandidates());
        scores.merge(
                lastKnownTaskType,
                (int) Math.round(followupCandidate.get().finalScore() * 100.0d),
                Math::max);
        double confidence = round(followupCandidate.get().finalScore());
        String reason = "Context-aware router inherited " + lastKnownTaskType
                + " from the previous turn; followupScore=" + round(followupCandidate.get().finalScore())
                + ", semanticRoute=" + (result.routeId() == null ? "UNRESOLVED" : result.routeId()) + ".";
        return Optional.of(new RouteDecision(
                lastKnownTaskType,
                model.get().getId(),
                confidence,
                false,
                scores,
                reason));
    }

    private Optional<RouteDecision> closeSpecialistDecision(
            RoutingResult result,
            TaskType lastKnownTaskType) {
        ModelRouteProperties.ContextRouting context = properties.getRouter().getContext();
        if (!context.isEnabled() || lastKnownTaskType == null || result == null
                || !context.getFollowupRouteId().equalsIgnoreCase(result.routeId())
                || result.topCandidates() == null) {
            return Optional.empty();
        }

        Optional<RouteScore> specialist = result.topCandidates().stream()
                .filter(candidate -> !context.getFollowupRouteId().equalsIgnoreCase(candidate.routeId()))
                .filter(candidate -> !context.getGeneralRouteId().equalsIgnoreCase(candidate.routeId()))
                .filter(candidate -> mapping(candidate.routeId())
                        .map(ModelRouteProperties.SemanticRouteMapping::getTaskType)
                        .filter(taskType -> taskType != lastKnownTaskType)
                        .isPresent())
                .max(java.util.Comparator.comparingDouble(RouteScore::finalScore));
        if (specialist.isEmpty()) {
            return Optional.empty();
        }

        double gap = result.rawScore() - specialist.get().finalScore();
        if (gap < 0.0d || gap > context.getMaximumSpecialistScoreGap()) {
            return Optional.empty();
        }

        ModelRouteProperties.SemanticRouteMapping routeMapping = mapping(specialist.get().routeId()).orElseThrow();
        Optional<ModelRouteProperties.ModelDefinition> model = configuredModel(
                specialist.get().routeId(), routeMapping.getTaskType());
        if (model.isEmpty()) {
            return Optional.empty();
        }

        String reason = "Close specialist route " + routeMapping.getTaskType()
                + " overrode previous " + lastKnownTaskType
                + "; followupScore=" + round(result.rawScore())
                + ", specialistScore=" + round(specialist.get().finalScore())
                + ", gap=" + round(gap) + ".";
        return Optional.of(new RouteDecision(
                routeMapping.getTaskType(),
                model.get().getId(),
                round(specialist.get().finalScore()),
                false,
                semanticScores(result.topCandidates()),
                reason));
    }

    private boolean canInheritContext(
            RoutingResult result,
            RouteScore followupCandidate,
            ModelRouteProperties.ContextRouting context) {
        if (context.getFollowupRouteId().equalsIgnoreCase(result.routeId())) {
            return isUsableStatus(result.status());
        }
        if (context.getGeneralRouteId().equalsIgnoreCase(result.routeId())) {
            double generalScore = candidate(result, context.getGeneralRouteId())
                    .map(RouteScore::finalScore)
                    .orElse(result.rawScore());
            return generalScore - followupCandidate.finalScore() <= context.getMaximumGeneralScoreGap();
        }
        if (result.status() != RoutingStatus.REJECTED || result.topCandidates() == null
                || result.topCandidates().isEmpty()) {
            return false;
        }

        RouteScore top = result.topCandidates().get(0);
        if (context.getFollowupRouteId().equalsIgnoreCase(top.routeId())) {
            return true;
        }
        if (!context.getGeneralRouteId().equalsIgnoreCase(top.routeId())) {
            return false;
        }
        return top.finalScore() - followupCandidate.finalScore() <= context.getMaximumGeneralScoreGap();
    }

    private Optional<RouteScore> candidate(RoutingResult result, String routeId) {
        if (result.topCandidates() == null) {
            return Optional.empty();
        }
        return result.topCandidates().stream()
                .filter(candidate -> routeId.equalsIgnoreCase(candidate.routeId()))
                .findFirst();
    }

    private RoutingRequest request(String question, TaskType lastKnownTaskType) {
        if (lastKnownTaskType == null) {
            return RoutingRequest.of(question);
        }
        Optional<String> lastKnownRouteId = routeId(lastKnownTaskType);
        if (lastKnownRouteId.isEmpty()) {
            return RoutingRequest.of(question);
        }
        return new RoutingRequest(
                question,
                lastKnownRouteId.get(),
                null,
                List.of(),
                Map.of("source", "model-route-agent"));
    }

    private Optional<RouteDecision> toDecision(RoutingResult result) {
        if (result == null || !isUsableStatus(result.status())) {
            return Optional.empty();
        }

        Optional<ModelRouteProperties.SemanticRouteMapping> mapping = mapping(result.routeId());
        if (mapping.isEmpty() || mapping.get().getTaskType() == null
                || mapping.get().getModelId() == null || mapping.get().getModelId().isBlank()) {
            log.warn("Ignoring unusable semantic route: routeId={}, target={}", result.routeId(), result.target());
            return Optional.empty();
        }
        TaskType taskType = mapping.get().getTaskType();

        ModelRouteProperties.ModelDefinition model;
        try {
            model = modelRegistry.getRequiredModel(mapping.get().getModelId());
        } catch (IllegalArgumentException exception) {
            log.warn("Semantic route mapping references an unregistered model: {}", mapping.get().getModelId());
            return Optional.empty();
        }
        if (!model.isEnabled() || !model.getSupportedTasks().contains(taskType)) {
            log.warn("Semantic route target {} does not support task {}", model.getId(), taskType);
            return Optional.empty();
        }

        String reasonCode = result.trace() == null || result.trace().reasonCode() == null
                ? "UNKNOWN"
                : result.trace().reasonCode().name();
        String reason = "Semantic router selected " + taskType
                + " with score " + round(result.rawScore())
                + ", margin " + round(result.margin())
                + " and confidence " + round(result.confidence())
                + "; status=" + result.status() + ", reason=" + reasonCode + ".";
        return Optional.of(new RouteDecision(
                taskType,
                model.getId(),
                round(result.confidence()),
                result.status() == RoutingStatus.FALLBACK,
                semanticScores(result.topCandidates()),
                reason));
    }

    private Map<TaskType, Integer> semanticScores(List<RouteScore> candidates) {
        Map<TaskType, Integer> scores = new EnumMap<>(TaskType.class);
        for (TaskType taskType : TaskType.values()) {
            scores.put(taskType, 0);
        }
        if (candidates == null) {
            return scores;
        }
        for (RouteScore candidate : candidates) {
            mapping(candidate.routeId()).map(ModelRouteProperties.SemanticRouteMapping::getTaskType)
                    .ifPresent(taskType -> scores.merge(
                            taskType,
                            (int) Math.round(candidate.finalScore() * 100.0d),
                            Math::max));
        }
        return scores;
    }

    private Optional<ModelRouteProperties.SemanticRouteMapping> mapping(String routeId) {
        if (routeId == null || routeId.isBlank()) {
            return Optional.empty();
        }
        Map<String, ModelRouteProperties.SemanticRouteMapping> mappings =
                properties.getRouter().getSemanticMappings();
        if (mappings == null) {
            return Optional.empty();
        }
        return mappings.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(routeId.trim()))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    private boolean isUsableStatus(RoutingStatus status) {
        return status == RoutingStatus.ROUTED
                || status == RoutingStatus.OVERRIDDEN
                || status == RoutingStatus.FALLBACK;
    }

    private Optional<String> routeId(TaskType taskType) {
        Map<String, ModelRouteProperties.SemanticRouteMapping> mappings =
                properties.getRouter().getSemanticMappings();
        if (mappings == null) {
            return Optional.empty();
        }
        return mappings.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().getTaskType() == taskType)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    private Optional<ModelRouteProperties.ModelDefinition> configuredModel(TaskType taskType) {
        Optional<String> configuredRouteId = routeId(taskType);
        if (configuredRouteId.isEmpty()) {
            return Optional.empty();
        }
        return configuredModel(configuredRouteId.get(), taskType);
    }

    private Optional<ModelRouteProperties.ModelDefinition> configuredModel(String routeId, TaskType taskType) {
        Optional<String> configuredModelId = mapping(routeId)
                .filter(routeMapping -> routeMapping.getTaskType() == taskType)
                .map(ModelRouteProperties.SemanticRouteMapping::getModelId);
        if (configuredModelId.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(modelRegistry.getRequiredModel(configuredModelId.get()));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0d) / 100.0d;
    }
}
