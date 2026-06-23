package com.growtharchive.controller.reading;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.reading.ReadingRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reading-records")
public class AdminReadingRecordController {

    private final ReadingRecordService readingRecordService;

    public AdminReadingRecordController(ReadingRecordService readingRecordService) {
        this.readingRecordService = readingRecordService;
    }

    @PostMapping("/{readingRecordId}/hide")
    public ApiResponse<Void> hide(HttpServletRequest request, @PathVariable Long readingRecordId) {
        readingRecordService.hideByAdmin(request, readingRecordId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{readingRecordId}/restore")
    public ApiResponse<Void> restore(HttpServletRequest request, @PathVariable Long readingRecordId) {
        readingRecordService.restoreByAdmin(request, readingRecordId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{readingRecordId}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable Long readingRecordId) {
        readingRecordService.deleteByAdmin(request, readingRecordId);
        return ApiResponse.success(null);
    }
}
