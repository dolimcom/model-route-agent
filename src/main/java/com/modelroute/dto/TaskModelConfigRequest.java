package com.modelroute.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TaskModelConfigRequest(
        String displayName,
        @NotBlank(message = "modelName must not be blank") String modelName,
        @NotBlank(message = "baseUrl must not be blank") String baseUrl,
        String apiKey,
        @Min(value = 1000, message = "timeoutMs must be at least 1000")
        @Max(value = 300000, message = "timeoutMs must not exceed 300000")
        int timeoutMs,
        @Min(value = 1, message = "maxTokens must be positive")
        @Max(value = 131072, message = "maxTokens is too large")
        int maxTokens,
        @DecimalMin(value = "0.0", message = "temperature must be at least 0")
        @DecimalMax(value = "2.0", message = "temperature must not exceed 2")
        double temperature) {
}
