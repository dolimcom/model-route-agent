package com.modelroute.dto;

import com.modelroute.domain.FileApprovalMode;
import com.modelroute.domain.FileOperationStatus;
import com.modelroute.domain.FileOperationType;
import java.time.LocalDateTime;

public record FileOperationProposalResponse(
        String operationId,
        String conversationId,
        String rootId,
        FileOperationType operationType,
        String sourcePath,
        String targetPath,
        String proposedContent,
        String beforeContent,
        FileApprovalMode approvalMode,
        FileOperationStatus status,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime approvedAt,
        LocalDateTime executedAt,
        LocalDateTime rolledBackAt) {
}
