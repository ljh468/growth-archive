package com.growtharchive.config.properties;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Cors cors = new Cors();
    private final Frontend frontend = new Frontend();
    private final Jwt jwt = new Jwt();
    private final Kakao kakao = new Kakao();
    private final BookSearch bookSearch = new BookSearch();
    private final Invite invite = new Invite();

    public Cors getCors() {
        return cors;
    }

    public Frontend getFrontend() {
        return frontend;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public Kakao getKakao() {
        return kakao;
    }

    public BookSearch getBookSearch() {
        return bookSearch;
    }

    public Invite getInvite() {
        return invite;
    }

    public static class Cors {
        private String allowedOrigins = "http://localhost:3000";

        public String getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Frontend {
        private String baseUrl = "http://localhost:3000";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class Jwt {
        private String secret = "local-development-jwt-secret-change-before-production";
        private long accessTokenSeconds = 3600;
        private long refreshTokenSeconds = 1_209_600;
        private boolean secureCookie;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getAccessTokenSeconds() {
            return accessTokenSeconds;
        }

        public void setAccessTokenSeconds(long accessTokenSeconds) {
            this.accessTokenSeconds = accessTokenSeconds;
        }

        public long getRefreshTokenSeconds() {
            return refreshTokenSeconds;
        }

        public void setRefreshTokenSeconds(long refreshTokenSeconds) {
            this.refreshTokenSeconds = refreshTokenSeconds;
        }

        public boolean isSecureCookie() {
            return secureCookie;
        }

        public void setSecureCookie(boolean secureCookie) {
            this.secureCookie = secureCookie;
        }
    }

    public static class Kakao {
        private String clientId = "";
        private String clientSecret = "";
        private String redirectUri = "http://localhost:8080/api/v1/auth/kakao/callback";
        private boolean mockEnabled = true;
        private List<String> initialAdminProviderIds = new ArrayList<>();

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }

        public boolean isMockEnabled() {
            return mockEnabled;
        }

        public void setMockEnabled(boolean mockEnabled) {
            this.mockEnabled = mockEnabled;
        }

        public List<String> getInitialAdminProviderIds() {
            return initialAdminProviderIds;
        }

        public void setInitialAdminProviderIds(List<String> initialAdminProviderIds) {
            this.initialAdminProviderIds = initialAdminProviderIds;
        }
    }

    public static class Invite {
        private String initialCode = "";

        public String getInitialCode() {
            return initialCode;
        }

        public void setInitialCode(String initialCode) {
            this.initialCode = initialCode;
        }
    }

    public static class BookSearch {
        private final KakaoBook kakao = new KakaoBook();

        public KakaoBook getKakao() {
            return kakao;
        }
    }

    public static class KakaoBook {
        private String restApiKey = "";
        private String endpoint = "https://dapi.kakao.com/v3/search/book";

        public String getRestApiKey() {
            return restApiKey;
        }

        public void setRestApiKey(String restApiKey) {
            this.restApiKey = restApiKey;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }
}
