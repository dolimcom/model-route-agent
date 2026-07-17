package com.dolimcom.semanticrouter.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FallbackSpec {

    private FallbackMode emptyInput = FallbackMode.REJECT;
    private FallbackMode lowConfidence = FallbackMode.REJECT;
    private FallbackMode ambiguous = FallbackMode.REJECT;
    private FallbackMode outOfDomain = FallbackMode.REJECT;
    private FallbackMode encoderError = FallbackMode.REJECT;
    private FallbackMode tie = FallbackMode.REJECT;
    private String defaultRouteId;

    public FallbackMode getEmptyInput() {
        return emptyInput;
    }

    public void setEmptyInput(FallbackMode emptyInput) {
        this.emptyInput = emptyInput;
    }

    public FallbackMode getLowConfidence() {
        return lowConfidence;
    }

    public void setLowConfidence(FallbackMode lowConfidence) {
        this.lowConfidence = lowConfidence;
    }

    public FallbackMode getAmbiguous() {
        return ambiguous;
    }

    public void setAmbiguous(FallbackMode ambiguous) {
        this.ambiguous = ambiguous;
    }

    public FallbackMode getOutOfDomain() {
        return outOfDomain;
    }

    public void setOutOfDomain(FallbackMode outOfDomain) {
        this.outOfDomain = outOfDomain;
    }

    public FallbackMode getEncoderError() {
        return encoderError;
    }

    public void setEncoderError(FallbackMode encoderError) {
        this.encoderError = encoderError;
    }

    public FallbackMode getTie() {
        return tie;
    }

    public void setTie(FallbackMode tie) {
        this.tie = tie;
    }

    public String getDefaultRouteId() {
        return defaultRouteId;
    }

    public void setDefaultRouteId(String defaultRouteId) {
        this.defaultRouteId = defaultRouteId;
    }
}
