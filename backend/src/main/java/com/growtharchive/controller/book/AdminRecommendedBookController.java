package com.growtharchive.controller.book;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.book.BookService;
import com.growtharchive.service.book.RecommendedBookView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/recommended-books")
public class AdminRecommendedBookController {

    private final BookService bookService;

    public AdminRecommendedBookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ApiResponse<RecommendedBookView> create(
        HttpServletRequest request,
        @Valid @RequestBody RecommendedBookRequest body
    ) {
        BookService.RecommendedBookCommand command = new BookService.RecommendedBookCommand(
            body.targetMonth(),
            body.bookId(),
            body.reason(),
            body.displayOrder() == null ? 1 : body.displayOrder()
        );
        return ApiResponse.success(bookService.createRecommended(request, command));
    }

    @DeleteMapping("/{recommendedBookId}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable Long recommendedBookId) {
        bookService.deleteRecommended(request, recommendedBookId);
        return ApiResponse.success(null);
    }

    public record RecommendedBookRequest(
        @NotNull(message = "대상 월을 입력해 주세요.")
        LocalDate targetMonth,
        @NotNull(message = "책을 선택해 주세요.")
        Long bookId,
        @NotBlank(message = "추천 이유를 입력해 주세요.")
        @Size(max = 500, message = "추천 이유는 500자 이하입니다.")
        String reason,
        Integer displayOrder
    ) {
    }
}
