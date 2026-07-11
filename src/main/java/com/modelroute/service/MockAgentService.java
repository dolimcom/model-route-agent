package com.modelroute.service;

import com.modelroute.config.ModelRouteProperties;
import com.modelroute.domain.TaskType;
import com.modelroute.dto.AgentResponse;
import com.modelroute.dto.RouteDecision;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MockAgentService {

    private final ModelRouteProperties properties;

    public MockAgentService(ModelRouteProperties properties) {
        this.properties = properties;
    }

    public AgentResponse chat(String question) {
        TaskType taskType = detectTaskType(question);
        String modelId = findModel(taskType);
        String reason = taskType == TaskType.GENERAL
                ? "No specific task signal was found; using the fallback model."
                : "Matched configured " + taskType + " keywords.";

        String answer = "[Mock response] Request routed to " + modelId
                + ". Real provider integration will be added in a later iteration.";
        return new AgentResponse(answer, new RouteDecision(taskType, modelId, reason));
    }

    private TaskType detectTaskType(String question) {
        String normalizedQuestion = question.toLowerCase(Locale.ROOT);
        for (Map.Entry<TaskType, List<String>> entry : properties.getRouter().getKeywords().entrySet()) {
            boolean matched = entry.getValue().stream()
                    .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                    .anyMatch(normalizedQuestion::contains);
            if (matched) {
                return entry.getKey();
            }
        }
        return TaskType.GENERAL;
    }

    private String findModel(TaskType taskType) {
        return properties.getModels().stream()
                .filter(model -> model.getSupportedTasks().contains(taskType))
                .map(ModelRouteProperties.ModelDefinition::getId)
                .findFirst()
                .orElse(properties.getRouter().getFallbackModelId());
    }
}
