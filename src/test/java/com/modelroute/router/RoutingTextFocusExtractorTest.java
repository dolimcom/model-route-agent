package com.modelroute.router;

import static org.assertj.core.api.Assertions.assertThat;

import com.modelroute.config.ModelRouteProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoutingTextFocusExtractorTest {

    private final RoutingTextFocusExtractor extractor = new RoutingTextFocusExtractor(properties());

    @Test
    void extractsInstructionAfterNegatedContextClause() {
        var result = extractor.extract("不继续技术问题了，为什么人容易拖延？");

        assertThat(result.text()).isEqualTo("为什么人容易拖延？");
        assertThat(result.contextSwitch()).isTrue();
        assertThat(result.changed()).isTrue();
    }

    @Test
    void extractsInstructionAfterColonInQuotedDomainConstraint() {
        var result = extractor.extract("不要写程序：一个接口每秒成功九十次，成功率是多少？");

        assertThat(result.text()).isEqualTo("一个接口每秒成功九十次，成功率是多少？");
        assertThat(result.contextSwitch()).isTrue();
    }

    @Test
    void extractsLastExplicitRequestClause() {
        var result = extractor.extract("有人引用了 Java 代码，请评价这种观点而不是分析代码");

        assertThat(result.text()).isEqualTo("请评价这种观点而不是分析代码");
        assertThat(result.contextSwitch()).isFalse();
    }

    @Test
    void extractsClauseAfterExplicitTaskMarker() {
        var result = extractor.extract("日程内容只是输入样例，只需要设计后端接口");

        assertThat(result.text()).isEqualTo("设计后端接口");
        assertThat(result.contextSwitch()).isFalse();
    }

    @Test
    void removesTrailingNegativeConstraint() {
        var result = extractor.extract("解释工作与生活平衡的含义，不要给时间安排");

        assertThat(result.text()).isEqualTo("解释工作与生活平衡的含义");
        assertThat(result.contextSwitch()).isFalse();
    }

    @Test
    void removesShortConversationAcknowledgement() {
        var result = extractor.extract("好的，按这个结果安排每天阅读时间。");

        assertThat(result.text()).isEqualTo("按这个结果安排每天阅读时间。");
        assertThat(result.changed()).isTrue();
    }

    @Test
    void keepsOrdinaryStandaloneQuestionUnchanged() {
        var result = extractor.extract("为什么天空看起来是蓝色的？");

        assertThat(result.text()).isEqualTo("为什么天空看起来是蓝色的？");
        assertThat(result.contextSwitch()).isFalse();
        assertThat(result.changed()).isFalse();
    }

    @Test
    void supportsCustomMarkersAndMultiCharacterDelimitersFromConfiguration() {
        ModelRouteProperties properties = properties();
        properties.getRouter().getFocus().setSwitchMarkers(List.of("切换至"));
        properties.getRouter().getFocus().setBoundaryDelimiters(List.of("=>"));
        RoutingTextFocusExtractor customExtractor = new RoutingTextFocusExtractor(properties);

        var result = customExtractor.extract("切换至新任务=>分析接口失败原因");

        assertThat(result.text()).isEqualTo("分析接口失败原因");
        assertThat(result.contextSwitch()).isTrue();
    }

    private ModelRouteProperties properties() {
        ModelRouteProperties.FocusRouting focus = new ModelRouteProperties.FocusRouting();
        focus.setEnabled(true);
        focus.setSwitchMarkers(List.of("不继续", "不要"));
        focus.setFocusMarkers(List.of("而是", "只需要"));
        focus.setTrailingConstraints(List.of("，不要"));
        focus.setAcknowledgements(List.of("好的，"));
        focus.setRequestMarkers(List.of("请"));
        focus.setIgnoredRequestPrefixes(List.of("请勿", "请不要"));
        focus.setBoundaryDelimiters(List.of("，", "："));

        ModelRouteProperties.Router router = new ModelRouteProperties.Router();
        router.setFocus(focus);
        ModelRouteProperties properties = new ModelRouteProperties();
        properties.setRouter(router);
        return properties;
    }
}
