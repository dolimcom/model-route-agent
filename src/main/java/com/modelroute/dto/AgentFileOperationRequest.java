package com.modelroute.dto;

import com.modelroute.domain.FileApprovalMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AgentFileOperationRequest(
        @NotBlank(message = "instruction must not be blank") String instruction,
        String conversationId,
        @NotBlank(message = "rootId must not be blank") String rootId,
        String selectedPath,
        @NotNull(message = "approvalMode must not be null") FileApprovalMode approvalMode) {
}
