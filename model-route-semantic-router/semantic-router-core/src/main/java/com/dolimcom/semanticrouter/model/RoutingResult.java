package com.dolimcom.semanticrouter.model;

import java.util.List;

public record RoutingResult(
        RoutingStatus status,
        String routeId,
        String target,
        double rawScore,
        double margin,
        double confidence,
        List<RouteScore> topCandidates,
        RouteTrace trace
) {
}
