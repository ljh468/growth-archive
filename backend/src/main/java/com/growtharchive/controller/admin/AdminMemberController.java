package com.growtharchive.controller.admin;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.admin.AdminMemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    public AdminMemberController(AdminMemberService adminMemberService) {
        this.adminMemberService = adminMemberService;
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
}
