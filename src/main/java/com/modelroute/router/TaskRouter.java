package com.modelroute.router;

import com.modelroute.config.ModelRouteProperties;
import com.modelroute.domain.TaskType;
import com.modelroute.dto.RouteDecision;
import com.modelroute.service.ModelRegistry;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Selects a model from scored rule signals and applies low-confidence fallback policy.
 */
@Service
public class TaskRouter {

    private final RuleEngine ruleEngine;
    private final ScoreCalculator scoreCalculator;
    private final ModelRegistry modelRegistry;
    private final ModelRouteProperties properties;

    public TaskRouter(
            RuleEngine ruleEngine,
            ScoreCalculator scoreCalculator,
            ModelRegistry modelRegistry,
            ModelRouteProperties properties) {
        this.ruleEngine = ruleEngine;
        this.scoreCalculator = scoreCalculator;
        this.modelRegistry = modelRegistry;
        this.properties = properties;
    }

    public RouteDecision route(String question) {
        RuleAnalysis analysis = ruleEngine.analyze(question);
        Map<TaskType, Integer> scores = scoreCalculator.calculate(analysis);
        List<Map.Entry<TaskType, Integer>> rankedScores = scores.entrySet().stream()
                .sorted(Map.Entry.<TaskType, Integer>comparingByValue(Comparator.reverseOrder()))
                .toList();

        int topScore = rankedScores.isEmpty() ? 0 : rankedScores.get(0).getValue();
        int secondScore = rankedScores.size() < 2 ? 0 : rankedScores.get(1).getValue();
        double confidence = confidence(topScore, secondScore);
        ModelRouteProperties.Scoring scoring = properties.getRouter().getScoring();

        if (topScore == 0) {
            return fallback(scores, confidence, "No routing signals were found.");
        }
        if (topScore < scoring.getMinimumScore()) {
            return fallback(
                    scores,
                    confidence,
                    "Highest routing score " + topScore + " is below the configured minimum score "
                            + scoring.getMinimumScore() + ".");
        }
        if (topScore - secondScore < scoring.getMinimumScoreGap()) {
            return fallback(
                    scores,
                    confidence,
                    "Top routing scores are too close: " + rankedScores.get(0).getKey() + "=" + topScore
                            + ", " + rankedScores.get(1).getKey() + "=" + secondScore + ".");
        }

        TaskType selectedTaskType = rankedScores.get(0).getKey();
        String modelId = modelRegistry.findFirstSupporting(selectedTaskType)
                .orElseGet(modelRegistry::getFallbackModel)
                .getId();
        String reason = "Selected " + selectedTaskType + " with score " + topScore + " and confidence "
                + confidence + "; matched signals: " + analysis.summaryFor(selectedTaskType) + ".";
        return new RouteDecision(selectedTaskType, modelId, confidence, false, scores, reason);
    }

    private RouteDecision fallback(Map<TaskType, Integer> scores, double confidence, String reason) {
        return new RouteDecision(
                TaskType.GENERAL,
                modelRegistry.getFallbackModel().getId(),
                confidence,
                true,
                scores,
                reason + " Using fallback model.");
    }

    private double confidence(int topScore, int secondScore) {
        if (topScore == 0) {
            return 0.0;
        }
        return Math.round((topScore / (double) (topScore + secondScore + 1)) * 100.0) / 100.0;
    }
}
