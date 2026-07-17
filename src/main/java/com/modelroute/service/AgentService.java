package com.modelroute.service;

import com.modelroute.config.ModelRouteProperties;
import com.modelroute.domain.TaskType;
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
public class AgentService {

    private final TaskRouter taskRouter;
    private final ModelRegistry modelRegistry;
    private final ModelProviderDispatcher providerDispatcher;
    private final ConversationService conversationService;

    public AgentService(
            TaskRouter taskRouter,
            ModelRegistry modelRegistry,
            ModelProviderDispatcher providerDispatcher,
            ConversationService conversationService) {
        this.taskRouter = taskRouter;
        this.modelRegistry = modelRegistry;
        this.providerDispatcher = providerDispatcher;
        this.conversationService = conversationService;
    }

    public AgentResponse chat(String question, String conversationId) {
        List<ChatMessage> messages = conversationId == null
                ? new ArrayList<>()
                : new ArrayList<>(conversationService.loadProviderHistory(conversationId));
        TaskType lastKnownTaskType = conversationId == null
                ? null
                : conversationService.findLastTaskType(conversationId).orElse(null);
        RouteDecision routeDecision = taskRouter.route(question, lastKnownTaskType);
        ModelRouteProperties.ModelDefinition model = modelRegistry.getRequiredModel(routeDecision.modelId());
        messages.add(ChatMessage.user(question));

        ProviderResponse providerResponse = providerDispatcher.complete(model, messages);
        if (conversationId != null) {
            conversationService.recordExchange(
                    conversationId, question, providerResponse.content(), routeDecision);
        }
        return new AgentResponse(providerResponse.content(), routeDecision, conversationId);
    }
}
