package com.modelroute.dto;

import java.time.Instant;

public record FileEntryResponse(
        String name,
        String relativePath,
        boolean directory,
        long size,
        Instant lastModified) {
}
