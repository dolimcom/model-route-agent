package com.modelroute.dto;

public record AgentStreamMeta(
        String conversationId,
        RouteDecision route,
        String modelDisplayName,
        boolean fileOperation) {
}
