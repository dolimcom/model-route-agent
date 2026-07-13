package com.modelroute.dto;

public record AgentResponse(String answer, RouteDecision route, Long conversationId) {

    public AgentResponse(String answer, RouteDecision route) {
        this(answer, route, null);
    }
}
