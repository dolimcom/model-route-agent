package com.modelroute.provider;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProviderTypeResolverTest {

    private final ProviderTypeResolver resolver = new ProviderTypeResolver();

    @Test
    void infersSupportedProviderProtocols() {
        assertThat(resolver.resolve("gpt-5.4", "https://api.openai.com/v1"))
                .isEqualTo("openai-responses");
        assertThat(resolver.resolve("gemini-2.5-pro", "https://generativelanguage.googleapis.com"))
                .isEqualTo("gemini");
        assertThat(resolver.resolve("claude-sonnet-4-5", "https://api.anthropic.com"))
                .isEqualTo("anthropic");
        assertThat(resolver.resolve("ollama/qwen3", "http://127.0.0.1:11434"))
                .isEqualTo("ollama");
        assertThat(resolver.resolve("deepseek-chat", "https://api.deepseek.com"))
                .isEqualTo("openai-compatible");
    }
}
