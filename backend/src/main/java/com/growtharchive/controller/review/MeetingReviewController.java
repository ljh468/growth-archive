package com.growtharchive.controller.review;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.review.MeetingReviewCommand;
import com.growtharchive.service.review.MeetingReviewDetail;
import com.growtharchive.service.review.MeetingReviewService;
import com.growtharchive.service.review.MeetingReviewSummary;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
public class MeetingReviewController {

    private final MeetingReviewService meetingReviewService;

    public MeetingReviewController(MeetingReviewService meetingReviewService) {
        this.meetingReviewService = meetingReviewService;
    }

    @GetMapping
    public ApiResponse<List<MeetingReviewSummary>> list(
        @RequestParam(required = false) Long meetingId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(meetingReviewService.list(meetingId, page, size));
    }

    @GetMapping("/{reviewId}")
    public ApiResponse<MeetingReviewDetail> detail(HttpServletRequest request, @PathVariable Long reviewId) {
        return ApiResponse.success(meetingReviewService.detail(request, reviewId));
    }

    @PostMapping
    public ApiResponse<MeetingReviewDetail> create(HttpServletRequest request, @Valid @RequestBody MeetingReviewRequest body) {
        return ApiResponse.success(meetingReviewService.create(request, body.toCommand()));
    }

    @PutMapping("/{reviewId}")
    public ApiResponse<MeetingReviewDetail> update(
        HttpServletRequest request,
        @PathVariable Long reviewId,
        @Valid @RequestBody MeetingReviewRequest body
    ) {
        return ApiResponse.success(meetingReviewService.update(request, reviewId, body.toCommand()));
    }

    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable Long reviewId) {
        meetingReviewService.deleteByAuthor(request, reviewId);
        return ApiResponse.success(null);
    }

    public record MeetingReviewRequest(
        @NotNull Long meetingId,
        @NotBlank String title,
        @NotBlank String content,
        List<Long> imageIds
    ) {
        MeetingReviewCommand toCommand() {
            return new MeetingReviewCommand(meetingId, title, content, imageIds);
        }
    }
}
