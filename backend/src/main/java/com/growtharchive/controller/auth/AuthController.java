package com.growtharchive.controller.auth;

import com.growtharchive.config.properties.AppProperties;
import com.growtharchive.dto.ApiResponse;
import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import com.growtharchive.service.auth.AccountWithdrawalService;
import com.growtharchive.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AccountWithdrawalService accountWithdrawalService;
    private final AppProperties properties;

    public AuthController(
        AuthService authService,
        AccountWithdrawalService accountWithdrawalService,
        AppProperties properties
    ) {
        this.authService = authService;
        this.accountWithdrawalService = accountWithdrawalService;
        this.properties = properties;
    }

    @GetMapping("/kakao/login")
    public ResponseEntity<Void> kakaoLogin(
        @RequestParam(defaultValue = "") String state
    ) {
        return ResponseEntity.status(302).location(authService.kakaoLoginUri(state)).build();
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<Void> kakaoCallback(
        @RequestParam String code,
        HttpServletResponse response
    ) {
        URI redirectUri = authService.handleKakaoCallback(code, response);
        return ResponseEntity.status(302).location(redirectUri).build();
    }

    @GetMapping("/me")
    public ApiResponse<AuthService.MeResponse> me(HttpServletRequest request) {
        return ApiResponse.success(authService.me(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        authService.refresh(request, response);
        return new ApiResponse<>(true, null, "인증이 갱신되었습니다.", null);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return new ApiResponse<>(true, null, "로그아웃되었습니다.", null);
    }

    @PostMapping("/kakao/unlink-webhook")
    public ApiResponse<Void> kakaoUnlinkWebhook(
        @RequestHeader(value = "X-Growth-Archive-Webhook-Secret", required = false) String webhookSecret,
        @RequestBody Map<String, Object> body
    ) {
        validateWebhookSecret(webhookSecret);
        accountWithdrawalService.withdrawByKakaoProviderUserId(readProviderUserId(body));
        return new ApiResponse<>(true, null, "처리되었습니다.", null);
    }

    private void validateWebhookSecret(String webhookSecret) {
        String expected = properties.getKakao().getWebhookSecret();
        if (expected != null && !expected.isBlank() && !expected.equals(webhookSecret)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
    }

    private String readProviderUserId(Map<String, Object> body) {
        Object userId = body.get("user_id");
        if (userId == null) {
            userId = body.get("id");
        }
        return userId == null ? null : userId.toString();
    }
}
