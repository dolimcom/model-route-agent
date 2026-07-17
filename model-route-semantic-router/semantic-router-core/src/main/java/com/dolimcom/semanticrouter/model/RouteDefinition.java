package com.dolimcom.semanticrouter.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteDefinition {

    private String routeId;
    private String target;
    private String description;
    private AggregationMode aggregationMode = AggregationMode.TOP_K_MEAN;
    private Integer topK = 3;
    private List<String> keywords = new ArrayList<>();
    private List<String> utterances = new ArrayList<>();
    private Map<String, String> metadata = Map.of();

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AggregationMode getAggregationMode() {
        return aggregationMode;
    }

    public void setAggregationMode(AggregationMode aggregationMode) {
        this.aggregationMode = aggregationMode;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getUtterances() {
        return utterances;
    }

    public void setUtterances(List<String> utterances) {
        this.utterances = utterances;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
