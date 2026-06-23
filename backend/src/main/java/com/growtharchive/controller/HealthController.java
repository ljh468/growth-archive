package com.growtharchive.controller;

import com.growtharchive.dto.ApiResponse;
import java.time.OffsetDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ApiResponse<HealthResponse> health() {
        return ApiResponse.success(new HealthResponse("UP", "growth-archive-backend", OffsetDateTime.now()));
    }

    public record HealthResponse(String status, String service, OffsetDateTime checkedAt) {
    }
}
