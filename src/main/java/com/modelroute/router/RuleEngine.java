package com.modelroute.router;

import com.modelroute.config.ModelRouteProperties;
import com.modelroute.domain.TaskType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Extracts deterministic, explainable signals without calling an external model.
 */
@Service
public class RuleEngine {

    private static final Pattern CODE_FENCE_PATTERN = Pattern.compile("```[\\s\\S]*?```");
    private static final Pattern STACK_TRACE_PATTERN = Pattern.compile(
            "(?m)^\\s*at\\s+[\\w.$]+\\([^\\r\\n]+\\)|\\b[\\w.$]+(?:Exception|Error)\\b");
    private static final Pattern CODE_SYNTAX_PATTERN = Pattern.compile(
            "\\b(public|private|protected|class|interface|void|static|import|package|def|function)\\b|[{};]");
    private static final Pattern MATH_EXPRESSION_PATTERN = Pattern.compile(
            "(?:\\d+\\s*[+\\-*/=<>]\\s*\\d+)|(?:[a-zA-Z]\\s*\\^\\s*\\d+)|[∫√Σπ∞]|\\b(sin|cos|tan|log|sqrt)\\s*\\(",
            Pattern.CASE_INSENSITIVE);

    private final ModelRouteProperties properties;

    public RuleEngine(ModelRouteProperties properties) {
        this.properties = properties;
    }

    public RuleAnalysis analyze(String question) {
        String normalizedQuestion = question.toLowerCase(Locale.ROOT);
        List<RuleSignal> signals = new ArrayList<>();

        collectConfiguredKeywords(normalizedQuestion, signals);
        if (CODE_FENCE_PATTERN.matcher(question).find()) {
            signals.add(new RuleSignal(TaskType.CODING, "code-fence", "triple-backtick block", true));
        }
        if (STACK_TRACE_PATTERN.matcher(question).find()) {
            signals.add(new RuleSignal(TaskType.CODING, "stack-trace", "exception or stack frame", true));
        }
        if (CODE_SYNTAX_PATTERN.matcher(question).find()) {
            signals.add(new RuleSignal(TaskType.CODING, "code-syntax", "programming syntax", false));
        }
        if (MATH_EXPRESSION_PATTERN.matcher(question).find()) {
            signals.add(new RuleSignal(TaskType.MATH, "math-expression", "formula or math symbol", true));
        }

        return new RuleAnalysis(signals);
    }

    public TaskType selectTaskType(RuleAnalysis analysis) {
        Optional<TaskType> strongTaskType = analysis.strongestTaskType();
        if (strongTaskType.isPresent()) {
            return strongTaskType.get();
        }
        return analysis.keywordCandidates().stream().findFirst().orElse(TaskType.GENERAL);
    }

    private void collectConfiguredKeywords(String normalizedQuestion, List<RuleSignal> signals) {
        for (Map.Entry<TaskType, List<String>> entry : properties.getRouter().getKeywords().entrySet()) {
            for (String keyword : entry.getValue()) {
                if (normalizedQuestion.contains(keyword.toLowerCase(Locale.ROOT))) {
                    signals.add(new RuleSignal(entry.getKey(), "keyword", keyword, false));
                }
            }
        }
    }
}
