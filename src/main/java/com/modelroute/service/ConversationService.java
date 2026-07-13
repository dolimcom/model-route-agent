package com.modelroute.service;

import com.modelroute.dto.ConversationMessageResponse;
import com.modelroute.dto.ConversationResponse;
import com.modelroute.dto.RouteDecision;
import com.modelroute.persistence.Conversation;
import com.modelroute.persistence.ConversationMessage;
import com.modelroute.persistence.ConversationMessageRepository;
import com.modelroute.persistence.ConversationRepository;
import com.modelroute.provider.ChatMessage;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConversationService {

    private static final String DEFAULT_TITLE = "New conversation";

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository messageRepository;

    public ConversationService(
            ConversationRepository conversationRepository,
            ConversationMessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public ConversationResponse create(String requestedTitle) {
        String title = StringUtils.hasText(requestedTitle) ? requestedTitle.trim() : DEFAULT_TITLE;
        Conversation savedConversation = conversationRepository.save(new Conversation(title));
        return toConversationResponse(savedConversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> list() {
        return conversationRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::toConversationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConversationMessageResponse> listMessages(Long conversationId) {
        requireConversation(conversationId);
        return messageRepository.findAllByConversationIdOrderByCreatedAtAscIdAsc(conversationId).stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> loadProviderHistory(Long conversationId) {
        requireConversation(conversationId);
        return messageRepository.findAllByConversationIdOrderByCreatedAtAscIdAsc(conversationId).stream()
                .map(message -> new ChatMessage(message.getRole(), message.getContent()))
                .toList();
    }

    @Transactional
    public void recordUserMessage(Long conversationId, String content, RouteDecision routeDecision) {
        recordMessage(conversationId, "user", content, routeDecision);
    }

    @Transactional
    public void recordAssistantMessage(Long conversationId, String content, RouteDecision routeDecision) {
        recordMessage(conversationId, "assistant", content, routeDecision);
    }

    private void recordMessage(Long conversationId, String role, String content, RouteDecision routeDecision) {
        Conversation conversation = requireConversation(conversationId);
        messageRepository.save(new ConversationMessage(
                conversation,
                role,
                content,
                routeDecision.taskType(),
                routeDecision.modelId(),
                toRouteData(routeDecision)));
        conversation.touch();
    }

    private Conversation requireConversation(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Conversation not found: " + conversationId));
    }

    private ConversationResponse toConversationResponse(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt());
    }

    private ConversationMessageResponse toMessageResponse(ConversationMessage message) {
        return new ConversationMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getTaskType(),
                message.getModelId(),
                message.getRouteData(),
                message.getCreatedAt());
    }

    private Map<String, Object> toRouteData(RouteDecision routeDecision) {
        Map<String, Integer> scores = new LinkedHashMap<>();
        routeDecision.scores().forEach((taskType, score) -> scores.put(taskType.name(), score));

        Map<String, Object> routeData = new LinkedHashMap<>();
        routeData.put("taskType", routeDecision.taskType().name());
        routeData.put("modelId", routeDecision.modelId());
        routeData.put("confidence", routeDecision.confidence());
        routeData.put("fallbackUsed", routeDecision.fallbackUsed());
        routeData.put("scores", scores);
        routeData.put("reason", routeDecision.reason());
        return routeData;
    }
}
