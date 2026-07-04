package com.growtharchive.controller.participation;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.participation.AdminParticipationSummary;
import com.growtharchive.service.participation.ParticipationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminParticipationController {

    private final ParticipationService participationService;

    public AdminParticipationController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @GetMapping("/participation")
    public ApiResponse<AdminParticipationSummary> getAdminStatus(
        HttpServletRequest request,
        @RequestParam String month
    ) {
        return ApiResponse.success(participationService.getAdminStatus(request, LocalDate.parse(month + "-01")));
    }

    @GetMapping("/participation.csv")
    public ResponseEntity<byte[]> csv(HttpServletRequest request, @RequestParam String month) {
        byte[] csv = participationService.getAdminCsv(request, LocalDate.parse(month + "-01"));
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("participation-" + month + ".csv").build().toString())
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csv);
    }

    @PutMapping("/participation/{month}/members/{memberId}/note")
    public ApiResponse<Void> saveNote(
        HttpServletRequest request,
        @PathVariable String month,
        @PathVariable Long memberId,
        @Valid @RequestBody ParticipationNoteRequest body
    ) {
        participationService.saveAdminNote(request, memberId, LocalDate.parse(month + "-01"), body.note());
        return ApiResponse.success(null);
    }

    @PostMapping("/participation/{month}/members/{memberId}/manual-completion")
    public ApiResponse<Void> markManualCompletion(
        HttpServletRequest request,
        @PathVariable String month,
        @PathVariable Long memberId
    ) {
        participationService.markManualCompletion(request, memberId, LocalDate.parse(month + "-01"));
        return new ApiResponse<>(true, null, "참여 처리되었습니다.", null);
    }

    @DeleteMapping("/participation/{month}/members/{memberId}/manual-completion")
    public ApiResponse<Void> clearManualCompletion(
        HttpServletRequest request,
        @PathVariable String month,
        @PathVariable Long memberId
    ) {
        participationService.clearManualCompletion(request, memberId, LocalDate.parse(month + "-01"));
        return new ApiResponse<>(true, null, "참여 처리가 취소되었습니다.", null);
    }

    public record ParticipationNoteRequest(String note) {
    }
}
