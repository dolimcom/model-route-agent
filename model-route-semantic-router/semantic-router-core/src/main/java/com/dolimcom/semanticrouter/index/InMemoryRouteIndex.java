package com.dolimcom.semanticrouter.index;

import com.dolimcom.semanticrouter.model.AggregationMode;
import com.dolimcom.semanticrouter.model.RouteDefinition;
import com.dolimcom.semanticrouter.model.RouteScore;
import com.dolimcom.semanticrouter.model.RouteSnapshot;
import com.dolimcom.semanticrouter.scoring.KeywordRuleScorer;
import com.dolimcom.semanticrouter.scoring.SimilarityScorer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class InMemoryRouteIndex implements RouteIndex {

    private final SimilarityScorer similarityScorer;
    private final KeywordRuleScorer keywordRuleScorer;

    public InMemoryRouteIndex(SimilarityScorer similarityScorer, KeywordRuleScorer keywordRuleScorer) {
        this.similarityScorer = similarityScorer;
        this.keywordRuleScorer = keywordRuleScorer;
    }

    @Override
    public List<RouteScore> search(RouteSnapshot snapshot, String input, double[] inputEmbedding, int limit) {
        List<RouteScore> scores = new ArrayList<>();
        for (Map.Entry<String, RouteDefinition> entry : snapshot.routes().entrySet()) {
            String routeId = entry.getKey();
            RouteDefinition route = entry.getValue();
            double semanticScore = aggregateScore(route, snapshot, inputEmbedding);
            double keywordScore = keywordRuleScorer.score(input, route);
            double finalScore = combine(snapshot, semanticScore, keywordScore);
            scores.add(new RouteScore(routeId, route.getTarget(), semanticScore, keywordScore, finalScore));
        }
        scores.sort(Comparator.comparingDouble(RouteScore::finalScore).reversed().thenComparing(RouteScore::routeId));
        return scores.stream().limit(limit).toList();
    }

    private double combine(RouteSnapshot snapshot, double semanticScore, double keywordScore) {
        if (keywordScore <= 0.0d) {
            return semanticScore;
        }
        double semanticWeight = snapshot.policySpec().getSemanticWeight();
        double keywordWeight = snapshot.policySpec().getKeywordWeight();
        double totalWeight = semanticWeight + keywordWeight;
        if (totalWeight <= 0.0d) {
            return semanticScore;
        }
        return ((semanticWeight * semanticScore) + (keywordWeight * keywordScore)) / totalWeight;
    }

    private double aggregateScore(RouteDefinition route, RouteSnapshot snapshot, double[] inputEmbedding) {
        AggregationMode mode = route.getAggregationMode() == null ? AggregationMode.TOP_K_MEAN : route.getAggregationMode();
        return switch (mode) {
            case MAX -> maxScore(snapshot.exampleEmbeddings().get(route.getRouteId()), inputEmbedding);
            case TOP_K_MEAN -> topKMean(snapshot.exampleEmbeddings().get(route.getRouteId()), inputEmbedding, route.getTopK());
            case CENTROID -> similarityScorer.score(snapshot.centroids().get(route.getRouteId()), inputEmbedding);
        };
    }

    private double maxScore(List<double[]> embeddings, double[] inputEmbedding) {
        return embeddings.stream()
                .mapToDouble(vector -> similarityScorer.score(vector, inputEmbedding))
                .max()
                .orElse(0.0d);
    }

    private double topKMean(List<double[]> embeddings, double[] inputEmbedding, Integer topK) {
        int k = topK == null || topK < 1 ? 3 : topK;
        return embeddings.stream()
                .mapToDouble(vector -> similarityScorer.score(vector, inputEmbedding))
                .boxed()
                .sorted(Comparator.reverseOrder())
                .limit(k)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0d);
    }
}
