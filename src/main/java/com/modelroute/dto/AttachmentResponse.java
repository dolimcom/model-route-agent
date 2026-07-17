package com.modelroute.dto;

public record AttachmentResponse(
        String id,
        String fileName,
        long size,
        String mediaType,
        String preview,
        boolean editable,
        String rootId,
        String relativePath) {
}
