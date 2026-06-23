package com.growtharchive.service.book;

import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import com.growtharchive.repository.BookRepository;
import com.growtharchive.repository.ReadingRecordRepository;
import com.growtharchive.repository.RecommendedBookRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import com.growtharchive.service.reading.ReadingRecordDetail;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private static final int MAX_PAGE_SIZE = 50;

    private final CurrentMemberResolver currentMemberResolver;
    private final BookSearchProvider bookSearchProvider;
    private final BookRepository bookRepository;
    private final ReadingRecordRepository readingRecordRepository;
    private final RecommendedBookRepository recommendedBookRepository;

    public BookService(
        CurrentMemberResolver currentMemberResolver,
        BookSearchProvider bookSearchProvider,
        BookRepository bookRepository,
        ReadingRecordRepository readingRecordRepository,
        RecommendedBookRepository recommendedBookRepository
    ) {
        this.currentMemberResolver = currentMemberResolver;
        this.bookSearchProvider = bookSearchProvider;
        this.bookRepository = bookRepository;
        this.readingRecordRepository = readingRecordRepository;
        this.recommendedBookRepository = recommendedBookRepository;
    }

    public List<BookSearchResult> searchExternal(HttpServletRequest request, String query, int page, int size) {
        currentMemberResolver.require(request, AccessLevel.MEMBER);
        validateQuery(query);
        return bookSearchProvider.search(query.trim(), safePage(page), safeSize(size));
    }

    public List<BookSummary> searchInternal(String query, int size) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return bookRepository.searchInternal(query.trim(), safeSize(size));
    }

    public BookDetailResponse getDetail(Long bookId) {
        BookDetail detail = bookRepository.findDetailById(bookId)
            .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        List<ReadingRecordDetail> records = readingRecordRepository.findPublic(bookId, null, 50, 0);
        return new BookDetailResponse(detail, records);
    }

    @Transactional
    public BookSummary createManual(HttpServletRequest request, ManualBookCommand command) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        Long bookId = bookRepository.createManualBook(
            member.memberId(),
            required(command.title(), "책 제목을 입력해 주세요."),
            required(command.author(), "저자를 입력해 주세요."),
            trimToNull(command.publisher()),
            command.publishedDate(),
            trimToNull(command.thumbnailUrl())
        );
        return bookRepository.findSummaryById(bookId).orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
    }

    @Transactional
    public BookSummary importVerified(HttpServletRequest request, BookSearchResult result) {
        currentMemberResolver.require(request, AccessLevel.MEMBER);
        validateSearchResult(result);
        Long bookId = bookRepository.importVerifiedBook(result);
        return bookRepository.findSummaryById(bookId).orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
    }

    public LibraryResponse getLibrary() {
        LocalDate targetMonth = LocalDate.now().withDayOfMonth(1);
        return new LibraryResponse(
            recommendedBookRepository.findActiveForMonth(targetMonth),
            bookRepository.findPopularBooks(5),
            readingRecordRepository.findRecentPublic(20)
        );
    }

    @Transactional
    public RecommendedBookView createRecommended(HttpServletRequest request, RecommendedBookCommand command) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        if (!readingRecordRepository.existsBook(command.bookId())) {
            throw new ApiException(ErrorCode.BOOK_NOT_FOUND);
        }
        Long id = recommendedBookRepository.create(
            admin.memberId(),
            command.targetMonth().withDayOfMonth(1),
            command.bookId(),
            required(command.reason(), "추천 이유를 입력해 주세요."),
            command.displayOrder()
        );
        return recommendedBookRepository.findActiveForMonth(command.targetMonth().withDayOfMonth(1))
            .stream()
            .filter(book -> book.id().equals(id))
            .findFirst()
            .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
    }

    @Transactional
    public void deleteRecommended(HttpServletRequest request, Long recommendedBookId) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        recommendedBookRepository.delete(recommendedBookId);
    }

    private void validateSearchResult(BookSearchResult result) {
        required(result.title(), "책 제목을 확인해 주세요.");
        required(result.authorsText(), "저자를 확인해 주세요.");
        if (!"KAKAO".equals(result.provider())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "지원하지 않는 책 검색 제공자입니다.");
        }
    }

    private void validateQuery(String query) {
        if (query == null || query.trim().length() < 2) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "검색어는 2자 이상 입력해 주세요.");
        }
    }

    private int safePage(int page) {
        return Math.max(page, 0);
    }

    private int safeSize(int size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, message);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public record ManualBookCommand(
        String title,
        String author,
        String publisher,
        LocalDate publishedDate,
        String thumbnailUrl
    ) {
    }

    public record BookDetailResponse(BookDetail book, List<ReadingRecordDetail> readingRecords) {
    }

    public record LibraryResponse(
        List<RecommendedBookView> recommendedBooks,
        List<LibraryBook> popularBooks,
        List<ReadingRecordDetail> recentReadingRecords
    ) {
    }

    public record RecommendedBookCommand(
        LocalDate targetMonth,
        Long bookId,
        String reason,
        int displayOrder
    ) {
    }
}
