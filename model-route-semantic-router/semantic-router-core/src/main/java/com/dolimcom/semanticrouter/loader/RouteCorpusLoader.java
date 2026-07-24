package com.dolimcom.semanticrouter.loader;

import com.dolimcom.semanticrouter.exception.SemanticRouterException;
import com.dolimcom.semanticrouter.model.RouteCorpus;
import com.dolimcom.semanticrouter.model.RouteDefinition;
import com.dolimcom.semanticrouter.model.FallbackMode;
import com.dolimcom.semanticrouter.model.OverrideRule;
import com.dolimcom.semanticrouter.model.RoutingPolicySpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
        if (corpus == null) {
            throw new SemanticRouterException("Route corpus must not be null");
        }
        if (corpus.getRoutes() == null || corpus.getRoutes().isEmpty()) {
            throw new SemanticRouterException("Route corpus must contain routes");
        }
        validatePolicy(corpus.getPolicy());
        Set<String> routeIds = new HashSet<>();
        for (RouteDefinition route : corpus.getRoutes()) {
            if (route == null) {
                throw new SemanticRouterException("Route definition must not be null");
            }
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
            if (route.getTopK() != null && route.getTopK() < 1) {
                throw new SemanticRouterException("topK must be positive for route " + route.getRouteId());
            }
        }
        validateRouteReferences(corpus.getPolicy(), routeIds);
    }

    private void validatePolicy(RoutingPolicySpec policy) {
        if (policy == null) {
            throw new SemanticRouterException("policy is required");
        }
        if (policy.getMinScore() < 0.0d || policy.getMinScore() > 1.0d) {
            throw new SemanticRouterException("minScore must be within [0,1]");
        }
        if (policy.getOutOfDomainThreshold() < 0.0d
                || policy.getOutOfDomainThreshold() > policy.getMinScore()) {
            throw new SemanticRouterException("outOfDomainThreshold must be within [0,minScore]");
        }
        if (policy.getMinMargin() < 0.0d || policy.getMinMargin() > 2.0d) {
            throw new SemanticRouterException("minMargin must be within [0,2]");
        }
        if (policy.getTieTolerance() < 0.0d || policy.getTieTolerance() > policy.getMinMargin()) {
            throw new SemanticRouterException("tieTolerance must be within [0,minMargin]");
        }
        if (policy.getSemanticWeight() < 0.0d || policy.getKeywordWeight() < 0.0d
                || policy.getSemanticWeight() + policy.getKeywordWeight() <= 0.0d) {
            throw new SemanticRouterException("semanticWeight and keywordWeight must be non-negative with a positive sum");
        }
        if (policy.getSoftHintBoost() < 0.0d || policy.getSoftHintBoost() > 1.0d) {
            throw new SemanticRouterException("softHintBoost must be within [0,1]");
        }
        if (policy.getTopCandidates() < 1) {
            throw new SemanticRouterException("topCandidates must be positive");
        }
        if (policy.getFallback() == null) {
            throw new SemanticRouterException("fallback policy is required");
        }
        if (policy.getFallback().getEmptyInput() == null
                || policy.getFallback().getLowConfidence() == null
                || policy.getFallback().getAmbiguous() == null
                || policy.getFallback().getOutOfDomain() == null
                || policy.getFallback().getEncoderError() == null
                || policy.getFallback().getTie() == null) {
            throw new SemanticRouterException("fallback modes must not be null");
        }
    }

    private void validateRouteReferences(RoutingPolicySpec policy, Set<String> routeIds) {
        boolean usesDefaultRoute = policy.getFallback().getEmptyInput() == FallbackMode.DEFAULT_ROUTE
                || policy.getFallback().getLowConfidence() == FallbackMode.DEFAULT_ROUTE
                || policy.getFallback().getAmbiguous() == FallbackMode.DEFAULT_ROUTE
                || policy.getFallback().getOutOfDomain() == FallbackMode.DEFAULT_ROUTE
                || policy.getFallback().getEncoderError() == FallbackMode.DEFAULT_ROUTE
                || policy.getFallback().getTie() == FallbackMode.DEFAULT_ROUTE;
        String defaultRouteId = policy.getFallback().getDefaultRouteId();
        if (usesDefaultRoute && (defaultRouteId == null || !routeIds.contains(defaultRouteId))) {
            throw new SemanticRouterException("defaultRouteId must reference an existing route");
        }
        if (policy.getOverrides() == null) {
            throw new SemanticRouterException("overrides must not be null");
        }
        for (OverrideRule override : policy.getOverrides()) {
            if (override == null || override.routeId() == null || !routeIds.contains(override.routeId())) {
                throw new SemanticRouterException("override must reference an existing route");
            }
            if (override.pattern() == null || override.pattern().isBlank()) {
                throw new SemanticRouterException("override pattern must not be blank");
            }
            if (override.matchMode() == com.dolimcom.semanticrouter.model.OverrideMatchMode.REGEX) {
                try {
                    Pattern.compile(override.pattern());
                } catch (PatternSyntaxException exception) {
                    throw new SemanticRouterException("Invalid override regex: " + override.pattern(), exception);
                }
            }
        }
    }
}
