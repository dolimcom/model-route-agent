package com.dolimcom.semanticrouter.model;

public enum ReasonCode {
    ACCEPTED,
    HARD_OVERRIDE,
    STATIC_OVERRIDE,
    LOW_CONFIDENCE,
    AMBIGUOUS,
    TIE,
    OUT_OF_DOMAIN,
    EMPTY_INPUT,
    ENCODER_ERROR,
    LAST_KNOWN_GOOD,
    DEFAULT_ROUTE,
    NO_CANDIDATES
}
