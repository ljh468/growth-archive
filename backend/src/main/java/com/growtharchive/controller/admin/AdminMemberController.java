package com.growtharchive.controller.admin;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.admin.AdminMemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    public AdminMemberController(AdminMemberService adminMemberService) {
        this.adminMemberService = adminMemberService;
    }

    @GetMapping("/members")
    public ApiResponse<List<AdminMemberService.MemberSummary>> members(
        HttpServletRequest request,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "20") Integer size
    ) {
        return ApiResponse.success(adminMemberService.list(
            request,
            keyword,
            page == null ? 0 : page,
            size == null ? 20 : size
        ));
    }

    @GetMapping("/members/{memberId}")
    public ApiResponse<AdminMemberService.MemberSummary> member(HttpServletRequest request, @PathVariable Long memberId) {
        return ApiResponse.success(adminMemberService.detail(request, memberId));
    }

    @PostMapping("/members/{memberId}/deactivate")
    public ApiResponse<Void> deactivate(HttpServletRequest request, @PathVariable Long memberId) {
        adminMemberService.deactivate(request, memberId);
        return new ApiResponse<>(true, null, "회원이 비활성화되었습니다.", null);
    }

    @PostMapping("/members/{memberId}/reactivate")
    public ApiResponse<Void> reactivate(HttpServletRequest request, @PathVariable Long memberId) {
        adminMemberService.reactivate(request, memberId);
        return new ApiResponse<>(true, null, "회원이 재활성화되었습니다.", null);
    }

    @PutMapping("/members/{memberId}/participation-start-month")
    public ApiResponse<AdminMemberService.MemberSummary> updateParticipationStartMonth(
        HttpServletRequest request,
        @PathVariable Long memberId,
        @Valid @RequestBody UpdateParticipationStartMonthRequest body
    ) {
        return ApiResponse.success(adminMemberService.updateParticipationStartMonth(
            request,
            memberId,
            LocalDate.parse(body.participationStartMonth() + "-01")
        ));
    }

    @GetMapping("/invite-code")
    public ApiResponse<AdminMemberService.InviteCodeResponse> activeInviteCode(HttpServletRequest request) {
        return ApiResponse.success(adminMemberService.activeInviteCode(request));
    }

    @PutMapping("/invite-code")
    public ApiResponse<Void> updateInviteCode(
        HttpServletRequest request,
        @Valid @RequestBody UpdateInviteCodeRequest body
    ) {
        adminMemberService.updateInviteCode(request, body.code());
        return new ApiResponse<>(true, null, "초대코드가 변경되었습니다.", null);
    }

    public record UpdateInviteCodeRequest(
        @NotBlank(message = "초대코드를 입력해 주세요.")
        String code
    ) {
    }

    public record UpdateParticipationStartMonthRequest(
        @NotBlank(message = "참여 시작월을 입력해 주세요.")
        @Pattern(regexp = "\\d{4}-\\d{2}", message = "참여 시작월은 YYYY-MM 형식입니다.")
        String participationStartMonth
    ) {
    }
}
