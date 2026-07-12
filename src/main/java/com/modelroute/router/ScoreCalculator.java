package com.modelroute.router;

import com.modelroute.config.ModelRouteProperties;
import com.modelroute.domain.TaskType;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Converts explainable rule signals into configurable task scores.
 */
@Service
public class ScoreCalculator {

    private final ModelRouteProperties properties;

    public ScoreCalculator(ModelRouteProperties properties) {
        this.properties = properties;
    }

    public Map<TaskType, Integer> calculate(RuleAnalysis analysis) {
        Map<TaskType, Integer> scores = new EnumMap<>(TaskType.class);
        for (TaskType taskType : TaskType.values()) {
            if (taskType != TaskType.GENERAL) {
                scores.put(taskType, 0);
            }
        }

        for (RuleSignal signal : analysis.signals()) {
            scores.merge(signal.taskType(), weightFor(signal), Integer::sum);
        }
        return Map.copyOf(scores);
    }

    private int weightFor(RuleSignal signal) {
        ModelRouteProperties.Scoring scoring = properties.getRouter().getScoring();
        if (signal.strong()) {
            return scoring.getStrongSignalWeight();
        }
        if ("code-syntax".equals(signal.type())) {
            return scoring.getCodeSyntaxWeight();
        }
        return scoring.getKeywordWeight();
    }
}
