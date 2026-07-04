package com.growtharchive.config;

import com.growtharchive.config.properties.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class SecurityConfig {

    private final AppProperties properties;

    public SecurityConfig(AppProperties properties) {
        this.properties = properties;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new UnsafeMethodOriginFilter(allowedOrigins()), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/health", "/actuator/health").permitAll()
                .anyRequest().permitAll()
            );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException(username);
        };
    }

    private List<String> allowedOrigins() {
        return Arrays.stream(properties.getCors().getAllowedOrigins().split(","))
            .map(String::trim)
            .filter(origin -> !origin.isBlank())
            .toList();
    }

    private static class UnsafeMethodOriginFilter extends OncePerRequestFilter {

        private final List<String> allowedOrigins;

        private UnsafeMethodOriginFilter(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        @Override
        protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
        ) throws ServletException, IOException {
            if (isUnsafe(request) && !isAllowed(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            filterChain.doFilter(request, response);
        }

        private boolean isUnsafe(HttpServletRequest request) {
            String method = request.getMethod();
            return !(HttpMethod.GET.matches(method)
                || HttpMethod.HEAD.matches(method)
                || HttpMethod.OPTIONS.matches(method));
        }

        private boolean isAllowed(HttpServletRequest request) {
            if ("/api/v1/auth/kakao/unlink-webhook".equals(request.getRequestURI())) {
                return true;
            }
            String origin = request.getHeader("Origin");
            if (origin != null && allowedOrigins.contains(origin)) {
                return true;
            }
            String referer = request.getHeader("Referer");
            if (referer == null) {
                return false;
            }
            try {
                URI uri = URI.create(referer);
                String refererOrigin = uri.getScheme() + "://" + uri.getAuthority();
                return allowedOrigins.contains(refererOrigin);
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }
    }
}
