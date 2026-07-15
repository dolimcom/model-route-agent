package com.modelroute.dto;

import com.modelroute.domain.FileOperationType;

public record FileOperationPlan(
        FileOperationType operationType,
        String sourcePath,
        String targetPath,
        String content,
        String summary) {
}
