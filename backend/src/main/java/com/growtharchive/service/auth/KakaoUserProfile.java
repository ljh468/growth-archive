package com.growtharchive.service.auth;

public record KakaoUserProfile(
    String providerUserId,
    String email,
    String nickname,
    String profileImageUrl
) {
}
