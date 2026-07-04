package com.growtharchive.controller.monthly;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.monthly.MonthlyActionPlanView;
import com.growtharchive.service.monthly.MonthlyReflectionSlot;
import com.growtharchive.service.monthly.MonthlyReflectionView;
import com.growtharchive.service.monthly.MonthlyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MonthlyController {

    private final MonthlyService monthlyService;

    public MonthlyController(MonthlyService monthlyService) {
        this.monthlyService = monthlyService;
    }

    @GetMapping("/action-plans/{month}")
    public ApiResponse<MonthlyActionPlanView> getActionPlan(HttpServletRequest request, @PathVariable String month) {
        return ApiResponse.success(monthlyService.getActionPlan(request, parseMonth(month)));
    }

    @PostMapping("/action-plans")
    public ApiResponse<MonthlyActionPlanView> createActionPlan(
        HttpServletRequest request,
        @Valid @RequestBody ActionPlanRequest body
    ) {
        return ApiResponse.success(monthlyService.createActionPlan(request, body.toCommand()));
    }

    @PutMapping("/action-plans/{actionPlanId}")
    public ApiResponse<MonthlyActionPlanView> updateActionPlan(
        HttpServletRequest request,
        @PathVariable Long actionPlanId,
        @Valid @RequestBody ActionPlanUpdateRequest body
    ) {
        return ApiResponse.success(monthlyService.updateActionPlan(request, actionPlanId, body.toCommand()));
    }

    @DeleteMapping("/action-plans/{actionPlanId}")
    public ApiResponse<Void> deleteActionPlan(HttpServletRequest request, @PathVariable Long actionPlanId) {
        monthlyService.deleteActionPlan(request, actionPlanId);
        return ApiResponse.success(null);
    }

    @GetMapping("/reflections/{month}")
    public ApiResponse<MonthlyReflectionSlot> getReflection(HttpServletRequest request, @PathVariable String month) {
        return ApiResponse.success(monthlyService.getReflectionSlot(request, parseMonth(month)));
    }

    @PutMapping("/reflections/{month}")
    public ApiResponse<MonthlyReflectionView> saveReflection(
        HttpServletRequest request,
        @PathVariable String month,
        @RequestBody ReflectionRequest body
    ) {
        return ApiResponse.success(monthlyService.saveReflection(request, parseMonth(month), body.toCommand()));
    }

    private LocalDate parseMonth(String month) {
        return LocalDate.parse(month + "-01");
    }

    public record ActionPlanRequest(
        @NotNull(message = "대상 월을 입력해 주세요.")
        String month,
        String title,
        @NotBlank(message = "실행계획 내용을 입력해 주세요.")
        String content
    ) {
        MonthlyService.ActionPlanCommand toCommand() {
            return new MonthlyService.ActionPlanCommand(LocalDate.parse(month + "-01"), title, content);
        }
    }

    public record ActionPlanUpdateRequest(
        String title,
        @NotBlank(message = "실행계획 내용을 입력해 주세요.")
        String content
    ) {
        MonthlyService.ActionPlanUpdateCommand toCommand() {
            return new MonthlyService.ActionPlanUpdateCommand(title, content);
        }
    }

    public record ReflectionRequest(String didWell, String couldImprove, String nextFocus) {
        MonthlyService.ReflectionCommand toCommand() {
            return new MonthlyService.ReflectionCommand(didWell, couldImprove, nextFocus);
        }
    }
}
