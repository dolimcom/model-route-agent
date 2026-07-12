package com.modelroute.service;

import com.modelroute.config.ModelRouteProperties;
import com.modelroute.domain.TaskType;
import com.modelroute.dto.AgentResponse;
import com.modelroute.dto.RouteDecision;
import com.modelroute.router.RuleAnalysis;
import com.modelroute.router.RuleEngine;
import org.springframework.stereotype.Service;

@Service
public class MockAgentService {

    private final ModelRegistry modelRegistry;
    private final RuleEngine ruleEngine;

    public MockAgentService(ModelRegistry modelRegistry, RuleEngine ruleEngine) {
        this.modelRegistry = modelRegistry;
        this.ruleEngine = ruleEngine;
    }

    public AgentResponse chat(String question) {
        RuleAnalysis analysis = ruleEngine.analyze(question);
        TaskType taskType = ruleEngine.selectTaskType(analysis);
        String modelId = findModel(taskType);
        String reason = taskType == TaskType.GENERAL
                ? "No specific task signal was found; using the fallback model."
                : "Matched " + taskType + " rule signals: " + analysis.summaryFor(taskType) + ".";

        String answer = "[Mock response] Request routed to " + modelId
                + ". Real provider integration will be added in a later iteration.";
        return new AgentResponse(answer, new RouteDecision(taskType, modelId, reason));
    }

    private String findModel(TaskType taskType) {
        return modelRegistry.findFirstSupporting(taskType)
                .map(ModelRouteProperties.ModelDefinition::getId)
                .orElseGet(() -> modelRegistry.getFallbackModel().getId());
    }
}
