package com.dolimcom.semanticrouter.scoring;

import com.dolimcom.semanticrouter.model.RouteDefinition;
import com.dolimcom.semanticrouter.support.TextSupport;
import java.util.List;

public class KeywordRuleScorer {

    private static final List<String> NEGATION_MARKERS = List.of(
            "不是", "不要", "不需要", "无需", "无须", "不用", "并非", "别", "not ", "don't ");
    private static final List<String> POST_KEYWORD_NEGATIONS = List.of(
            "之外", "先不用", "不用", "不是", "不要", "先放一边", "放一边");
    private static final List<String> CONTRAST_MARKERS = List.of("而是", "但是", "但", "rather than", "but ");

    public double score(String input, RouteDefinition routeDefinition) {
        if (routeDefinition.getKeywords() == null || routeDefinition.getKeywords().isEmpty()) {
            return 0.0d;
        }
        String normalizedInput = TextSupport.normalize(input);
        boolean matched = routeDefinition.getKeywords().stream()
                .map(TextSupport::normalize)
                .filter(keyword -> !keyword.isBlank())
                .anyMatch(keyword -> containsAffirmedKeyword(normalizedInput, keyword));
        return matched ? 1.0d : 0.0d;
    }

    private boolean containsAffirmedKeyword(String input, String keyword) {
        int fromIndex = 0;
        while (fromIndex < input.length()) {
            int keywordIndex = input.indexOf(keyword, fromIndex);
            if (keywordIndex < 0) {
                return false;
            }
            if (!isNegated(input, keywordIndex, keyword.length())) {
                return true;
            }
            fromIndex = keywordIndex + keyword.length();
        }
        return false;
    }

    private boolean isNegated(String input, int keywordIndex, int keywordLength) {
        String before = input.substring(Math.max(0, keywordIndex - 10), keywordIndex);
        String after = input.substring(
                keywordIndex + keywordLength,
                Math.min(input.length(), keywordIndex + keywordLength + 8));
        int lastNegation = lastMarkerIndex(before, NEGATION_MARKERS);
        int lastContrast = lastMarkerIndex(before, CONTRAST_MARKERS);
        return (lastNegation >= 0 && lastNegation > lastContrast)
                || POST_KEYWORD_NEGATIONS.stream().anyMatch(after::startsWith);
    }

    private int lastMarkerIndex(String text, List<String> markers) {
        return markers.stream().mapToInt(text::lastIndexOf).max().orElse(-1);
    }
}
