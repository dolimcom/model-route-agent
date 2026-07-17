package com.modelroute.provider;

import java.util.Locale;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ProviderTypeResolver {

    public String resolve(String modelName, String baseUrl) {
        String model = normalize(modelName);
        String url = normalize(baseUrl);
        if (model.startsWith("gemini")) {
            return "gemini";
        }
        if (model.startsWith("claude")) {
            return "anthropic";
        }
        if (url.contains("localhost:11434") || url.contains("127.0.0.1:11434")
                || model.startsWith("ollama/")) {
            return "ollama";
        }
        if ((model.startsWith("gpt-") || model.matches("o[134]-.*"))
                && url.contains("api.openai.com")) {
            return "openai-responses";
        }
        return "openai-compatible";
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : "";
    }
}
