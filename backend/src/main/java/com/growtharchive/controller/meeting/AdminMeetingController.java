package com.growtharchive.controller.meeting;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.meeting.MeetingDetail;
import com.growtharchive.service.meeting.MeetingService;
import com.growtharchive.service.meeting.MeetingSummary;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/admin/meetings")
public class AdminMeetingController {

    private final MeetingService meetingService;

    public AdminMeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @GetMapping
    public ApiResponse<List<MeetingSummary>> list(
        HttpServletRequest request,
        @RequestParam(required = false) String type,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        return ApiResponse.success(meetingService.adminList(request, type, page, size));
    }

    @GetMapping("/{meetingId}")
    public ApiResponse<MeetingDetail> detail(HttpServletRequest request, @PathVariable Long meetingId) {
        return ApiResponse.success(meetingService.adminDetail(request, meetingId));
    }

    @PutMapping("/{meetingId}")
    public ApiResponse<MeetingDetail> updateRegular(
        HttpServletRequest request,
        @PathVariable Long meetingId,
        @Valid @RequestBody MeetingController.MeetingRequest body
    ) {
        return ApiResponse.success(meetingService.updateRegular(request, meetingId, body.toCommand()));
    }

    @PostMapping("/{meetingId}/hide")
    public ApiResponse<Void> hide(HttpServletRequest request, @PathVariable Long meetingId) {
        meetingService.hide(request, meetingId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{meetingId}/restore")
    public ApiResponse<Void> restore(HttpServletRequest request, @PathVariable Long meetingId) {
        meetingService.restore(request, meetingId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{meetingId}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable Long meetingId) {
        meetingService.deleteByAdmin(request, meetingId);
        return ApiResponse.success(null);
    }
}
