package com.modelroute.dto;

public record ModelCatalogEntry(
        String family,
        String label,
        String modelName,
        String provider,
        String defaultBaseUrl) {
}
