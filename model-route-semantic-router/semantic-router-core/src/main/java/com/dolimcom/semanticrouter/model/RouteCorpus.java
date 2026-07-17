package com.dolimcom.semanticrouter.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteCorpus {

    private String configVersion;
    private String datasetVersion;
    private String encoderVersion;
    private RoutingPolicySpec policy = new RoutingPolicySpec();
    private List<RouteDefinition> routes = new ArrayList<>();

    public String getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
    }

    public String getDatasetVersion() {
        return datasetVersion;
    }

    public void setDatasetVersion(String datasetVersion) {
        this.datasetVersion = datasetVersion;
    }

    public String getEncoderVersion() {
        return encoderVersion;
    }

    public void setEncoderVersion(String encoderVersion) {
        this.encoderVersion = encoderVersion;
    }

    public RoutingPolicySpec getPolicy() {
        return policy;
    }

    public void setPolicy(RoutingPolicySpec policy) {
        this.policy = policy;
    }

    public List<RouteDefinition> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteDefinition> routes) {
        this.routes = routes;
    }
}
