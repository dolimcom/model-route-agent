package com.modelroute.provider;

/**
 * Provider-neutral chat message. Later conversation storage will supply the full history.
 */
public record ChatMessage(String role, String content) {

    public static ChatMessage user(String content) {
        return new ChatMessage("user", content);
    }
}
