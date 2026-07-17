package com.dolimcom.semanticrouter.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RoutingPolicySpec {

    private double minScore = 0.56d;
    private double outOfDomainThreshold = 0.36d;
    private double minMargin = 0.08d;
    private double tieTolerance = 0.01d;
    private double semanticWeight = 0.88d;
    private double keywordWeight = 0.12d;
    private double softHintBoost = 0.10d;
    private int topCandidates = 3;
    private FallbackSpec fallback = new FallbackSpec();
    private List<OverrideRule> overrides = new ArrayList<>();

    public double getMinScore() {
        return minScore;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }

    public double getOutOfDomainThreshold() {
        return outOfDomainThreshold;
    }

    public void setOutOfDomainThreshold(double outOfDomainThreshold) {
        this.outOfDomainThreshold = outOfDomainThreshold;
    }

    public double getMinMargin() {
        return minMargin;
    }

    public void setMinMargin(double minMargin) {
        this.minMargin = minMargin;
    }

    public double getTieTolerance() {
        return tieTolerance;
    }

    public void setTieTolerance(double tieTolerance) {
        this.tieTolerance = tieTolerance;
    }

    public double getSemanticWeight() {
        return semanticWeight;
    }

    public void setSemanticWeight(double semanticWeight) {
        this.semanticWeight = semanticWeight;
    }

    public double getKeywordWeight() {
        return keywordWeight;
    }

    public void setKeywordWeight(double keywordWeight) {
        this.keywordWeight = keywordWeight;
    }

    public double getSoftHintBoost() {
        return softHintBoost;
    }

    public void setSoftHintBoost(double softHintBoost) {
        this.softHintBoost = softHintBoost;
    }

    public int getTopCandidates() {
        return topCandidates;
    }

    public void setTopCandidates(int topCandidates) {
        this.topCandidates = topCandidates;
    }

    public FallbackSpec getFallback() {
        return fallback;
    }

    public void setFallback(FallbackSpec fallback) {
        this.fallback = fallback;
    }

    public List<OverrideRule> getOverrides() {
        return overrides;
    }

    public void setOverrides(List<OverrideRule> overrides) {
        this.overrides = overrides;
    }
}
