package com.modelroute.service;

import com.modelroute.config.ModelRouteProperties;
import com.modelroute.dto.AgentResponse;
import com.modelroute.dto.RouteDecision;
import com.modelroute.provider.ChatMessage;
import com.modelroute.provider.ModelProviderDispatcher;
import com.modelroute.provider.ProviderResponse;
import com.modelroute.router.TaskRouter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MockAgentService {

    private final TaskRouter taskRouter;
    private final ModelRegistry modelRegistry;
    private final ModelProviderDispatcher providerDispatcher;
    private final ConversationService conversationService;

    public MockAgentService(
            TaskRouter taskRouter,
            ModelRegistry modelRegistry,
            ModelProviderDispatcher providerDispatcher,
            ConversationService conversationService) {
        this.taskRouter = taskRouter;
        this.modelRegistry = modelRegistry;
        this.providerDispatcher = providerDispatcher;
        this.conversationService = conversationService;
    }

    public AgentResponse chat(String question) {
        return chat(question, null);
    }

    public AgentResponse chat(String question, Long conversationId) {
        List<ChatMessage> messages = conversationId == null
                ? new ArrayList<>()
                : new ArrayList<>(conversationService.loadProviderHistory(conversationId));
        RouteDecision routeDecision = taskRouter.route(question);
        ModelRouteProperties.ModelDefinition model = modelRegistry.getRequiredModel(routeDecision.modelId());
        messages.add(ChatMessage.user(question));

        if (conversationId != null) {
            conversationService.recordUserMessage(conversationId, question, routeDecision);
        }

        ProviderResponse providerResponse = providerDispatcher.complete(model, messages);

        if (conversationId != null) {
            conversationService.recordAssistantMessage(conversationId, providerResponse.content(), routeDecision);
        }

        return new AgentResponse(providerResponse.content(), routeDecision, conversationId);
    }
}
