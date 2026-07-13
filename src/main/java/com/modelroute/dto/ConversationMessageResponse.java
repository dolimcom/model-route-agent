package com.modelroute.dto;

import com.modelroute.domain.TaskType;
import java.time.LocalDateTime;
import java.util.Map;

public record ConversationMessageResponse(
        Long id,
        String role,
        String content,
        TaskType taskType,
        String modelId,
        Map<String, Object> route,
        LocalDateTime createdAt) {
}
