package com.dolimcom.semanticrouter.loader;

import com.dolimcom.semanticrouter.exception.SemanticRouterException;
import com.dolimcom.semanticrouter.model.RouteCorpus;
import com.dolimcom.semanticrouter.model.RouteDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class RouteCorpusLoader {

    private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    public RouteCorpus load(InputStream inputStream) {
        try {
            RouteCorpus corpus = objectMapper.readValue(inputStream, RouteCorpus.class);
            validate(corpus);
            return corpus;
        } catch (IOException ex) {
            throw new SemanticRouterException("Failed to load route corpus", ex);
        }
    }

    private void validate(RouteCorpus corpus) {
        if (corpus.getRoutes() == null || corpus.getRoutes().isEmpty()) {
            throw new SemanticRouterException("Route corpus must contain routes");
        }
        if (corpus.getPolicy().getMinScore() < 0.0d || corpus.getPolicy().getMinScore() > 1.0d) {
            throw new SemanticRouterException("minScore must be within [0,1]");
        }
        Set<String> routeIds = new HashSet<>();
        for (RouteDefinition route : corpus.getRoutes()) {
            if (route.getRouteId() == null || route.getRouteId().isBlank()) {
                throw new SemanticRouterException("routeId is required");
            }
            if (!routeIds.add(route.getRouteId())) {
                throw new SemanticRouterException("Duplicate routeId: " + route.getRouteId());
            }
            if (route.getTarget() == null || route.getTarget().isBlank()) {
                throw new SemanticRouterException("target is required for route " + route.getRouteId());
            }
            if (route.getUtterances() == null || route.getUtterances().isEmpty()) {
                throw new SemanticRouterException("utterances are required for route " + route.getRouteId());
            }
            if (route.getUtterances().stream().anyMatch(text -> text == null || text.isBlank())) {
                throw new SemanticRouterException("empty utterance found in route " + route.getRouteId());
            }
        }
    }
}
