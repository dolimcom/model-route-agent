package com.dolimcom.semanticrouter.model;

import java.time.Instant;

public record SnapshotReloadResult(
        boolean success,
        String configVersion,
        Instant loadedAt,
        String message
) {
}
