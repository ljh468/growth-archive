package com.growtharchive.security;

import com.growtharchive.config.properties.AppProperties;
import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class JwtService {

    public enum TokenType {
        ACCESS,
        REFRESH
    }

    private static final String SIGNUP_TOKEN_TYPE = "SIGNUP";

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final AppProperties properties;
    private final ObjectMapper objectMapper;

    public JwtService(AppProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String issue(Long memberId, TokenType tokenType) {
        long now = Instant.now().getEpochSecond();
        long ttl = tokenType == TokenType.ACCESS
            ? properties.getJwt().getAccessTokenSeconds()
            : properties.getJwt().getRefreshTokenSeconds();
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", memberId.toString());
        payload.put("typ", tokenType.name());
        payload.put("iat", now);
        payload.put("exp", now + ttl);

        try {
            String headerPart = encodeJson(header);
            String payloadPart = encodeJson(payload);
            String signaturePart = sign(headerPart + "." + payloadPart);
            return headerPart + "." + payloadPart + "." + signaturePart;
        } catch (Exception exception) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR);
        }
    }

    public String issueSignup(SignupToken signupToken) {
        long now = Instant.now().getEpochSecond();
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", signupToken.providerUserId());
        payload.put("typ", SIGNUP_TOKEN_TYPE);
        payload.put("provider", signupToken.provider());
        payload.put("email", signupToken.email());
        payload.put("nickname", signupToken.nickname());
        payload.put("profileImageUrl", signupToken.profileImageUrl());
        payload.put("inviteVerified", signupToken.inviteVerified());
        payload.put("inviteRole", signupToken.inviteRole());
        payload.put("termsAgreed", signupToken.termsAgreed());
        payload.put("privacyAgreed", signupToken.privacyAgreed());
        payload.put("iat", now);
        payload.put("exp", now + properties.getJwt().getRefreshTokenSeconds());

        try {
            String headerPart = encodeJson(header);
            String payloadPart = encodeJson(payload);
            String signaturePart = sign(headerPart + "." + payloadPart);
            return headerPart + "." + payloadPart + "." + signaturePart;
        } catch (Exception exception) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR);
        }
    }

    public Long verify(String token, TokenType expectedType) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new ApiException(ErrorCode.UNAUTHORIZED);
            }
            String expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!constantTimeEquals(expectedSignature, parts[2])) {
                throw new ApiException(ErrorCode.UNAUTHORIZED);
            }
            Map<String, Object> payload = objectMapper.readValue(
                URL_DECODER.decode(parts[1]),
                new TypeReference<>() {
                }
            );
            if (!expectedType.name().equals(payload.get("typ"))) {
                throw new ApiException(ErrorCode.UNAUTHORIZED);
            }
            Number exp = (Number) payload.get("exp");
            if (exp.longValue() < Instant.now().getEpochSecond()) {
                throw new ApiException(ErrorCode.UNAUTHORIZED);
            }
            return Long.valueOf((String) payload.get("sub"));
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
    }

    public SignupToken verifySignup(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new ApiException(ErrorCode.UNAUTHORIZED);
            }
            String expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!constantTimeEquals(expectedSignature, parts[2])) {
                throw new ApiException(ErrorCode.UNAUTHORIZED);
            }
            Map<String, Object> payload = objectMapper.readValue(
                URL_DECODER.decode(parts[1]),
                new TypeReference<>() {
                }
            );
            if (!SIGNUP_TOKEN_TYPE.equals(payload.get("typ"))) {
                throw new ApiException(ErrorCode.UNAUTHORIZED);
            }
            Number exp = (Number) payload.get("exp");
            if (exp.longValue() < Instant.now().getEpochSecond()) {
                throw new ApiException(ErrorCode.UNAUTHORIZED);
            }
            return new SignupToken(
                readText(payload, "provider"),
                readText(payload, "sub"),
                readText(payload, "email"),
                readText(payload, "nickname"),
                readText(payload, "profileImageUrl"),
                Boolean.TRUE.equals(payload.get("inviteVerified")),
                readText(payload, "inviteRole") == null ? "MEMBER" : readText(payload, "inviteRole"),
                Boolean.TRUE.equals(payload.get("termsAgreed")),
                Boolean.TRUE.equals(payload.get("privacyAgreed"))
            );
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return URL_ENCODER.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private boolean constantTimeEquals(String left, String right) {
        return MessageDigestUtil.equals(
            left.getBytes(StandardCharsets.UTF_8),
            right.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String readText(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? null : value.toString();
    }

    public record SignupToken(
        String provider,
        String providerUserId,
        String email,
        String nickname,
        String profileImageUrl,
        boolean inviteVerified,
        String inviteRole,
        boolean termsAgreed,
        boolean privacyAgreed
    ) {
    }

    private static final class MessageDigestUtil {
        private static boolean equals(byte[] left, byte[] right) {
            if (left.length != right.length) {
                return false;
            }
            int result = 0;
            for (int i = 0; i < left.length; i++) {
                result |= left[i] ^ right[i];
            }
            return result == 0;
        }
    }
}
