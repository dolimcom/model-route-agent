package com.modelroute.router;

import static org.assertj.core.api.Assertions.assertThat;

import com.modelroute.config.ModelRouteProperties;
import com.modelroute.domain.TaskType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuleEngineTest {

    private final RuleEngine ruleEngine = new RuleEngine(properties());

    @Test
    void selectsCodingForCodeFence() {
        RuleAnalysis analysis = ruleEngine.analyze("请解释这段代码：```java\\nclass Demo {}\\n```");

        assertThat(ruleEngine.selectTaskType(analysis)).isEqualTo(TaskType.CODING);
        assertThat(analysis.summaryFor(TaskType.CODING)).contains("code-fence");
    }

    @Test
    void selectsCodingForStackTrace() {
        RuleAnalysis analysis = ruleEngine.analyze(
                "java.lang.NullPointerException\\n  at com.example.Demo.main(Demo.java:10)");

        assertThat(ruleEngine.selectTaskType(analysis)).isEqualTo(TaskType.CODING);
        assertThat(analysis.summaryFor(TaskType.CODING)).contains("stack-trace");
    }

    @Test
    void selectsMathForFormulaWithoutMathKeyword() {
        RuleAnalysis analysis = ruleEngine.analyze("请解方程 x^2 + 2x = 0");

        assertThat(ruleEngine.selectTaskType(analysis)).isEqualTo(TaskType.MATH);
        assertThat(analysis.summaryFor(TaskType.MATH)).contains("math-expression");
    }

    @Test
    void selectsTaskUsingConfiguredKeyword() {
        RuleAnalysis analysis = ruleEngine.analyze("帮我润色这段文案");

        assertThat(ruleEngine.selectTaskType(analysis)).isEqualTo(TaskType.LITERARY);
        assertThat(analysis.summaryFor(TaskType.LITERARY)).contains("keyword:润色");
    }

    @Test
    void fallsBackToGeneralWhenNoSignalExists() {
        RuleAnalysis analysis = ruleEngine.analyze("你好，请介绍一下你自己");

        assertThat(ruleEngine.selectTaskType(analysis)).isEqualTo(TaskType.GENERAL);
        assertThat(analysis.signals()).isEmpty();
    }

    private ModelRouteProperties properties() {
        ModelRouteProperties properties = new ModelRouteProperties();
        ModelRouteProperties.Router router = new ModelRouteProperties.Router();
        router.setKeywords(Map.of(
                TaskType.DAILY, List.of("计划", "提醒"),
                TaskType.LITERARY, List.of("润色", "文案"),
                TaskType.CODING, List.of("java", "代码"),
                TaskType.MATH, List.of("数学", "方程")));
        properties.setRouter(router);
        return properties;
    }
}
