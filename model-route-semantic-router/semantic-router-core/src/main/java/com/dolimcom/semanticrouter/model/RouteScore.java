package com.dolimcom.semanticrouter.model;

public record RouteScore(
        String routeId,
        String target,
        double semanticScore,
        double keywordScore,
        double finalScore
) {
}
