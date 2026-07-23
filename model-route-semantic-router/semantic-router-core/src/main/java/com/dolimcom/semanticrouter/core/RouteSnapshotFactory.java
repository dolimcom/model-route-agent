package com.dolimcom.semanticrouter.core;

import com.dolimcom.semanticrouter.api.SemanticEncoder;
import com.dolimcom.semanticrouter.exception.SemanticRouterException;
import com.dolimcom.semanticrouter.model.RouteCorpus;
import com.dolimcom.semanticrouter.model.RouteDefinition;
import com.dolimcom.semanticrouter.model.RouteSnapshot;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RouteSnapshotFactory {

    public RouteSnapshot build(RouteCorpus corpus, SemanticEncoder encoder) {
        Map<String, RouteDefinition> routes = new LinkedHashMap<>();
        Map<String, List<double[]>> embeddings = new LinkedHashMap<>();
        Map<String, double[]> centroids = new LinkedHashMap<>();

        for (RouteDefinition route : corpus.getRoutes()) {
            List<double[]> routeEmbeddings = encoder.encodeAll(route.getUtterances());
            if (routeEmbeddings.isEmpty()) {
                throw new SemanticRouterException("No embeddings generated for route " + route.getRouteId());
            }
            if (routeEmbeddings.size() != route.getUtterances().size()) {
                throw new SemanticRouterException("Embedding count mismatch for route " + route.getRouteId()
                        + ": expected " + route.getUtterances().size() + " but received " + routeEmbeddings.size());
            }
            routes.put(route.getRouteId(), route);
            embeddings.put(route.getRouteId(), routeEmbeddings);
            centroids.put(route.getRouteId(), centroid(routeEmbeddings));
        }

        return new RouteSnapshot(
                corpus.getConfigVersion(),
                corpus.getDatasetVersion(),
                corpus.getEncoderVersion() == null ? encoder.version() : corpus.getEncoderVersion(),
                Instant.now(),
                corpus.getPolicy(),
                Map.copyOf(routes),
                Map.copyOf(embeddings),
                Map.copyOf(centroids)
        );
    }

    private double[] centroid(List<double[]> vectors) {
        int size = vectors.get(0).length;
        if (size == 0) {
            throw new SemanticRouterException("Embedding vectors must not be empty");
        }
        double[] centroid = new double[size];
        for (double[] vector : vectors) {
            if (vector.length != size) {
                throw new SemanticRouterException("Embedding dimension mismatch");
            }
            for (int i = 0; i < vector.length; i++) {
                centroid[i] += vector[i];
            }
        }
        for (int i = 0; i < centroid.length; i++) {
            centroid[i] /= vectors.size();
        }
        return centroid;
    }
}
