package com.dolimcom.semanticrouter.evaluation;

import java.util.List;
import java.util.Map;

public record EvaluationReport(
        double accuracy,
        double macroF1,
        double coverage,
        double fallbackF1,
        Map<String, Map<String, Integer>> confusionMatrix,
        Map<String, Double> baselineAccuracy,
        List<Map<String, Object>> errors
) {
}
