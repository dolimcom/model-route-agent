package com.modelroute.controller;

import com.modelroute.dto.ConversationMessageResponse;
import com.modelroute.dto.ConversationResponse;
import com.modelroute.dto.CreateConversationRequest;
import com.modelroute.service.ConversationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ResponseEntity<ConversationResponse> create(
            @Valid @RequestBody(required = false) CreateConversationRequest request) {
        String title = request == null ? null : request.title();
        return ResponseEntity.status(HttpStatus.CREATED).body(conversationService.create(title));
    }

    @GetMapping
    public List<ConversationResponse> list() {
        return conversationService.list();
    }

    @GetMapping("/{conversationId}/messages")
    public List<ConversationMessageResponse> listMessages(@PathVariable String conversationId) {
        return conversationService.listMessages(conversationId);
    }
}
