package com.dolimcom.semanticrouter.model;

import java.util.List;

public record RouteTrace(
        String inputHash,
        String inputPreview,
        String configVersion,
        String datasetVersion,
        String encoderVersion,
        ReasonCode reasonCode,
        String fallbackReason,
        DecisionTimings timings,
        List<RouteScore> topCandidates
) {
}
