package com.growtharchive.security;

import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import com.growtharchive.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class CurrentMemberResolver {

    private final AuthCookieService cookieService;
    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final AccessLevelCalculator accessLevelCalculator;

    public CurrentMemberResolver(
        AuthCookieService cookieService,
        JwtService jwtService,
        MemberRepository memberRepository,
        AccessLevelCalculator accessLevelCalculator
    ) {
        this.cookieService = cookieService;
        this.jwtService = jwtService;
        this.memberRepository = memberRepository;
        this.accessLevelCalculator = accessLevelCalculator;
    }

    public MemberPrincipal resolveOptional(HttpServletRequest request) {
        String token = cookieService.readCookie(request, AuthCookieService.ACCESS_TOKEN_COOKIE);
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Long memberId = jwtService.verify(token, JwtService.TokenType.ACCESS);
            return memberRepository.findPrincipalById(memberId).orElse(null);
        } catch (ApiException exception) {
            return null;
        }
    }

    public MemberPrincipal require(HttpServletRequest request, AccessLevel required) {
        MemberPrincipal member = resolveOptional(request);
        AccessLevel actual = accessLevelCalculator.calculate(member);
        if (!accessLevelCalculator.hasAtLeast(actual, required)) {
            throw new ApiException(actual == AccessLevel.PUBLIC ? ErrorCode.UNAUTHORIZED : ErrorCode.FORBIDDEN);
        }
        return member;
    }
}
