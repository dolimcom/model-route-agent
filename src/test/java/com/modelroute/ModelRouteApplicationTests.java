package com.modelroute;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ModelRouteApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/api/agent/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void chatEndpointRoutesCodingQuestion() throws Exception {
        mockMvc.perform(post("/api/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"请帮我分析这段 Java 代码的 bug\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route.taskType").value("CODING"))
                .andExpect(jsonPath("$.route.modelId").value("coding-mock"))
                .andExpect(jsonPath("$.route.fallbackUsed").value(false));
    }

    @Test
    void chatEndpointRoutesMathExpressionWithoutConfiguredKeyword() throws Exception {
        mockMvc.perform(post("/api/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Solve x^2 + 2x = 0\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route.taskType").value("MATH"))
                .andExpect(jsonPath("$.route.modelId").value("math-mock"))
                .andExpect(jsonPath("$.route.confidence").value(0.8));
    }

    @Test
    void chatEndpointFallsBackForAmbiguousRequest() throws Exception {
        mockMvc.perform(post("/api/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"请帮我润色明天的学习计划\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route.taskType").value("GENERAL"))
                .andExpect(jsonPath("$.route.modelId").value("general-mock"))
                .andExpect(jsonPath("$.route.fallbackUsed").value(true));
    }

    @Test
    void modelsEndpointReturnsConfiguredRegistry() throws Exception {
        mockMvc.perform(get("/api/agent/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("general-mock"))
                .andExpect(jsonPath("$.length()").value(4));
    }
}
