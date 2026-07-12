package com.modelroute.dto;

import com.modelroute.domain.TaskType;
import java.util.Map;

public record RouteDecision(
        TaskType taskType,
        String modelId,
        double confidence,
        boolean fallbackUsed,
        Map<TaskType, Integer> scores,
        String reason) {
}
