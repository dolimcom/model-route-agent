package com.modelroute.dto;

import java.time.Instant;

public record FileOperationResponse(
        String operation,
        String rootId,
        String path,
        String previousPath,
        Instant completedAt) {
}
