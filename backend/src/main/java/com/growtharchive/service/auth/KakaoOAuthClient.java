package com.growtharchive.service.auth;

import com.growtharchive.config.properties.AppProperties;
import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class KakaoOAuthClient {

    private static final Logger log = LoggerFactory.getLogger(KakaoOAuthClient.class);
    private static final String AUTHORIZATION_CODE_GRANT = "authorization_code";
    private static final String RESPONSE_TYPE_CODE = "code";
    private static final String KAKAO_ADMIN_AUTHORIZATION_SCHEME = "KakaoAK ";
    private static final String BEARER_AUTHORIZATION_SCHEME = "Bearer ";

    private final AppProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Autowired
    public KakaoOAuthClient(AppProperties properties, ObjectMapper objectMapper) {
        this(properties, objectMapper, RestClient.builder().build());
    }

    KakaoOAuthClient(AppProperties properties, ObjectMapper objectMapper, RestClient restClient) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    public String authorizationUrl(String state) {
        if (properties.getKakao().getClientId().isBlank()
            || properties.getKakao().getRedirectUri().isBlank()
            || properties.getKakao().getAuthorizationUri().isBlank()) {
            throw new ApiException(ErrorCode.KAKAO_NOT_CONFIGURED);
        }
        return properties.getKakao().getAuthorizationUri()
            + "?response_type=" + RESPONSE_TYPE_CODE
            + "&client_id=" + encode(properties.getKakao().getClientId())
            + "&redirect_uri=" + encode(properties.getKakao().getRedirectUri())
            + "&state=" + encode(state);
    }

    public KakaoUserProfile fetchProfile(String code) {
        if (properties.getKakao().getClientId().isBlank() || properties.getKakao().getRedirectUri().isBlank()) {
            throw new ApiException(ErrorCode.KAKAO_NOT_CONFIGURED);
        }
        return fetchUserProfile(exchangeAccessToken(code));
    }

    public boolean unlinkProviderUser(String providerUserId) {
        if (providerUserId == null || providerUserId.isBlank() || properties.getKakao().getAdminKey().isBlank()) {
            return false;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("target_id_type", "user_id");
        form.add("target_id", providerUserId);
        try {
            restClient.post()
                .uri(properties.getKakao().getUnlinkUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", KAKAO_ADMIN_AUTHORIZATION_SCHEME + properties.getKakao().getAdminKey())
                .body(form)
                .retrieve()
                .body(String.class);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private String exchangeAccessToken(String code) {
        if (code == null || code.isBlank()) {
            throw new ApiException(ErrorCode.KAKAO_AUTH_FAILED);
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", AUTHORIZATION_CODE_GRANT);
        form.add("client_id", properties.getKakao().getClientId());
        form.add("redirect_uri", properties.getKakao().getRedirectUri());
        form.add("code", code);
        if (!properties.getKakao().getClientSecret().isBlank()) {
            form.add("client_secret", properties.getKakao().getClientSecret());
        }
        try {
            String body = restClient.post()
                .uri(properties.getKakao().getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String.class);
            String accessToken = text(objectMapper.readTree(body), "access_token");
            if (accessToken.isBlank()) {
                throw new ApiException(ErrorCode.KAKAO_AUTH_FAILED);
            }
            return accessToken;
        } catch (ApiException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            log.warn(
                "Kakao token exchange failed. status={}, body={}",
                exception.getStatusCode(),
                exception.getResponseBodyAsString()
            );
            throw new ApiException(ErrorCode.KAKAO_AUTH_FAILED);
        } catch (RuntimeException exception) {
            log.warn("Kakao token exchange failed before response. message={}", exception.getMessage());
            throw new ApiException(ErrorCode.KAKAO_AUTH_FAILED);
        }
    }

    private KakaoUserProfile fetchUserProfile(String accessToken) {
        try {
            String body = restClient.get()
                .uri(properties.getKakao().getUserInfoUri())
                .header("Authorization", BEARER_AUTHORIZATION_SCHEME + accessToken)
                .retrieve()
                .body(String.class);
            JsonNode root = objectMapper.readTree(body);
            String providerUserId = text(root, "id");
            if (providerUserId.isBlank()) {
                throw new ApiException(ErrorCode.KAKAO_AUTH_FAILED);
            }
            JsonNode kakaoAccount = root.get("kakao_account");
            JsonNode propertiesNode = root.get("properties");
            JsonNode profile = kakaoAccount == null ? null : kakaoAccount.get("profile");
            String nickname = firstNonBlank(
                text(propertiesNode, "nickname"),
                text(profile, "nickname")
            );
            String profileImageUrl = firstNonBlank(
                text(propertiesNode, "profile_image"),
                text(propertiesNode, "thumbnail_image"),
                text(profile, "profile_image_url"),
                text(profile, "thumbnail_image_url")
            );
            return new KakaoUserProfile(
                providerUserId,
                blankToNull(text(kakaoAccount, "email")),
                blankToNull(nickname),
                profileImageUrl.isBlank() ? null : profileImageUrl
            );
        } catch (ApiException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            log.warn(
                "Kakao user profile fetch failed. status={}, body={}",
                exception.getStatusCode(),
                exception.getResponseBodyAsString()
            );
            throw new ApiException(ErrorCode.KAKAO_AUTH_FAILED);
        } catch (RuntimeException exception) {
            log.warn("Kakao user profile fetch failed before response. message={}", exception.getMessage());
            throw new ApiException(ErrorCode.KAKAO_AUTH_FAILED);
        }
    }

    private String text(JsonNode node, String fieldName) {
        if (node == null) {
            return "";
        }
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return "";
        }
        String text = value.asString();
        return text == null ? "" : text;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
