package com.dolimcom.semanticrouter.core;

import com.dolimcom.semanticrouter.api.RouteDefinitionProvider;
import com.dolimcom.semanticrouter.api.SemanticEncoder;
import com.dolimcom.semanticrouter.index.InMemoryRouteIndex;
import com.dolimcom.semanticrouter.model.AggregationMode;
import com.dolimcom.semanticrouter.model.ReasonCode;
import com.dolimcom.semanticrouter.model.RouteDefinition;
import com.dolimcom.semanticrouter.model.RouteHint;
import com.dolimcom.semanticrouter.model.RouteHintType;
import com.dolimcom.semanticrouter.model.RouteCorpus;
import com.dolimcom.semanticrouter.model.RoutingPolicySpec;
import com.dolimcom.semanticrouter.model.RoutingRequest;
import com.dolimcom.semanticrouter.policy.ConfigurableRoutingPolicy;
import com.dolimcom.semanticrouter.policy.HeuristicConfidenceCalibrator;
import com.dolimcom.semanticrouter.scoring.CosineSimilarityScorer;
import com.dolimcom.semanticrouter.scoring.KeywordRuleScorer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultSemanticRouterTest {

    @Test
    void reportsUnavailableUntilADegradedSnapshotCanBeLoaded() {
        SemanticEncoder encoder = new SemanticEncoder() {
            @Override
            public List<double[]> encodeAll(List<String> texts) {
                throw new IllegalStateException("encoder offline");
            }

            @Override
            public String version() {
                return "offline";
            }
        };
        RouteDefinitionProvider provider = new RouteDefinitionProvider() {
            @Override
            public RouteCorpus load() {
                throw new IllegalStateException("routes unavailable");
            }

            @Override
            public String description() {
                return "unavailable";
            }
        };
        RouteSnapshotManager manager = new RouteSnapshotManager(
                provider, encoder, new RouteSnapshotFactory(), List.of(), false);
        DefaultSemanticRouter router = new DefaultSemanticRouter(
                encoder,
                manager,
                new InMemoryRouteIndex(new CosineSimilarityScorer(), new KeywordRuleScorer()),
                new ConfigurableRoutingPolicy(new HeuristicConfidenceCalibrator()),
                List.of());

        assertThat(manager.reload().success()).isFalse();
        assertThat(manager.available()).isFalse();
        assertThatThrownBy(() -> router.route(RoutingRequest.of("hello")))
                .isInstanceOf(com.dolimcom.semanticrouter.exception.SemanticRouterException.class)
                .hasMessageContaining("snapshot is not available");
    }

    @Test
    void shouldRespectHardHint() {
        SemanticEncoder encoder = new SemanticEncoder() {
            @Override
            public List<double[]> encodeAll(List<String> texts) {
                return texts.stream().map(text -> new double[]{1.0d, 0.0d}).toList();
            }

            @Override
            public String version() {
                return "stub";
            }
        };

        RouteDefinition route = new RouteDefinition();
        route.setRouteId("coding");
        route.setTarget("gpt");
        route.setAggregationMode(AggregationMode.MAX);
        route.setUtterances(List.of("写代码"));

        RouteCorpus corpus = new RouteCorpus();
        corpus.setConfigVersion("v1");
        corpus.setDatasetVersion("d1");
        corpus.setEncoderVersion("stub");
        corpus.setPolicy(new RoutingPolicySpec());
        corpus.setRoutes(List.of(route));

        RouteDefinitionProvider provider = new RouteDefinitionProvider() {
            @Override
            public RouteCorpus load() {
                return corpus;
            }

            @Override
            public String description() {
                return "test";
            }
        };

        DefaultSemanticRouter router = new DefaultSemanticRouter(
                encoder,
                new RouteSnapshotManager(provider, encoder, new RouteSnapshotFactory(), List.of()),
                new InMemoryRouteIndex(new CosineSimilarityScorer(), new KeywordRuleScorer()),
                new ConfigurableRoutingPolicy(new HeuristicConfidenceCalibrator()),
                List.of()
        );

        var result = router.route(new RoutingRequest("任何内容", null, null, List.of(new RouteHint("coding", RouteHintType.HARD, 0.0d)), Map.of()));

        assertThat(result.routeId()).isEqualTo("coding");
        assertThat(result.trace().reasonCode()).isEqualTo(ReasonCode.HARD_OVERRIDE);
    }

    @Test
    void shouldExcludeContextOnlyRouteWhenConversationContextIsMissing() {
        SemanticEncoder encoder = new SemanticEncoder() {
            @Override
            public List<double[]> encodeAll(List<String> texts) {
                return texts.stream()
                        .map(text -> text.contains("继续")
                                ? new double[]{1.0d, 0.0d}
                                : new double[]{0.8d, 0.2d})
                        .toList();
            }

            @Override
            public String version() {
                return "stub";
            }
        };

        RouteDefinition followup = new RouteDefinition();
        followup.setRouteId("followup");
        followup.setTarget("last-known");
        followup.setAggregationMode(AggregationMode.MAX);
        followup.setMetadata(Map.of("requiresContext", "true"));
        followup.setUtterances(List.of("继续说明"));

        RouteDefinition coding = new RouteDefinition();
        coding.setRouteId("coding");
        coding.setTarget("coding-model");
        coding.setAggregationMode(AggregationMode.MAX);
        coding.setUtterances(List.of("编程问题"));

        RoutingPolicySpec policy = new RoutingPolicySpec();
        policy.setMinScore(0.1d);
        policy.setOutOfDomainThreshold(0.05d);
        policy.setMinMargin(0.0d);
        policy.setTieTolerance(0.0d);

        RouteCorpus corpus = new RouteCorpus();
        corpus.setConfigVersion("v1");
        corpus.setDatasetVersion("d1");
        corpus.setEncoderVersion("stub");
        corpus.setPolicy(policy);
        corpus.setRoutes(List.of(followup, coding));

        RouteDefinitionProvider provider = new RouteDefinitionProvider() {
            @Override
            public RouteCorpus load() {
                return corpus;
            }

            @Override
            public String description() {
                return "test";
            }
        };

        DefaultSemanticRouter router = new DefaultSemanticRouter(
                encoder,
                new RouteSnapshotManager(provider, encoder, new RouteSnapshotFactory(), List.of()),
                new InMemoryRouteIndex(new CosineSimilarityScorer(), new KeywordRuleScorer()),
                new ConfigurableRoutingPolicy(new HeuristicConfidenceCalibrator()),
                List.of()
        );

        var withoutContext = router.route(RoutingRequest.of("继续说明"));
        var withContext = router.route(new RoutingRequest(
                "继续说明", "coding", null, List.of(), Map.of()));

        assertThat(withoutContext.routeId()).isEqualTo("coding");
        assertThat(withContext.routeId()).isEqualTo("followup");
    }
}
