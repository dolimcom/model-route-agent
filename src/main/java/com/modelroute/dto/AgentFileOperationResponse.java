package com.modelroute.dto;

public record AgentFileOperationResponse(
        String answer,
        RouteDecision route,
        String conversationId,
        FileOperationProposalResponse operation) {
}
