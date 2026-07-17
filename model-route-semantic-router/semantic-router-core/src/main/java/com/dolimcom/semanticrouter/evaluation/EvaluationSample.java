package com.dolimcom.semanticrouter.evaluation;

public record EvaluationSample(
        String input,
        String expectedRouteId,
        boolean expectedFallback
) {
}
