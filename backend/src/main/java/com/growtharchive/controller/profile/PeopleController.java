package com.growtharchive.controller.profile;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.profile.ProfileCard;
import com.growtharchive.service.profile.ProfileDetail;
import com.growtharchive.service.profile.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/people")
public class PeopleController {

    private final ProfileService profileService;

    public PeopleController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ApiResponse<List<ProfileCard>> list(
        HttpServletRequest request,
        @RequestParam(required = false) Long interestTagId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(profileService.getPeople(request, interestTagId, page, size));
    }

    @GetMapping("/{memberId}")
    public ApiResponse<ProfileDetail> detail(HttpServletRequest request, @PathVariable Long memberId) {
        return ApiResponse.success(profileService.getProfile(request, memberId));
    }
}
