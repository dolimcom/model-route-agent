package com.modelroute.dto;

import com.modelroute.domain.TaskType;

public record TaskModelConfigResponse(
        TaskType taskType,
        String id,
        String displayName,
        String provider,
        String baseUrl,
        boolean apiKeyConfigured,
        String modelName,
        int timeoutMs,
        int maxTokens,
        double temperature) {
}
