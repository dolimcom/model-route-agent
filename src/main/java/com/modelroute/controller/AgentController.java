package com.modelroute.controller;

import com.modelroute.dto.AgentRequest;
import com.modelroute.dto.AgentResponse;
import com.modelroute.dto.AgentFileOperationRequest;
import com.modelroute.dto.AgentFileOperationResponse;
import com.modelroute.config.ModelRouteProperties;
import com.modelroute.service.AgentFileOperationService;
import com.modelroute.service.ModelRegistry;
import com.modelroute.service.MockAgentService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Collection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final MockAgentService agentService;
    private final AgentFileOperationService fileOperationAgentService;
    private final ModelRegistry modelRegistry;

    public AgentController(
            MockAgentService agentService,
            AgentFileOperationService fileOperationAgentService,
            ModelRegistry modelRegistry) {
        this.agentService = agentService;
        this.fileOperationAgentService = fileOperationAgentService;
        this.modelRegistry = modelRegistry;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "application", "model-route-agent",
                "registeredModels", modelRegistry.getRegisteredModels().size());
    }

    @GetMapping("/models")
    public Collection<ModelRouteProperties.ModelDefinition> models() {
        return modelRegistry.getRegisteredModels();
    }

    @PostMapping("/chat")
    public ResponseEntity<AgentResponse> chat(@Valid @RequestBody AgentRequest request) {
        return ResponseEntity.ok(agentService.chat(request.question(), request.conversationId()));
    }

    @PostMapping("/file-operations")
    public ResponseEntity<AgentFileOperationResponse> planFileOperation(
            @Valid @RequestBody AgentFileOperationRequest request) {
        return ResponseEntity.status(201).body(fileOperationAgentService.plan(request));
    }
}
