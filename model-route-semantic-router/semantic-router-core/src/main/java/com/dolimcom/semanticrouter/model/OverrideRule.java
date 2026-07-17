package com.dolimcom.semanticrouter.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OverrideRule(
        OverrideMatchMode matchMode,
        String pattern,
        String routeId
) {
}
