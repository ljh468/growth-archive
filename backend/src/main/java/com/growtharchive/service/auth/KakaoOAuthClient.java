package com.growtharchive.service.auth;

import com.growtharchive.config.properties.AppProperties;
import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class KakaoOAuthClient {

    private final AppProperties properties;

    public KakaoOAuthClient(AppProperties properties) {
        this.properties = properties;
    }

    public String authorizationUrl(String state) {
        if (properties.getKakao().isMockEnabled() && properties.getKakao().getClientId().isBlank()) {
            String code = "mock-" + UUID.randomUUID();
            return "/api/v1/auth/kakao/callback?code=" + code + "&state=" + encode(state);
        }
        if (properties.getKakao().getClientId().isBlank()) {
            throw new ApiException(ErrorCode.KAKAO_NOT_CONFIGURED);
        }
        return "https://kauth.kakao.com/oauth/authorize"
            + "?response_type=code"
            + "&client_id=" + encode(properties.getKakao().getClientId())
            + "&redirect_uri=" + encode(properties.getKakao().getRedirectUri())
            + "&state=" + encode(state);
    }

    public String authorizationUrl(String state, String mockProviderUserId) {
        if (properties.getKakao().isMockEnabled() && properties.getKakao().getClientId().isBlank()) {
            return "/api/v1/auth/kakao/callback?code=mock-" + encode(mockProviderUserId) + "&state=" + encode(state);
        }
        if (properties.getKakao().getClientId().isBlank()) {
            throw new ApiException(ErrorCode.KAKAO_NOT_CONFIGURED);
        }
        return "https://kauth.kakao.com/oauth/authorize"
            + "?response_type=code"
            + "&client_id=" + encode(properties.getKakao().getClientId())
            + "&redirect_uri=" + encode(properties.getKakao().getRedirectUri())
            + "&state=" + encode(state);
    }

    public KakaoUserProfile fetchProfile(String code) {
        if (properties.getKakao().isMockEnabled() && code != null && code.startsWith("mock-")) {
            String id = code.substring("mock-".length());
            return new KakaoUserProfile(id, id + "@mock.kakao.local", "카카오 사용자", null);
        }
        throw new ApiException(ErrorCode.KAKAO_NOT_CONFIGURED, "실제 카카오 OAuth 연동은 환경변수 설정 후 활성화됩니다.");
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
