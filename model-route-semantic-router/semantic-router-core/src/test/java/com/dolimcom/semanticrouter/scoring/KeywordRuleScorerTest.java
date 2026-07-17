package com.dolimcom.semanticrouter.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.dolimcom.semanticrouter.model.RouteDefinition;
import java.util.List;
import org.junit.jupiter.api.Test;

class KeywordRuleScorerTest {

    private final KeywordRuleScorer scorer = new KeywordRuleScorer();

    @Test
    void returnsFullSignalWhenAnyConfiguredKeywordMatches() {
        RouteDefinition route = new RouteDefinition();
        route.setKeywords(List.of("代码", "Java", "调试", "接口"));

        assertThat(scorer.score("请帮我检查这个 Java 服务", route)).isEqualTo(1.0d);
    }

    @Test
    void returnsZeroWhenNoKeywordMatches() {
        RouteDefinition route = new RouteDefinition();
        route.setKeywords(List.of("代码", "Java"));

        assertThat(scorer.score("帮我安排明天的课程", route)).isZero();
    }

    @Test
    void ignoresNegatedKeywordButKeepsAffirmedKeyword() {
        RouteDefinition route = new RouteDefinition();
        route.setKeywords(List.of("代码", "Java"));

        assertThat(scorer.score("代码先不用写，请手工计算结果", route)).isZero();
        assertThat(scorer.score("不要分析代码，只修改 Java 接口", route)).isEqualTo(1.0d);
    }

    @Test
    void contrastMarkerEndsEarlierNegationScope() {
        RouteDefinition coding = new RouteDefinition();
        coding.setKeywords(List.of("代码"));
        RouteDefinition literary = new RouteDefinition();
        literary.setKeywords(List.of("文学"));

        String input = "这不是文学创作，而是代码调试";

        assertThat(scorer.score(input, literary)).isZero();
        assertThat(scorer.score(input, coding)).isEqualTo(1.0d);
    }
}
