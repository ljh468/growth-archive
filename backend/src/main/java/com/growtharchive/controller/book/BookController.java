package com.growtharchive.controller.book;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.book.BookDetail;
import com.growtharchive.service.book.BookSearchResult;
import com.growtharchive.service.book.BookService;
import com.growtharchive.service.book.BookSummary;
import com.growtharchive.service.reading.ReadingRecordDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ApiResponse<List<BookSummary>> list(@RequestParam(defaultValue = "") String query) {
        return ApiResponse.success(bookService.searchInternal(query, 30));
    }

    @GetMapping("/search")
    public ApiResponse<List<BookSearchResult>> search(
        HttpServletRequest request,
        @RequestParam String query,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(bookService.searchExternal(request, query, page, size));
    }

    @PostMapping("/import")
    public ApiResponse<BookSummary> importBook(
        HttpServletRequest request,
        @Valid @RequestBody ImportBookRequest body
    ) {
        BookSearchResult result = new BookSearchResult(
            "KAKAO",
            body.title(),
            body.authorsText(),
            body.publisher(),
            body.publishedDate(),
            body.thumbnailUrl(),
            body.isbn10(),
            body.isbn13(),
            body.sourcePayload()
        );
        return ApiResponse.success(bookService.importVerified(request, result));
    }

    @PostMapping("/manual")
    public ApiResponse<BookSummary> createManual(
        HttpServletRequest request,
        @Valid @RequestBody ManualBookRequest body
    ) {
        BookService.ManualBookCommand command = new BookService.ManualBookCommand(
            body.title(),
            body.author(),
            body.publisher(),
            body.publishedDate(),
            body.thumbnailUrl()
        );
        return ApiResponse.success(bookService.createManual(request, command));
    }

    @GetMapping("/{bookId}")
    public ApiResponse<BookDetailResponse> detail(@PathVariable Long bookId) {
        BookService.BookDetailResponse detail = bookService.getDetail(bookId);
        return ApiResponse.success(new BookDetailResponse(detail.book(), detail.readingRecords()));
    }

    public record ManualBookRequest(
        @NotBlank(message = "책 제목을 입력해 주세요.")
        @Size(max = 255, message = "책 제목은 255자 이하입니다.")
        String title,
        @NotBlank(message = "저자를 입력해 주세요.")
        @Size(max = 255, message = "저자는 255자 이하입니다.")
        String author,
        @Size(max = 120, message = "출판사는 120자 이하입니다.")
        String publisher,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate publishedDate,
        String thumbnailUrl
    ) {
    }

    public record ImportBookRequest(
        @NotBlank(message = "책 제목을 입력해 주세요.")
        String title,
        @NotBlank(message = "저자를 입력해 주세요.")
        String authorsText,
        String publisher,
        LocalDate publishedDate,
        String thumbnailUrl,
        String isbn10,
        String isbn13,
        @NotNull(message = "원본 검색 결과가 필요합니다.")
        String sourcePayload
    ) {
    }

    public record BookDetailResponse(BookDetail book, List<ReadingRecordDetail> readingRecords) {
    }
}
