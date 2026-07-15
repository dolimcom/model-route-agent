package com.modelroute.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modelroute.dto.FileOperationPlan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Component
public class FileOperationPlanParser {

    private final ObjectMapper objectMapper;

    public FileOperationPlanParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public FileOperationPlan parse(String modelOutput) {
        if (!StringUtils.hasText(modelOutput)) {
            throw invalidPlan("Model returned an empty file operation plan", null);
        }

        String json = extractJsonObject(modelOutput);
        try {
            FileOperationPlan plan = objectMapper.readValue(json, FileOperationPlan.class);
            if (plan.operationType() == null) {
                throw invalidPlan("Model plan did not contain operationType", null);
            }
            if (!StringUtils.hasText(plan.summary())) {
                throw invalidPlan("Model plan did not contain summary", null);
            }
            return plan;
        } catch (JsonProcessingException exception) {
            throw invalidPlan("Model did not return a valid file operation JSON object", exception);
        }
    }

    private String extractJsonObject(String modelOutput) {
        String value = modelOutput.trim();
        int start = value.indexOf('{');
        int end = value.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw invalidPlan("Model response did not contain a JSON object", null);
        }
        return value.substring(start, end + 1);
    }

    private ResponseStatusException invalidPlan(String message, Exception cause) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, message, cause);
    }
}
