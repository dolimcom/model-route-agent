package com.modelroute.router;

import com.modelroute.domain.TaskType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Immutable rule-engine output that Day 4 scoring can consume directly.
 */
public record RuleAnalysis(List<RuleSignal> signals) {

    public RuleAnalysis {
        signals = List.copyOf(signals);
    }

    public List<RuleSignal> signalsFor(TaskType taskType) {
        return signals.stream()
                .filter(signal -> signal.taskType() == taskType)
                .toList();
    }

    public Optional<TaskType> strongestTaskType() {
        return signals.stream()
                .filter(RuleSignal::strong)
                .map(RuleSignal::taskType)
                .min(Comparator.comparingInt(RuleAnalysis::priority));
    }

    public List<TaskType> keywordCandidates() {
        List<TaskType> candidates = new ArrayList<>();
        for (RuleSignal signal : signals) {
            if ("keyword".equals(signal.type()) && !candidates.contains(signal.taskType())) {
                candidates.add(signal.taskType());
            }
        }
        return List.copyOf(candidates);
    }

    public String summaryFor(TaskType taskType) {
        return signalsFor(taskType).stream()
                .map(signal -> signal.type() + ":" + signal.detail())
                .reduce((left, right) -> left + ", " + right)
                .orElse("no rule signal");
    }

    private static int priority(TaskType taskType) {
        return switch (taskType) {
            case CODING -> 0;
            case MATH -> 1;
            default -> 2;
        };
    }
}
