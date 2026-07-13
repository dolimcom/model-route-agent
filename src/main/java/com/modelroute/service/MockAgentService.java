package com.modelroute.service;

import com.modelroute.config.ModelRouteProperties;
import com.modelroute.dto.AgentResponse;
import com.modelroute.dto.RouteDecision;
import com.modelroute.provider.ChatMessage;
import com.modelroute.provider.ModelProviderDispatcher;
import com.modelroute.provider.ProviderResponse;
import com.modelroute.router.TaskRouter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MockAgentService {

    private final TaskRouter taskRouter;
    private final ModelRegistry modelRegistry;
    private final ModelProviderDispatcher providerDispatcher;

    public MockAgentService(
            TaskRouter taskRouter,
            ModelRegistry modelRegistry,
            ModelProviderDispatcher providerDispatcher) {
        this.taskRouter = taskRouter;
        this.modelRegistry = modelRegistry;
        this.providerDispatcher = providerDispatcher;
    }

    public AgentResponse chat(String question) {
        RouteDecision routeDecision = taskRouter.route(question);
        ModelRouteProperties.ModelDefinition model = modelRegistry.getRequiredModel(routeDecision.modelId());
        ProviderResponse providerResponse = providerDispatcher.complete(model, List.of(ChatMessage.user(question)));
        return new AgentResponse(providerResponse.content(), routeDecision);
    }
}
