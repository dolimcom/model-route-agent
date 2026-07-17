package com.dolimcom.semanticrouter.model;

import java.util.List;
import java.util.Map;

public record RoutingRequest(
        String input,
        String lastKnownRouteId,
        String forcedRouteId,
        List<RouteHint> hints,
        Map<String, String> metadata
) {

    public RoutingRequest {
        hints = hints == null ? List.of() : List.copyOf(hints);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static RoutingRequest of(String input) {
        return new RoutingRequest(input, null, null, List.of(), Map.of());
    }
}
