package com.dolimcom.semanticrouter.model;

public record RouteHint(
        String routeId,
        RouteHintType type,
        double weight
) {
}
