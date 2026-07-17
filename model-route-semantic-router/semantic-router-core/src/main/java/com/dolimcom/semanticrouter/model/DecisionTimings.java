package com.dolimcom.semanticrouter.model;

public record DecisionTimings(
        long encodeMs,
        long searchMs,
        long policyMs,
        long totalMs
) {
}
