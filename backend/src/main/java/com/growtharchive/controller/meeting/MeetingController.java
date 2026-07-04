package com.growtharchive.controller.meeting;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.meeting.MeetingCommand;
import com.growtharchive.service.meeting.MeetingDetail;
import com.growtharchive.service.meeting.MeetingService;
import com.growtharchive.service.meeting.MeetingSummary;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
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
@RequestMapping("/api/v1/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @GetMapping
    public ApiResponse<List<MeetingSummary>> list(
        @RequestParam(required = false) String type,
        @RequestParam(defaultValue = "current") String scope,
        @RequestParam(defaultValue = "0") int monthOffset,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(meetingService.list(type, scope, monthOffset, page, size));
    }

    @GetMapping("/{meetingId}")
    public ApiResponse<MeetingDetail> detail(HttpServletRequest request, @PathVariable Long meetingId) {
        return ApiResponse.success(meetingService.detail(request, meetingId));
    }

    @PostMapping
    public ApiResponse<MeetingDetail> createSmall(
        HttpServletRequest request,
        @Valid @RequestBody MeetingRequest body
    ) {
        return ApiResponse.success(meetingService.createSmall(request, body.toCommand()));
    }

    @PutMapping("/{meetingId}")
    public ApiResponse<MeetingDetail> updateSmall(
        HttpServletRequest request,
        @PathVariable Long meetingId,
        @Valid @RequestBody MeetingRequest body
    ) {
        return ApiResponse.success(meetingService.updateSmall(request, meetingId, body.toCommand()));
    }

    @DeleteMapping("/{meetingId}")
    public ApiResponse<Void> deleteSmall(HttpServletRequest request, @PathVariable Long meetingId) {
        meetingService.deleteByCreator(request, meetingId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{meetingId}/join")
    public ApiResponse<MeetingDetail> join(HttpServletRequest request, @PathVariable Long meetingId) {
        return ApiResponse.success(meetingService.join(request, meetingId));
    }

    @DeleteMapping("/{meetingId}/join")
    public ApiResponse<MeetingDetail> cancel(HttpServletRequest request, @PathVariable Long meetingId) {
        return ApiResponse.success(meetingService.cancel(request, meetingId));
    }

    public record MeetingRequest(
        @NotBlank String title,
        String description,
        @NotNull OffsetDateTime meetingAt,
        @NotBlank String locationRegion,
        String exactLocation,
        Integer capacity,
        Long thumbnailImageId,
        Integer feeAmount,
        String status
    ) {
        MeetingCommand toCommand() {
            return new MeetingCommand(
                title,
                description,
                meetingAt,
                locationRegion,
                exactLocation,
                capacity,
                thumbnailImageId,
                feeAmount,
                status
            );
        }
    }
}
