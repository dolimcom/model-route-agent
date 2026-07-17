package com.modelroute.controller;

import com.dolimcom.semanticrouter.model.RoutingResult;
import com.modelroute.dto.AgentRequest;
import com.modelroute.dto.AgentResponse;
import com.modelroute.dto.AgentFileOperationRequest;
import com.modelroute.dto.AgentFileOperationResponse;
import com.modelroute.dto.AgentStreamRequest;
import com.modelroute.dto.RouteDecision;
import com.modelroute.config.ModelRouteProperties;
import com.modelroute.domain.TaskType;
import com.modelroute.service.AgentFileOperationService;
import com.modelroute.service.AgentStreamService;
import com.modelroute.service.ConversationService;
import com.modelroute.service.ModelRegistry;
import com.modelroute.service.AgentService;
import com.modelroute.router.TaskRouter;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Collection;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;
    private final AgentFileOperationService fileOperationAgentService;
    private final ModelRegistry modelRegistry;
    private final TaskRouter taskRouter;
    private final ConversationService conversationService;
    private final AgentStreamService streamService;

    public AgentController(
            AgentService agentService,
            AgentFileOperationService fileOperationAgentService,
            ModelRegistry modelRegistry,
            TaskRouter taskRouter,
            ConversationService conversationService,
            AgentStreamService streamService) {
        this.agentService = agentService;
        this.fileOperationAgentService = fileOperationAgentService;
        this.modelRegistry = modelRegistry;
        this.taskRouter = taskRouter;
        this.conversationService = conversationService;
        this.streamService = streamService;
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

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> stream(@Valid @RequestBody AgentStreamRequest request) {
        return streamService.stream(request);
    }

    @PostMapping("/route")
    public ResponseEntity<RouteDecision> route(@Valid @RequestBody AgentRequest request) {
        TaskType lastKnownTaskType = request.conversationId() == null
                ? null
                : conversationService.findLastTaskType(request.conversationId()).orElse(null);
        return ResponseEntity.ok(taskRouter.route(request.question(), lastKnownTaskType));
    }

    @PostMapping("/route/semantic")
    public ResponseEntity<RoutingResult> semanticRoute(@Valid @RequestBody AgentRequest request) {
        TaskType lastKnownTaskType = request.conversationId() == null
                ? null
                : conversationService.findLastTaskType(request.conversationId()).orElse(null);
        return ResponseEntity.ok(taskRouter.semanticRoute(request.question(), lastKnownTaskType)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE, "Semantic router is unavailable")));
    }

    @PostMapping("/file-operations")
    public ResponseEntity<AgentFileOperationResponse> planFileOperation(
            @Valid @RequestBody AgentFileOperationRequest request) {
        return ResponseEntity.status(201).body(fileOperationAgentService.plan(request));
    }
}
