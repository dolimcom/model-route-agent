package com.modelroute;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.assertj.core.api.Assertions.assertThat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.modelroute.provider.ModelProviderDispatcher;
import com.modelroute.provider.ModelProviderException;
import com.modelroute.provider.ProviderResponse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
class ModelRouteApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ModelProviderDispatcher providerDispatcher;

    @BeforeEach
    void stubProviderDispatcher() {
        given(providerDispatcher.complete(any(), any()))
                .willReturn(new ProviderResponse("[Test provider response]"));
    }

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
                .andExpect(jsonPath("$[?(@.id == 'general-mock')]").isNotEmpty())
                .andExpect(content().string(not(containsString("\"apiKey\""))));
    }

    @Test
    void providerFailureReturnsReadableBadGatewayResponse() throws Exception {
        given(providerDispatcher.complete(any(), any()))
                .willThrow(new ModelProviderException("Provider denied model access"));

        mockMvc.perform(post("/api/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"请分析 Java 代码\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Provider denied model access"))
                .andExpect(jsonPath("$.path").value("/api/agent/chat"));
    }

    @Test
    void agentFileOperationCreatesPendingProposalFromModelPlan() throws Exception {
        String createResponse = mockMvc.perform(post("/api/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Day 10 agent test\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String conversationId = objectMapper.readTree(createResponse).get("id").asText();

        given(providerDispatcher.complete(any(), any())).willReturn(new ProviderResponse("""
                {
                  "operationType": "CREATE_FILE",
                  "sourcePath": null,
                  "targetPath": "nested/day10-test-proposal.txt",
                  "content": "Generated by the agent planner.",
                  "summary": "Create a test file after approval."
                }
                """));

        mockMvc.perform(post("/api/agent/file-operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "instruction": "Create a test file in the nested directory",
                                  "conversationId": "%s",
                                  "rootId": "agent-workspace",
                                  "approvalMode": "MANUAL"
                                }
                                """.formatted(conversationId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.answer").value("Create a test file after approval."))
                .andExpect(jsonPath("$.conversationId").value(conversationId))
                .andExpect(jsonPath("$.operation.operationType").value("CREATE_FILE"))
                .andExpect(jsonPath("$.operation.conversationId").value(conversationId))
                .andExpect(jsonPath("$.operation.targetPath").value("nested/day10-test-proposal.txt"))
                .andExpect(jsonPath("$.operation.status").value("PENDING"));

        mockMvc.perform(get("/api/conversations/{conversationId}/messages", conversationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].role").value("user"))
                .andExpect(jsonPath("$[1].role").value("assistant"));
    }

    @Test
    void persistsMessagesAndReusesConversationHistory() throws Exception {
        String createResponse = mockMvc.perform(post("/api/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Java debugging\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Java debugging"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode createdConversation = objectMapper.readTree(createResponse);
        String conversationId = createdConversation.get("id").asText();
        assertThat(conversationId).matches("\\d{8}-\\d{9}-\\d{10}");

        mockMvc.perform(post("/api/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"请帮我分析 Java 代码\",\"conversationId\":\"" + conversationId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(conversationId));

        mockMvc.perform(get("/api/conversations/{conversationId}/messages", conversationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].role").value("user"))
                .andExpect(jsonPath("$[1].role").value("assistant"))
                .andExpect(jsonPath("$[1].route.modelId").value("coding-mock"));
    }
}
