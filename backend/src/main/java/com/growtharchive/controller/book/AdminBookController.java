package com.growtharchive.controller.book;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.book.BookService;
import com.growtharchive.service.book.BookSummary;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/books")
public class AdminBookController {

    private final BookService bookService;

    public AdminBookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ApiResponse<List<BookSummary>> list(
        HttpServletRequest request,
        @RequestParam(required = false) String verificationStatus,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(bookService.getAdminBooks(request, verificationStatus, page, size));
    }

    @PutMapping("/{bookId}")
    public ApiResponse<BookSummary> update(
        HttpServletRequest request,
        @PathVariable Long bookId,
        @Valid @RequestBody AdminBookRequest body
    ) {
        return ApiResponse.success(bookService.updateBookByAdmin(request, bookId, body.toCommand()));
    }

    @PostMapping("/{bookId}/verify")
    public ApiResponse<BookSummary> verify(HttpServletRequest request, @PathVariable Long bookId) {
        return ApiResponse.success(bookService.verifyBookByAdmin(request, bookId));
    }

    public record AdminBookRequest(
        @NotBlank(message = "책 제목을 입력해 주세요.")
        @Size(max = 255, message = "책 제목은 255자 이하입니다.")
        String title,
        @NotBlank(message = "저자를 입력해 주세요.")
        @Size(max = 255, message = "저자는 255자 이하입니다.")
        String authorsText,
        @Size(max = 120, message = "출판사는 120자 이하입니다.")
        String publisher,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate publishedDate,
        String thumbnailUrl
    ) {
        BookService.AdminBookCommand toCommand() {
            return new BookService.AdminBookCommand(title, authorsText, publisher, publishedDate, thumbnailUrl);
        }
    }
}
