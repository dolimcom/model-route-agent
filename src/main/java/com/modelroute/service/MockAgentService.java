package com.modelroute.service;

import com.modelroute.dto.AgentResponse;
import com.modelroute.dto.RouteDecision;
import com.modelroute.router.TaskRouter;
import org.springframework.stereotype.Service;

@Service
public class MockAgentService {

    private final TaskRouter taskRouter;

    public MockAgentService(TaskRouter taskRouter) {
        this.taskRouter = taskRouter;
    }

    public AgentResponse chat(String question) {
        RouteDecision routeDecision = taskRouter.route(question);
        String answer = "[Mock response] Request routed to " + routeDecision.modelId()
                + ". Real provider integration will be added in a later iteration.";
        return new AgentResponse(answer, routeDecision);
    }
}
