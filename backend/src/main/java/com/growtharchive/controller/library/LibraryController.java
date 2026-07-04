package com.growtharchive.controller.library;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.service.book.BookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/library")
public class LibraryController {

    private final BookService bookService;

    public LibraryController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ApiResponse<BookService.LibraryResponse> getLibrary() {
        return ApiResponse.success(bookService.getLibrary());
    }
}
