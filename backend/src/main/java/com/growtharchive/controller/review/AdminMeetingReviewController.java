package com.growtharchive.controller.review;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.review.MeetingReviewDetail;
import com.growtharchive.service.review.MeetingReviewService;
import com.growtharchive.service.review.MeetingReviewSummary;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reviews")
public class AdminMeetingReviewController {

    private final MeetingReviewService meetingReviewService;

    public AdminMeetingReviewController(MeetingReviewService meetingReviewService) {
        this.meetingReviewService = meetingReviewService;
    }

    @GetMapping
    public ApiResponse<List<MeetingReviewSummary>> list(
        HttpServletRequest request,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "100") int size
    ) {
        return ApiResponse.success(meetingReviewService.adminList(request, page, size));
    }

    @GetMapping("/{reviewId}")
    public ApiResponse<MeetingReviewDetail> detail(HttpServletRequest request, @PathVariable Long reviewId) {
        return ApiResponse.success(meetingReviewService.adminDetail(request, reviewId));
    }

    @PostMapping("/{reviewId}/hide")
    public ApiResponse<Void> hide(HttpServletRequest request, @PathVariable Long reviewId) {
        meetingReviewService.hide(request, reviewId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{reviewId}/restore")
    public ApiResponse<Void> restore(HttpServletRequest request, @PathVariable Long reviewId) {
        meetingReviewService.restore(request, reviewId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable Long reviewId) {
        meetingReviewService.deleteByAdmin(request, reviewId);
        return ApiResponse.success(null);
    }
}
