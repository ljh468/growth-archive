package com.growtharchive.service.storage;

public record StoredLocalImage(
    byte[] bytes,
    String mimeType
) {
}
