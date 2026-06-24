package com.growtharchive.controller.upload;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.review.MeetingReviewService;
import com.growtharchive.service.review.UploadedImageView;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {

    private final MeetingReviewService meetingReviewService;

    public UploadController(MeetingReviewService meetingReviewService) {
        this.meetingReviewService = meetingReviewService;
    }

    @PostMapping("/review-images")
    public ApiResponse<List<UploadedImageView>> uploadReviewImages(
        HttpServletRequest request,
        @RequestParam("files") List<MultipartFile> files
    ) {
        return ApiResponse.success(meetingReviewService.uploadReviewImages(request, files));
    }
}
