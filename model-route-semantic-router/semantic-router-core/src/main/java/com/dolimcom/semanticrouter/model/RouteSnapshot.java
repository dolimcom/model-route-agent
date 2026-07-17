package com.dolimcom.semanticrouter.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record RouteSnapshot(
        String configVersion,
        String datasetVersion,
        String encoderVersion,
        Instant loadedAt,
        RoutingPolicySpec policySpec,
        Map<String, RouteDefinition> routes,
        Map<String, List<double[]>> exampleEmbeddings,
        Map<String, double[]> centroids
) {
}
