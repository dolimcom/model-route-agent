package com.modelroute.dto;

import java.time.LocalDateTime;

public record ConversationResponse(
        String id,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
