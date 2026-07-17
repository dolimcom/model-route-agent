package com.dolimcom.semanticrouter.index;

import static org.assertj.core.api.Assertions.assertThat;

import com.dolimcom.semanticrouter.model.RouteDefinition;
import com.dolimcom.semanticrouter.model.RouteSnapshot;
import com.dolimcom.semanticrouter.model.RoutingPolicySpec;
import com.dolimcom.semanticrouter.scoring.CosineSimilarityScorer;
import com.dolimcom.semanticrouter.scoring.KeywordRuleScorer;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InMemoryRouteIndexTest {

    @Test
    void doesNotPenalizeSemanticScoreWhenNoKeywordMatches() {
        RouteDefinition coding = new RouteDefinition();
        coding.setRouteId("coding");
        coding.setTarget("coding-model");
        coding.setKeywords(List.of("Java"));

        RoutingPolicySpec policy = new RoutingPolicySpec();
        policy.setSemanticWeight(0.85d);
        policy.setKeywordWeight(0.15d);
        RouteSnapshot snapshot = new RouteSnapshot(
                "v1",
                "d1",
                "stub",
                Instant.now(),
                policy,
                Map.of("coding", coding),
                Map.of("coding", List.of(new double[]{1.0d, 0.0d})),
                Map.of("coding", new double[]{1.0d, 0.0d}));

        InMemoryRouteIndex index = new InMemoryRouteIndex(
                new CosineSimilarityScorer(), new KeywordRuleScorer());

        var result = index.search(snapshot, "排查服务异常", new double[]{1.0d, 0.0d}, 1);

        assertThat(result).singleElement().satisfies(score -> {
            assertThat(score.semanticScore()).isEqualTo(1.0d);
            assertThat(score.keywordScore()).isZero();
            assertThat(score.finalScore()).isEqualTo(1.0d);
        });
    }
}
