package com.modelroute.dto;

import com.modelroute.domain.FileApprovalMode;
import jakarta.validation.constraints.NotBlank;

public record AgentStreamRequest(
        @NotBlank(message = "question must not be blank") String question,
        String conversationId,
        String rootId,
        String selectedPath,
        String attachmentId,
        FileApprovalMode approvalMode) {
}
