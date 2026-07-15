package com.modelroute.dto;

import com.modelroute.domain.FileApprovalMode;
import com.modelroute.domain.FileOperationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FileOperationProposalRequest(
        String conversationId,
        @NotBlank(message = "rootId must not be blank") String rootId,
        @NotNull(message = "operationType must not be null") FileOperationType operationType,
        String sourcePath,
        String targetPath,
        String content,
        @NotNull(message = "approvalMode must not be null") FileApprovalMode approvalMode) {
}
