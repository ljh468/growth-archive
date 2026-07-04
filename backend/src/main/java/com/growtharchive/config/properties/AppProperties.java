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
    private final Storage storage = new Storage();

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

    public Storage getStorage() {
        return storage;
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
        private String sameSite = "Lax";

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

        public String getSameSite() {
            return sameSite;
        }

        public void setSameSite(String sameSite) {
            this.sameSite = sameSite;
        }
    }

    public static class Kakao {
        private String clientId = "";
        private String clientSecret = "";
        private String adminKey = "";
        private String webhookSecret = "";
        private String redirectUri = "";
        private String authorizationUri = "";
        private String tokenUri = "";
        private String userInfoUri = "";
        private String unlinkUri = "";
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

        public String getAdminKey() {
            return adminKey;
        }

        public void setAdminKey(String adminKey) {
            this.adminKey = adminKey;
        }

        public String getWebhookSecret() {
            return webhookSecret;
        }

        public void setWebhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }

        public String getAuthorizationUri() {
            return authorizationUri;
        }

        public void setAuthorizationUri(String authorizationUri) {
            this.authorizationUri = authorizationUri;
        }

        public String getTokenUri() {
            return tokenUri;
        }

        public void setTokenUri(String tokenUri) {
            this.tokenUri = tokenUri;
        }

        public String getUserInfoUri() {
            return userInfoUri;
        }

        public void setUserInfoUri(String userInfoUri) {
            this.userInfoUri = userInfoUri;
        }

        public String getUnlinkUri() {
            return unlinkUri;
        }

        public void setUnlinkUri(String unlinkUri) {
            this.unlinkUri = unlinkUri;
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
        private String adminInitialCode = "";

        public String getInitialCode() {
            return initialCode;
        }

        public void setInitialCode(String initialCode) {
            this.initialCode = initialCode;
        }

        public String getAdminInitialCode() {
            return adminInitialCode;
        }

        public void setAdminInitialCode(String adminInitialCode) {
            this.adminInitialCode = adminInitialCode;
        }
    }

    public static class BookSearch {
        private final KakaoBook kakao = new KakaoBook();

        public KakaoBook getKakao() {
            return kakao;
        }
    }

    public static class Storage {
        private final Supabase supabase = new Supabase();
        private String environmentPrefix = "local";
        private boolean localFallbackEnabled = true;

        public Supabase getSupabase() {
            return supabase;
        }

        public String getEnvironmentPrefix() {
            return environmentPrefix;
        }

        public void setEnvironmentPrefix(String environmentPrefix) {
            this.environmentPrefix = environmentPrefix;
        }

        public boolean isLocalFallbackEnabled() {
            return localFallbackEnabled;
        }

        public void setLocalFallbackEnabled(boolean localFallbackEnabled) {
            this.localFallbackEnabled = localFallbackEnabled;
        }
    }

    public static class Supabase {
        private String url = "";
        private String serviceRoleKey = "";
        private String bucket = "images";
        private String s3Endpoint = "";
        private String s3Region = "ap-northeast-2";
        private String s3AccessKeyId = "";
        private String s3SecretAccessKey = "";
        private String publicBaseUrl = "";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getServiceRoleKey() {
            return serviceRoleKey;
        }

        public void setServiceRoleKey(String serviceRoleKey) {
            this.serviceRoleKey = serviceRoleKey;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getS3Endpoint() {
            return s3Endpoint;
        }

        public void setS3Endpoint(String s3Endpoint) {
            this.s3Endpoint = s3Endpoint;
        }

        public String getS3Region() {
            return s3Region;
        }

        public void setS3Region(String s3Region) {
            this.s3Region = s3Region;
        }

        public String getS3AccessKeyId() {
            return s3AccessKeyId;
        }

        public void setS3AccessKeyId(String s3AccessKeyId) {
            this.s3AccessKeyId = s3AccessKeyId;
        }

        public String getS3SecretAccessKey() {
            return s3SecretAccessKey;
        }

        public void setS3SecretAccessKey(String s3SecretAccessKey) {
            this.s3SecretAccessKey = s3SecretAccessKey;
        }

        public String getPublicBaseUrl() {
            return publicBaseUrl;
        }

        public void setPublicBaseUrl(String publicBaseUrl) {
            this.publicBaseUrl = publicBaseUrl;
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
