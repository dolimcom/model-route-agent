package com.modelroute.dto;

import com.modelroute.domain.TaskType;

public record RouteDecision(TaskType taskType, String modelId, String reason) {
}
