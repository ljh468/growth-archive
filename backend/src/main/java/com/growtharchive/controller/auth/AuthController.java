package com.growtharchive.controller.auth;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/kakao/login")
    public ResponseEntity<Void> kakaoLogin(
        @RequestParam(defaultValue = "") String state,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        return ResponseEntity.status(302).location(authService.kakaoLoginUri(state, request, response)).build();
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
}
