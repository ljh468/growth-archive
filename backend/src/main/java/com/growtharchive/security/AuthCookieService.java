package com.growtharchive.security;

import com.growtharchive.config.properties.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    public static final String SIGNUP_TOKEN_COOKIE = "signup_token";
    public static final String MOCK_KAKAO_PROVIDER_USER_COOKIE = "mock_kakao_provider_user_id";

    private final AppProperties properties;

    public AuthCookieService(AppProperties properties) {
        this.properties = properties;
    }

    public void addAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        addCookie(response, ACCESS_TOKEN_COOKIE, accessToken, properties.getJwt().getAccessTokenSeconds());
        addCookie(response, REFRESH_TOKEN_COOKIE, refreshToken, properties.getJwt().getRefreshTokenSeconds());
    }

    public void clearAuthCookies(HttpServletResponse response) {
        addCookie(response, ACCESS_TOKEN_COOKIE, "", 0);
        addCookie(response, REFRESH_TOKEN_COOKIE, "", 0);
    }

    public void addSignupCookie(HttpServletResponse response, String signupToken) {
        addCookie(response, SIGNUP_TOKEN_COOKIE, signupToken, properties.getJwt().getRefreshTokenSeconds());
    }

    public void clearSignupCookie(HttpServletResponse response) {
        addCookie(response, SIGNUP_TOKEN_COOKIE, "", 0);
    }

    public void addMockKakaoProviderUserCookie(HttpServletResponse response, String providerUserId) {
        addCookie(response, MOCK_KAKAO_PROVIDER_USER_COOKIE, providerUserId, properties.getJwt().getRefreshTokenSeconds());
    }

    public void clearMockKakaoProviderUserCookie(HttpServletResponse response) {
        addCookie(response, MOCK_KAKAO_PROVIDER_USER_COOKIE, "", 0);
    }

    public String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (var cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void addCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(properties.getJwt().isSecureCookie())
            .sameSite("Lax")
            .path("/")
            .maxAge(Duration.ofSeconds(maxAgeSeconds))
            .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
