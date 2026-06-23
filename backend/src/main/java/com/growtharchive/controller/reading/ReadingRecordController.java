package com.growtharchive.controller.reading;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.reading.ReadingRecordDetail;
import com.growtharchive.service.reading.ReadingRecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@RequestMapping("/api/v1/reading-records")
public class ReadingRecordController {

    private final ReadingRecordService readingRecordService;

    public ReadingRecordController(ReadingRecordService readingRecordService) {
        this.readingRecordService = readingRecordService;
    }

    @GetMapping
    public ApiResponse<List<ReadingRecordDetail>> list(
        @RequestParam(required = false) Long bookId,
        @RequestParam(required = false) Long memberId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(readingRecordService.getPublic(bookId, memberId, page, size));
    }

    @PostMapping
    public ApiResponse<ReadingRecordDetail> create(
        HttpServletRequest request,
        @Valid @RequestBody ReadingRecordRequest body
    ) {
        return ApiResponse.success(readingRecordService.create(request, body.toCommand()));
    }

    @PutMapping("/{readingRecordId}")
    public ApiResponse<ReadingRecordDetail> update(
        HttpServletRequest request,
        @PathVariable Long readingRecordId,
        @Valid @RequestBody ReadingRecordRequest body
    ) {
        return ApiResponse.success(readingRecordService.update(request, readingRecordId, body.toCommand()));
    }

    @DeleteMapping("/{readingRecordId}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable Long readingRecordId) {
        readingRecordService.deleteByAuthor(request, readingRecordId);
        return ApiResponse.success(null);
    }

    public record ReadingRecordRequest(
        @NotNull(message = "책을 선택해 주세요.")
        Long bookId,
        @Min(value = 1, message = "평점은 1~5 정수만 입력할 수 있습니다.")
        @Max(value = 5, message = "평점은 1~5 정수만 입력할 수 있습니다.")
        Integer rating,
        @NotBlank(message = "한줄평을 입력해 주세요.")
        @Size(max = 300, message = "한줄평은 300자 이하입니다.")
        String oneLineReview,
        @NotBlank(message = "블로그 URL을 입력해 주세요.")
        String blogUrl,
        Long imageId
    ) {
        ReadingRecordService.ReadingRecordCommand toCommand() {
            return new ReadingRecordService.ReadingRecordCommand(bookId, rating, oneLineReview, blogUrl, imageId);
        }
    }
}
