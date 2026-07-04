package com.growtharchive.controller.participation;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.participation.ParticipationService;
import com.growtharchive.service.participation.ParticipationStatusView;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/participation")
public class ParticipationController {

    private final ParticipationService participationService;

    public ParticipationController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @GetMapping("/{month}")
    public ApiResponse<ParticipationStatusView> getMyStatus(HttpServletRequest request, @PathVariable String month) {
        return ApiResponse.success(participationService.getMyStatus(request, LocalDate.parse(month + "-01")));
    }
}
