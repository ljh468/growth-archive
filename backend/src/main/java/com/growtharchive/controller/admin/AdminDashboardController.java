package com.growtharchive.controller.admin;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.admin.AdminDashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping
    public ApiResponse<AdminDashboardService.AdminDashboardView> dashboard(HttpServletRequest request) {
        return ApiResponse.success(adminDashboardService.getDashboard(request));
    }
}
