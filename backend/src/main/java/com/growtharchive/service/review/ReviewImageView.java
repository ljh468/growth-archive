package com.growtharchive.service.review;

public record ReviewImageView(
    Long imageId,
    String imageUrl,
    int displayOrder
) {
}
