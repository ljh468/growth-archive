package com.growtharchive.controller.admin;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.admin.AdminInterestTagService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/interest-tags")
public class AdminInterestTagController {

    private final AdminInterestTagService adminInterestTagService;

    public AdminInterestTagController(AdminInterestTagService adminInterestTagService) {
        this.adminInterestTagService = adminInterestTagService;
    }

    @GetMapping
    public ApiResponse<List<AdminInterestTagService.InterestTagView>> list(HttpServletRequest request) {
        return ApiResponse.success(adminInterestTagService.list(request));
    }

    @PostMapping
    public ApiResponse<AdminInterestTagService.InterestTagView> create(
        HttpServletRequest request,
        @Valid @RequestBody InterestTagRequest body
    ) {
        return ApiResponse.success(adminInterestTagService.create(request, body.toCommand()));
    }

    @PutMapping("/{tagId}")
    public ApiResponse<AdminInterestTagService.InterestTagView> update(
        HttpServletRequest request,
        @PathVariable Long tagId,
        @Valid @RequestBody InterestTagRequest body
    ) {
        return ApiResponse.success(adminInterestTagService.update(request, tagId, body.toCommand()));
    }

    @DeleteMapping("/{tagId}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable Long tagId) {
        adminInterestTagService.deactivate(request, tagId);
        return ApiResponse.success(null);
    }

    public record InterestTagRequest(
        @NotBlank(message = "태그명을 입력해 주세요.")
        @Size(max = 50, message = "태그명은 50자 이하입니다.")
        String name,
        @Size(max = 80, message = "slug는 80자 이하입니다.")
        String slug,
        Integer displayOrder,
        Boolean active
    ) {
        AdminInterestTagService.InterestTagCommand toCommand() {
            return new AdminInterestTagService.InterestTagCommand(name, slug, displayOrder, active);
        }
    }
}
