package com.growtharchive.service.storage;

public record StoredImage(
    String bucket,
    String objectKey,
    String publicUrl,
    String mimeType,
    Long sizeBytes
) {
}
