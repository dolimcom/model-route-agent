package com.dolimcom.semanticrouter.policy;

import com.dolimcom.semanticrouter.model.FallbackMode;
import com.dolimcom.semanticrouter.model.OverrideMatchMode;
import com.dolimcom.semanticrouter.model.OverrideRule;
import com.dolimcom.semanticrouter.model.ReasonCode;
import com.dolimcom.semanticrouter.model.RouteHint;
import com.dolimcom.semanticrouter.model.RouteHintType;
import com.dolimcom.semanticrouter.model.RouteScore;
import com.dolimcom.semanticrouter.model.RouteSnapshot;
import com.dolimcom.semanticrouter.model.RouteTrace;
import com.dolimcom.semanticrouter.model.RoutingRequest;
import com.dolimcom.semanticrouter.model.RoutingResult;
import com.dolimcom.semanticrouter.model.RoutingStatus;
import com.dolimcom.semanticrouter.support.TextSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class ConfigurableRoutingPolicy implements RoutingPolicy {

    private final ConfidenceCalibrator confidenceCalibrator;

    public ConfigurableRoutingPolicy(ConfidenceCalibrator confidenceCalibrator) {
        this.confidenceCalibrator = confidenceCalibrator;
    }

    @Override
    public Optional<String> resolveStaticOverride(RouteSnapshot snapshot, RoutingRequest request) {
        String input = TextSupport.normalize(request.input());
        return snapshot.policySpec().getOverrides().stream()
                .filter(rule -> matches(rule, input))
                .map(OverrideRule::routeId)
                .findFirst();
    }

    @Override
    public RoutingResult decide(RouteSnapshot snapshot, RoutingRequest request, List<RouteScore> candidates, String inputHash, String inputPreview, long encodeMs, long searchMs) {
        long policyStart = System.nanoTime();
        if (candidates.isEmpty()) {
            return fallback(snapshot, request, ReasonCode.NO_CANDIDATES, candidates, inputHash, inputPreview, encodeMs, searchMs, policyStart);
        }

        List<RouteScore> adjustedCandidates = applySoftHints(snapshot, request, candidates);
        RouteScore top = adjustedCandidates.get(0);
        RouteScore second = adjustedCandidates.size() > 1 ? adjustedCandidates.get(1) : null;
        double margin = second == null ? top.finalScore() : top.finalScore() - second.finalScore();

        ReasonCode reasonCode = determineReason(snapshot, top.finalScore(), margin, second);
        if (reasonCode == ReasonCode.ACCEPTED) {
            return buildResult(snapshot, top, adjustedCandidates, RoutingStatus.ROUTED, reasonCode, null, inputHash, inputPreview, encodeMs, searchMs, policyStart);
        }
        return fallback(snapshot, request, reasonCode, adjustedCandidates, inputHash, inputPreview, encodeMs, searchMs, policyStart);
    }

    @Override
    public RoutingResult handleFailure(RouteSnapshot snapshot, RoutingRequest request, ReasonCode reasonCode, String inputHash, String inputPreview, long encodeMs, long searchMs) {
        long policyStart = System.nanoTime();
        return fallback(snapshot, request, reasonCode, List.of(), inputHash, inputPreview, encodeMs, searchMs, policyStart);
    }

    @Override
    public RoutingResult override(RouteSnapshot snapshot, RoutingRequest request, String routeId, ReasonCode reasonCode, String inputHash, String inputPreview) {
        long now = System.nanoTime();
        RouteScore overrideScore = new RouteScore(routeId, snapshot.routes().get(routeId).getTarget(), 1.0d, 1.0d, 1.0d);
        return buildResult(snapshot, overrideScore, List.of(overrideScore), RoutingStatus.OVERRIDDEN, reasonCode, null, inputHash, inputPreview, 0L, 0L, now);
    }

    @Override
    public RoutingStatus statusForReason(ReasonCode reasonCode) {
        return switch (reasonCode) {
            case HARD_OVERRIDE, STATIC_OVERRIDE -> RoutingStatus.OVERRIDDEN;
            case ACCEPTED -> RoutingStatus.ROUTED;
            case LAST_KNOWN_GOOD, DEFAULT_ROUTE -> RoutingStatus.FALLBACK;
            default -> RoutingStatus.REJECTED;
        };
    }

    private RoutingResult fallback(RouteSnapshot snapshot, RoutingRequest request, ReasonCode reasonCode, List<RouteScore> candidates, String inputHash, String inputPreview, long encodeMs, long searchMs, long policyStart) {
        FallbackMode mode = fallbackMode(snapshot, reasonCode);
        if (mode == FallbackMode.LAST_KNOWN_GOOD && request.lastKnownRouteId() != null && snapshot.routes().containsKey(request.lastKnownRouteId())) {
            RouteScore routeScore = new RouteScore(request.lastKnownRouteId(), snapshot.routes().get(request.lastKnownRouteId()).getTarget(), 0.0d, 0.0d, 0.0d);
            return buildResult(snapshot, routeScore, candidates, RoutingStatus.FALLBACK, ReasonCode.LAST_KNOWN_GOOD, reasonCode.name(), inputHash, inputPreview, encodeMs, searchMs, policyStart);
        }
        if (mode == FallbackMode.DEFAULT_ROUTE && snapshot.policySpec().getFallback().getDefaultRouteId() != null) {
            String defaultRouteId = snapshot.policySpec().getFallback().getDefaultRouteId();
            if (snapshot.routes().containsKey(defaultRouteId)) {
                RouteScore routeScore = new RouteScore(defaultRouteId, snapshot.routes().get(defaultRouteId).getTarget(), 0.0d, 0.0d, 0.0d);
                return buildResult(snapshot, routeScore, candidates, RoutingStatus.FALLBACK, ReasonCode.DEFAULT_ROUTE, reasonCode.name(), inputHash, inputPreview, encodeMs, searchMs, policyStart);
            }
        }
        RouteTrace trace = new RouteTrace(
                inputHash,
                inputPreview,
                snapshot.configVersion(),
                snapshot.datasetVersion(),
                snapshot.encoderVersion(),
                reasonCode,
                reasonCode.name(),
                new com.dolimcom.semanticrouter.model.DecisionTimings(encodeMs, searchMs, elapsedMs(policyStart), encodeMs + searchMs + elapsedMs(policyStart)),
                candidates.stream().limit(snapshot.policySpec().getTopCandidates()).toList()
        );
        return new RoutingResult(RoutingStatus.REJECTED, null, null, candidates.isEmpty() ? 0.0d : candidates.get(0).finalScore(), candidates.size() > 1 ? candidates.get(0).finalScore() - candidates.get(1).finalScore() : 0.0d, 0.0d, candidates, trace);
    }

    private RoutingResult buildResult(RouteSnapshot snapshot, RouteScore selected, List<RouteScore> candidates, RoutingStatus status, ReasonCode reasonCode, String fallbackReason, String inputHash, String inputPreview, long encodeMs, long searchMs, long policyStart) {
        double margin = candidates.size() > 1 ? selected.finalScore() - candidates.get(1).finalScore() : selected.finalScore();
        double confidence = confidenceCalibrator.calibrate(selected.finalScore(), margin);
        RouteTrace trace = new RouteTrace(
                inputHash,
                inputPreview,
                snapshot.configVersion(),
                snapshot.datasetVersion(),
                snapshot.encoderVersion(),
                reasonCode,
                fallbackReason,
                new com.dolimcom.semanticrouter.model.DecisionTimings(encodeMs, searchMs, elapsedMs(policyStart), encodeMs + searchMs + elapsedMs(policyStart)),
                candidates.stream().limit(snapshot.policySpec().getTopCandidates()).toList()
        );
        return new RoutingResult(status, selected.routeId(), selected.target(), selected.finalScore(), margin, confidence, candidates, trace);
    }

    private long elapsedMs(long start) {
        return (System.nanoTime() - start) / 1_000_000L;
    }

    private ReasonCode determineReason(RouteSnapshot snapshot, double score, double margin, RouteScore second) {
        if (score < snapshot.policySpec().getOutOfDomainThreshold()) {
            return ReasonCode.OUT_OF_DOMAIN;
        }
        if (score < snapshot.policySpec().getMinScore()) {
            return ReasonCode.LOW_CONFIDENCE;
        }
        if (second != null && Math.abs(margin) <= snapshot.policySpec().getTieTolerance()) {
            return ReasonCode.TIE;
        }
        if (second != null && margin < snapshot.policySpec().getMinMargin()) {
            return ReasonCode.AMBIGUOUS;
        }
        return ReasonCode.ACCEPTED;
    }

    private FallbackMode fallbackMode(RouteSnapshot snapshot, ReasonCode reasonCode) {
        return switch (reasonCode) {
            case EMPTY_INPUT -> snapshot.policySpec().getFallback().getEmptyInput();
            case LOW_CONFIDENCE -> snapshot.policySpec().getFallback().getLowConfidence();
            case AMBIGUOUS -> snapshot.policySpec().getFallback().getAmbiguous();
            case OUT_OF_DOMAIN, NO_CANDIDATES -> snapshot.policySpec().getFallback().getOutOfDomain();
            case ENCODER_ERROR -> snapshot.policySpec().getFallback().getEncoderError();
            case TIE -> snapshot.policySpec().getFallback().getTie();
            default -> FallbackMode.REJECT;
        };
    }

    private List<RouteScore> applySoftHints(RouteSnapshot snapshot, RoutingRequest request, List<RouteScore> candidates) {
        List<RouteHint> softHints = request.hints().stream().filter(hint -> hint.type() == RouteHintType.SOFT).toList();
        if (softHints.isEmpty()) {
            return candidates;
        }
        List<RouteScore> adjusted = new ArrayList<>();
        for (RouteScore candidate : candidates) {
            double bonus = softHints.stream()
                    .filter(hint -> candidate.routeId().equals(hint.routeId()))
                    .mapToDouble(hint -> hint.weight() > 0.0d ? hint.weight() : snapshot.policySpec().getSoftHintBoost())
                    .sum();
            double finalScore = Math.min(1.0d, candidate.finalScore() + bonus);
            adjusted.add(new RouteScore(candidate.routeId(), candidate.target(), candidate.semanticScore(), candidate.keywordScore(), finalScore));
        }
        adjusted.sort(java.util.Comparator.comparingDouble(RouteScore::finalScore).reversed().thenComparing(RouteScore::routeId));
        return adjusted;
    }

    private boolean matches(OverrideRule rule, String input) {
        if (rule == null || rule.pattern() == null || rule.routeId() == null) {
            return false;
        }
        String pattern = TextSupport.normalize(rule.pattern());
        OverrideMatchMode mode = rule.matchMode() == null ? OverrideMatchMode.CONTAINS : rule.matchMode();
        return switch (mode) {
            case EXACT -> input.equals(pattern);
            case CONTAINS -> input.contains(pattern);
            case REGEX -> Pattern.compile(rule.pattern(), Pattern.CASE_INSENSITIVE).matcher(input).find();
        };
    }
}
