package com.dolimcom.semanticrouter.evaluation;

import com.dolimcom.semanticrouter.encoder.OllamaEmbeddingEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

public class SemanticRouterEvaluationCli {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: SemanticRouterEvaluationCli <routes-yaml> <evaluation-jsonl> [ollama-base-url] [model]");
            return;
        }
        String baseUrl = args.length > 2 ? args[2] : "http://127.0.0.1:11434";
        String model = args.length > 3 ? args[3] : "bge-m3:latest";

        EvaluationRunner runner = new EvaluationRunner();
        EvaluationReport report = runner.run(
                Path.of(args[0]),
                Path.of(args[1]),
                new OllamaEmbeddingEncoder(URI.create(baseUrl), model, Duration.ofSeconds(20))
        );

        System.out.println("accuracy=" + report.accuracy());
        System.out.println("macroF1=" + report.macroF1());
        System.out.println("coverage=" + report.coverage());
        System.out.println("fallbackF1=" + report.fallbackF1());
        System.out.println("baselines=" + report.baselineAccuracy());

        Path errorsPath = Path.of("errors.jsonl");
        ObjectMapper mapper = new ObjectMapper();
        try (var writer = Files.newBufferedWriter(errorsPath)) {
            for (Map<String, Object> error : report.errors()) {
                writer.write(mapper.writeValueAsString(error));
                writer.newLine();
            }
        }
        System.out.println("errors exported to " + errorsPath.toAbsolutePath());
    }
}
