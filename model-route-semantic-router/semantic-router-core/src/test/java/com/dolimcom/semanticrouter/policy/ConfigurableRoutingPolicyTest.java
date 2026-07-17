package com.dolimcom.semanticrouter.policy;

import com.dolimcom.semanticrouter.model.FallbackMode;
import com.dolimcom.semanticrouter.model.FallbackSpec;
import com.dolimcom.semanticrouter.model.ReasonCode;
import com.dolimcom.semanticrouter.model.RouteDefinition;
import com.dolimcom.semanticrouter.model.RouteScore;
import com.dolimcom.semanticrouter.model.RouteSnapshot;
import com.dolimcom.semanticrouter.model.RoutingPolicySpec;
import com.dolimcom.semanticrouter.model.RoutingRequest;
import com.dolimcom.semanticrouter.model.RoutingStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurableRoutingPolicyTest {

    private final ConfigurableRoutingPolicy policy = new ConfigurableRoutingPolicy(new HeuristicConfidenceCalibrator());

    @Test
    void shouldFallbackToLastKnownGoodOnLowConfidence() {
        RoutingPolicySpec spec = new RoutingPolicySpec();
        FallbackSpec fallback = new FallbackSpec();
        fallback.setLowConfidence(FallbackMode.LAST_KNOWN_GOOD);
        spec.setFallback(fallback);
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setRouteId("coding");
        routeDefinition.setTarget("gpt");
        RouteSnapshot snapshot = new RouteSnapshot(
                "v1",
                "dataset",
                "encoder",
                Instant.now(),
                spec,
                Map.of("coding", routeDefinition),
                Map.of(),
                Map.of()
        );

        var result = policy.decide(
                snapshot,
                new RoutingRequest("text", "coding", null, List.of(), Map.of()),
                List.of(new RouteScore("coding", "gpt", 0.45d, 0.0d, 0.45d)),
                "hash",
                "preview",
                1L,
                1L
        );

        assertThat(result.status()).isEqualTo(RoutingStatus.FALLBACK);
        assertThat(result.routeId()).isEqualTo("coding");
        assertThat(result.trace().reasonCode()).isEqualTo(ReasonCode.LAST_KNOWN_GOOD);
        assertThat(result.trace().fallbackReason()).isEqualTo(ReasonCode.LOW_CONFIDENCE.name());
    }

    @Test
    void shouldUseOutOfDomainFallbackBeforeLowConfidenceFallback() {
        RoutingPolicySpec spec = new RoutingPolicySpec();
        FallbackSpec fallback = new FallbackSpec();
        fallback.setLowConfidence(FallbackMode.LAST_KNOWN_GOOD);
        fallback.setOutOfDomain(FallbackMode.REJECT);
        spec.setFallback(fallback);
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setRouteId("coding");
        routeDefinition.setTarget("gpt");
        RouteSnapshot snapshot = new RouteSnapshot(
                "v1",
                "dataset",
                "encoder",
                Instant.now(),
                spec,
                Map.of("coding", routeDefinition),
                Map.of(),
                Map.of()
        );

        var result = policy.decide(
                snapshot,
                new RoutingRequest("text", "coding", null, List.of(), Map.of()),
                List.of(new RouteScore("coding", "gpt", 0.1d, 0.0d, 0.1d)),
                "hash",
                "preview",
                1L,
                1L
        );

        assertThat(result.status()).isEqualTo(RoutingStatus.REJECTED);
        assertThat(result.routeId()).isNull();
        assertThat(result.trace().reasonCode()).isEqualTo(ReasonCode.OUT_OF_DOMAIN);
    }
}
