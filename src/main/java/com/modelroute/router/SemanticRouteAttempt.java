package com.modelroute.router;

import com.modelroute.dto.RouteDecision;
import java.util.Optional;

public record SemanticRouteAttempt(Optional<RouteDecision> decision, String diagnostic) {

    public static SemanticRouteAttempt unavailable() {
        return new SemanticRouteAttempt(Optional.empty(), "Semantic router is unavailable.");
    }
}
