package com.modelroute.dto;

public record FileContentResponse(
        String rootId,
        String relativePath,
        long size,
        String content) {
}
