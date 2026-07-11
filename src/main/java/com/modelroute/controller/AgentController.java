package com.modelroute.controller;

import com.modelroute.dto.AgentRequest;
import com.modelroute.dto.AgentResponse;
import com.modelroute.service.MockAgentService;
import jakarta.validation.Valid;
import java.util.Map;
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

    public AgentController(MockAgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "application", "model-route-agent");
    }

    @PostMapping("/chat")
    public ResponseEntity<AgentResponse> chat(@Valid @RequestBody AgentRequest request) {
        return ResponseEntity.ok(agentService.chat(request.question()));
    }
}
