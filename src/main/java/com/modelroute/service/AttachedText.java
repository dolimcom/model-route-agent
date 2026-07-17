package com.modelroute.service;

public record AttachedText(
        String id,
        String fileName,
        String mediaType,
        String content,
        long size,
        boolean editable,
        String rootId,
        String relativePath) {
}
