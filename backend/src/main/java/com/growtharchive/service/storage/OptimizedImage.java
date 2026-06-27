package com.growtharchive.service.storage;

public record OptimizedImage(
    byte[] bytes,
    String mimeType,
    long sizeBytes,
    Integer width,
    Integer height
) {
}
