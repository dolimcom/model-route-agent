package com.dolimcom.semanticrouter.evaluation;

import com.dolimcom.semanticrouter.api.RouteDefinitionProvider;
import com.dolimcom.semanticrouter.api.SemanticEncoder;
import com.dolimcom.semanticrouter.api.SemanticRouter;
import com.dolimcom.semanticrouter.core.DefaultSemanticRouter;
import com.dolimcom.semanticrouter.core.RouteSnapshotFactory;
import com.dolimcom.semanticrouter.core.RouteSnapshotManager;
import com.dolimcom.semanticrouter.index.InMemoryRouteIndex;
import com.dolimcom.semanticrouter.loader.RouteCorpusLoader;
import com.dolimcom.semanticrouter.loader.YamlRouteDefinitionProvider;
import com.dolimcom.semanticrouter.model.RouteDefinition;
import com.dolimcom.semanticrouter.model.RouteSnapshot;
import com.dolimcom.semanticrouter.model.RoutingRequest;
import com.dolimcom.semanticrouter.model.RoutingResult;
import com.dolimcom.semanticrouter.policy.ConfigurableRoutingPolicy;
import com.dolimcom.semanticrouter.policy.HeuristicConfidenceCalibrator;
import com.dolimcom.semanticrouter.scoring.CosineSimilarityScorer;
import com.dolimcom.semanticrouter.scoring.KeywordRuleScorer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EvaluationRunner {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public EvaluationReport run(Path routeCorpusPath, Path evaluationPath, SemanticEncoder encoder) throws IOException {
        RouteDefinitionProvider provider = new YamlRouteDefinitionProvider(routeCorpusPath);
        RouteSnapshotManager snapshotManager = new RouteSnapshotManager(provider, encoder, new RouteSnapshotFactory(), List.of());
        SemanticRouter router = new DefaultSemanticRouter(
                encoder,
                snapshotManager,
                new InMemoryRouteIndex(new CosineSimilarityScorer(), new KeywordRuleScorer()),
                new ConfigurableRoutingPolicy(new HeuristicConfidenceCalibrator()),
                List.of()
        );
        List<EvaluationSample> samples = loadSamples(evaluationPath);
        RouteSnapshot snapshot = snapshotManager.currentSnapshot();

        int correct = 0;
        int fallbackCorrect = 0;
        int fallbackExpected = 0;
        int fallbackPredicted = 0;
        Map<String, Map<String, Integer>> confusion = new LinkedHashMap<>();
        List<Map<String, Object>> errors = new ArrayList<>();

        for (EvaluationSample sample : samples) {
            RoutingResult result = router.route(RoutingRequest.of(sample.input()));
            String predicted = result.routeId() == null ? "__REJECTED__" : result.routeId();
            confusion.computeIfAbsent(sample.expectedRouteId(), ignored -> new LinkedHashMap<>())
                    .merge(predicted, 1, Integer::sum);
            boolean expectedFallback = sample.expectedFallback();
            boolean predictedFallback = result.status() != com.dolimcom.semanticrouter.model.RoutingStatus.ROUTED
                    && result.status() != com.dolimcom.semanticrouter.model.RoutingStatus.OVERRIDDEN;
            if (sample.expectedRouteId().equals(result.routeId())) {
                correct++;
            } else {
                errors.add(Map.of(
                        "input", sample.input(),
                        "expectedRouteId", sample.expectedRouteId(),
                        "actualRouteId", predicted,
                        "topCandidates", result.topCandidates(),
                        "rawScore", result.rawScore(),
                        "margin", result.margin(),
                        "fallbackReason", result.trace().fallbackReason(),
                        "configVersion", result.trace().configVersion()
                ));
            }
            if (expectedFallback) {
                fallbackExpected++;
            }
            if (predictedFallback) {
                fallbackPredicted++;
            }
            if (expectedFallback && predictedFallback) {
                fallbackCorrect++;
            }
        }

        Map<String, Double> baselines = baselineAccuracy(snapshot.routes().values().stream().toList(), samples);
        return new EvaluationReport(
                ratio(correct, samples.size()),
                macroF1(confusion),
                coverage(samples.size() - fallbackPredicted, samples.size()),
                f1(fallbackCorrect, fallbackPredicted, fallbackExpected),
                confusion,
                baselines,
                errors
        );
    }

    public List<EvaluationSample> loadSamples(Path evaluationPath) throws IOException {
        List<EvaluationSample> samples = new ArrayList<>();
        for (String line : Files.readAllLines(evaluationPath)) {
            if (line.isBlank()) {
                continue;
            }
            samples.add(objectMapper.readValue(line, EvaluationSample.class));
        }
        return samples;
    }

    private Map<String, Double> baselineAccuracy(List<RouteDefinition> routes, List<EvaluationSample> samples) {
        Map<String, Double> baselines = new LinkedHashMap<>();
        baselines.put("random", randomBaseline(routes, samples));
        baselines.put("fixed", fixedBaseline(routes, samples));
        baselines.put("keyword", keywordBaseline(routes, samples));
        return baselines;
    }

    private double randomBaseline(List<RouteDefinition> routes, List<EvaluationSample> samples) {
        Random random = new Random(42L);
        int correct = 0;
        for (EvaluationSample sample : samples) {
            String predicted = routes.get(random.nextInt(routes.size())).getRouteId();
            if (predicted.equals(sample.expectedRouteId())) {
                correct++;
            }
        }
        return ratio(correct, samples.size());
    }

    private double fixedBaseline(List<RouteDefinition> routes, List<EvaluationSample> samples) {
        String predicted = routes.stream()
                .max(Comparator.comparingInt(route -> route.getUtterances().size()))
                .map(RouteDefinition::getRouteId)
                .orElse("");
        int correct = 0;
        for (EvaluationSample sample : samples) {
            if (predicted.equals(sample.expectedRouteId())) {
                correct++;
            }
        }
        return ratio(correct, samples.size());
    }

    private double keywordBaseline(List<RouteDefinition> routes, List<EvaluationSample> samples) {
        KeywordRuleScorer scorer = new KeywordRuleScorer();
        int correct = 0;
        for (EvaluationSample sample : samples) {
            String predicted = routes.stream()
                    .max(Comparator.comparingDouble(route -> scorer.score(sample.input(), route)))
                    .map(RouteDefinition::getRouteId)
                    .orElse("");
            if (predicted.equals(sample.expectedRouteId())) {
                correct++;
            }
        }
        return ratio(correct, samples.size());
    }

    private double macroF1(Map<String, Map<String, Integer>> confusion) {
        List<Double> f1Scores = new ArrayList<>();
        for (String label : confusion.keySet()) {
            int tp = confusion.getOrDefault(label, Map.of()).getOrDefault(label, 0);
            int fp = confusion.values().stream()
                    .filter(entry -> !entry.equals(confusion.get(label)))
                    .mapToInt(entry -> entry.getOrDefault(label, 0))
                    .sum();
            int fn = confusion.getOrDefault(label, Map.of()).entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(label))
                    .mapToInt(Map.Entry::getValue)
                    .sum();
            f1Scores.add(f1(tp, tp + fp, tp + fn));
        }
        return f1Scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0d);
    }

    private double f1(int tp, int predictedPositive, int actualPositive) {
        double precision = predictedPositive == 0 ? 0.0d : (double) tp / (double) predictedPositive;
        double recall = actualPositive == 0 ? 0.0d : (double) tp / (double) actualPositive;
        if (precision + recall == 0.0d) {
            return 0.0d;
        }
        return (2.0d * precision * recall) / (precision + recall);
    }

    private double coverage(int routed, int total) {
        return ratio(routed, total);
    }

    private double ratio(int numerator, int denominator) {
        if (denominator == 0) {
            return 0.0d;
        }
        return (double) numerator / (double) denominator;
    }
}
