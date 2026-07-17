package com.dolimcom.semanticrouter.observability;

import com.dolimcom.semanticrouter.api.RoutingEventListener;
import com.dolimcom.semanticrouter.model.RoutingRequest;
import com.dolimcom.semanticrouter.model.RoutingResult;
import com.dolimcom.semanticrouter.model.SnapshotReloadResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class StructuredLoggingRoutingEventListener implements RoutingEventListener {

    private static final Logger log = LoggerFactory.getLogger(StructuredLoggingRoutingEventListener.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onDecision(RoutingRequest request, RoutingResult result) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", "semantic_route_decision");
        payload.put("inputHash", result.trace().inputHash());
        payload.put("routeId", result.routeId());
        payload.put("status", result.status().name());
        payload.put("topScore", result.rawScore());
        payload.put("margin", result.margin());
        payload.put("confidence", result.confidence());
        payload.put("reasonCode", result.trace().reasonCode().name());
        payload.put("fallbackReason", result.trace().fallbackReason());
        payload.put("configVersion", result.trace().configVersion());
        payload.put("encoderVersion", result.trace().encoderVersion());
        payload.put("encodeMs", result.trace().timings().encodeMs());
        payload.put("searchMs", result.trace().timings().searchMs());
        payload.put("policyMs", result.trace().timings().policyMs());
        payload.put("totalMs", result.trace().timings().totalMs());
        log.info(asJson(payload));
    }

    @Override
    public void onReload(SnapshotReloadResult result) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", "semantic_route_reload");
        payload.put("success", result.success());
        payload.put("configVersion", result.configVersion());
        payload.put("message", result.message());
        log.info(asJson(payload));
    }

    private String asJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return payload.toString();
        }
    }
}
