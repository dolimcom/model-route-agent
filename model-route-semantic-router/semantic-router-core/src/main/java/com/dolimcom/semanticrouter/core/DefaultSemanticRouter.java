package com.dolimcom.semanticrouter.core;

import com.dolimcom.semanticrouter.api.RoutingEventListener;
import com.dolimcom.semanticrouter.api.SemanticEncoder;
import com.dolimcom.semanticrouter.api.SemanticRouter;
import com.dolimcom.semanticrouter.index.RouteIndex;
import com.dolimcom.semanticrouter.model.ReasonCode;
import com.dolimcom.semanticrouter.model.RouteHintType;
import com.dolimcom.semanticrouter.model.RouteScore;
import com.dolimcom.semanticrouter.model.RouteSnapshot;
import com.dolimcom.semanticrouter.model.RouteDefinition;
import com.dolimcom.semanticrouter.model.RoutingRequest;
import com.dolimcom.semanticrouter.model.RoutingResult;
import com.dolimcom.semanticrouter.policy.RoutingPolicy;
import com.dolimcom.semanticrouter.support.HashingSupport;
import com.dolimcom.semanticrouter.support.TextSupport;

import java.util.List;
import java.util.Optional;

public class DefaultSemanticRouter implements SemanticRouter {

    private final SemanticEncoder encoder;
    private final RouteSnapshotManager snapshotManager;
    private final RouteIndex routeIndex;
    private final RoutingPolicy routingPolicy;
    private final List<RoutingEventListener> listeners;

    public DefaultSemanticRouter(
            SemanticEncoder encoder,
            RouteSnapshotManager snapshotManager,
            RouteIndex routeIndex,
            RoutingPolicy routingPolicy,
            List<RoutingEventListener> listeners
    ) {
        this.encoder = encoder;
        this.snapshotManager = snapshotManager;
        this.routeIndex = routeIndex;
        this.routingPolicy = routingPolicy;
        this.listeners = listeners == null ? List.of() : List.copyOf(listeners);
    }

    @Override
    public RoutingResult route(RoutingRequest request) {
        long totalStart = System.nanoTime();
        RouteSnapshot snapshot = snapshotManager.currentSnapshot();
        String input = request.input() == null ? "" : request.input();
        String inputHash = HashingSupport.sha256(input);
        String inputPreview = TextSupport.preview(input, 96);

        if (request.forcedRouteId() != null && snapshot.routes().containsKey(request.forcedRouteId())) {
            return publish(request, routingPolicy.override(snapshot, request, request.forcedRouteId(), ReasonCode.HARD_OVERRIDE, inputHash, inputPreview));
        }

        Optional<String> hardHint = request.hints().stream()
                .filter(hint -> hint.type() == RouteHintType.HARD)
                .map(hint -> hint.routeId())
                .filter(snapshot.routes()::containsKey)
                .findFirst();
        if (hardHint.isPresent()) {
            return publish(request, routingPolicy.override(snapshot, request, hardHint.get(), ReasonCode.HARD_OVERRIDE, inputHash, inputPreview));
        }

        Optional<String> override = routingPolicy.resolveStaticOverride(snapshot, request);
        if (override.isPresent()) {
            return publish(request, routingPolicy.override(snapshot, request, override.get(), ReasonCode.STATIC_OVERRIDE, inputHash, inputPreview));
        }

        if (input.isBlank()) {
            return publish(request, routingPolicy.handleFailure(snapshot, request, ReasonCode.EMPTY_INPUT, inputHash, inputPreview, 0L, 0L));
        }

        long encodeStart = System.nanoTime();
        double[] embedding;
        try {
            embedding = encoder.encode(input);
        } catch (RuntimeException ex) {
            return publish(request, routingPolicy.handleFailure(snapshot, request, ReasonCode.ENCODER_ERROR, inputHash, inputPreview, elapsedMs(encodeStart), 0L));
        }
        long encodeMs = elapsedMs(encodeStart);

        long searchStart = System.nanoTime();
        List<RouteScore> candidates = routeIndex.search(snapshot, input, embedding, snapshot.routes().size()).stream()
                .filter(candidate -> isEligible(snapshot.routes().get(candidate.routeId()), request))
                .limit(snapshot.policySpec().getTopCandidates())
                .toList();
        long searchMs = elapsedMs(searchStart);

        RoutingResult result = routingPolicy.decide(snapshot, request, candidates, inputHash, inputPreview, encodeMs, searchMs);
        long totalMs = elapsedMs(totalStart);
        RoutingResult normalized = new RoutingResult(
                result.status(),
                result.routeId(),
                result.target(),
                result.rawScore(),
                result.margin(),
                result.confidence(),
                result.topCandidates(),
                new com.dolimcom.semanticrouter.model.RouteTrace(
                        result.trace().inputHash(),
                        result.trace().inputPreview(),
                        result.trace().configVersion(),
                        result.trace().datasetVersion(),
                        result.trace().encoderVersion(),
                        result.trace().reasonCode(),
                        result.trace().fallbackReason(),
                        new com.dolimcom.semanticrouter.model.DecisionTimings(
                                result.trace().timings().encodeMs(),
                                result.trace().timings().searchMs(),
                                result.trace().timings().policyMs(),
                                totalMs
                        ),
                        result.trace().topCandidates()
                )
        );
        return publish(request, normalized);
    }

    private RoutingResult publish(RoutingRequest request, RoutingResult result) {
        listeners.forEach(listener -> listener.onDecision(request, result));
        return result;
    }

    private boolean isEligible(RouteDefinition route, RoutingRequest request) {
        if (route == null || route.getMetadata() == null) {
            return true;
        }
        boolean requiresContext = Boolean.parseBoolean(route.getMetadata().getOrDefault("requiresContext", "false"));
        return !requiresContext || (request.lastKnownRouteId() != null && !request.lastKnownRouteId().isBlank());
    }

    private long elapsedMs(long start) {
        return (System.nanoTime() - start) / 1_000_000L;
    }
}
