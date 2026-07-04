package com.growtharchive.service.book;

import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import com.growtharchive.repository.BookRepository;
import com.growtharchive.repository.ReadingRecordRepository;
import com.growtharchive.repository.RecommendedBookRepository;
import com.growtharchive.repository.AdminAuditLogRepository;
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
    private final AdminAuditLogRepository adminAuditLogRepository;

    public BookService(
        CurrentMemberResolver currentMemberResolver,
        BookSearchProvider bookSearchProvider,
        BookRepository bookRepository,
        ReadingRecordRepository readingRecordRepository,
        RecommendedBookRepository recommendedBookRepository,
        AdminAuditLogRepository adminAuditLogRepository
    ) {
        this.currentMemberResolver = currentMemberResolver;
        this.bookSearchProvider = bookSearchProvider;
        this.bookRepository = bookRepository;
        this.readingRecordRepository = readingRecordRepository;
        this.recommendedBookRepository = recommendedBookRepository;
        this.adminAuditLogRepository = adminAuditLogRepository;
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
        List<ReadingRecordDetail> records = readingRecordRepository.findPublic(bookId, null, null, null, 50, 0);
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
        return new LibraryResponse(
            recommendedBookRepository.findActiveForDisplay(),
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
        adminAuditLogRepository.record(
            admin.memberId(),
            "CREATE_RECOMMENDED_BOOK",
            "RECOMMENDED_BOOK",
            id,
            null,
            "{\"bookId\":" + command.bookId() + ",\"targetMonth\":\"" + command.targetMonth().withDayOfMonth(1) + "\"}"
        );
        return recommendedBookRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
    }

    public List<RecommendedBookView> getRecommendedForAdmin(HttpServletRequest request, LocalDate targetMonth) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        return recommendedBookRepository.findActiveForMonth(targetMonth.withDayOfMonth(1));
    }

    public List<RecommendedBookView> getRecommendedForAdminDisplay(HttpServletRequest request) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        return recommendedBookRepository.findForAdminDisplay();
    }

    public List<BookSummary> getAdminBooks(HttpServletRequest request, String verificationStatus, int page, int size) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        String status = normalizeVerificationStatus(verificationStatus);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return bookRepository.findAdminBooks(status, safeSize, Math.max(page, 0) * safeSize);
    }

    @Transactional
    public BookSummary updateBookByAdmin(HttpServletRequest request, Long bookId, AdminBookCommand command) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        BookSummary before = bookRepository.findSummaryById(bookId)
            .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        bookRepository.updateBasicInfo(
            bookId,
            required(command.title(), "책 제목을 입력해 주세요."),
            required(command.authorsText(), "저자를 입력해 주세요."),
            trimToNull(command.publisher()),
            command.publishedDate(),
            trimToNull(command.thumbnailUrl())
        );
        adminAuditLogRepository.record(
            admin.memberId(),
            "UPDATE_BOOK",
            "BOOK",
            bookId,
            "{\"title\":\"" + escape(before.title()) + "\",\"authorsText\":\"" + escape(before.authorsText()) + "\"}",
            "{\"title\":\"" + escape(command.title()) + "\",\"authorsText\":\"" + escape(command.authorsText()) + "\"}"
        );
        return bookRepository.findSummaryById(bookId).orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
    }

    @Transactional
    public BookSummary verifyBookByAdmin(HttpServletRequest request, Long bookId) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        BookSummary before = bookRepository.findSummaryById(bookId)
            .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        bookRepository.verify(bookId);
        adminAuditLogRepository.record(
            admin.memberId(),
            "VERIFY_BOOK",
            "BOOK",
            bookId,
            "{\"status\":\"" + before.status() + "\"}",
            "{\"status\":\"VERIFIED\"}"
        );
        return bookRepository.findSummaryById(bookId).orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
    }

    @Transactional
    public RecommendedBookView updateRecommended(HttpServletRequest request, Long recommendedBookId, RecommendedBookCommand command) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        RecommendedBookView before = recommendedBookRepository.findById(recommendedBookId)
            .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        if (!readingRecordRepository.existsBook(command.bookId())) {
            throw new ApiException(ErrorCode.BOOK_NOT_FOUND);
        }
        LocalDate targetMonth = command.targetMonth().withDayOfMonth(1);
        recommendedBookRepository.update(
            recommendedBookId,
            admin.memberId(),
            targetMonth,
            command.bookId(),
            required(command.reason(), "추천 이유를 입력해 주세요."),
            command.displayOrder()
        );
        adminAuditLogRepository.record(
            admin.memberId(),
            "UPDATE_RECOMMENDED_BOOK",
            "RECOMMENDED_BOOK",
            recommendedBookId,
            "{\"bookId\":" + before.bookId() + ",\"displayOrder\":" + before.displayOrder() + "}",
            "{\"bookId\":" + command.bookId() + ",\"displayOrder\":" + command.displayOrder() + "}"
        );
        return recommendedBookRepository.findById(recommendedBookId)
            .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
    }

    @Transactional
    public void deleteRecommended(HttpServletRequest request, Long recommendedBookId) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        recommendedBookRepository.delete(recommendedBookId);
        adminAuditLogRepository.record(
            admin.memberId(),
            "DELETE_RECOMMENDED_BOOK",
            "RECOMMENDED_BOOK",
            recommendedBookId,
            null,
            "{\"status\":\"DELETED\"}"
        );
    }

    @Transactional
    public void hideRecommended(HttpServletRequest request, Long recommendedBookId) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        recommendedBookRepository.findById(recommendedBookId)
            .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        recommendedBookRepository.hide(recommendedBookId);
        adminAuditLogRepository.record(
            admin.memberId(),
            "HIDE_RECOMMENDED_BOOK",
            "RECOMMENDED_BOOK",
            recommendedBookId,
            null,
            "{\"status\":\"HIDDEN\"}"
        );
    }

    @Transactional
    public void restoreRecommended(HttpServletRequest request, Long recommendedBookId) {
        MemberPrincipal admin = currentMemberResolver.require(request, AccessLevel.ADMIN);
        recommendedBookRepository.findById(recommendedBookId)
            .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        recommendedBookRepository.restore(recommendedBookId);
        adminAuditLogRepository.record(
            admin.memberId(),
            "RESTORE_RECOMMENDED_BOOK",
            "RECOMMENDED_BOOK",
            recommendedBookId,
            null,
            "{\"status\":\"ACTIVE\"}"
        );
    }

    private void validateSearchResult(BookSearchResult result) {
        required(result.title(), "책 제목을 확인해 주세요.");
        required(result.authorsText(), "저자를 확인해 주세요.");
        if (!"KAKAO".equals(result.provider())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "지원하지 않는 책 검색 제공자입니다.");
        }
    }

    private void validateQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "검색어는 1자 이상 입력해 주세요.");
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

    private String normalizeVerificationStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (!List.of("VERIFIED", "UNVERIFIED").contains(normalized)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "책 검증 상태를 확인해 주세요.");
        }
        return normalized;
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
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

    public record AdminBookCommand(
        String title,
        String authorsText,
        String publisher,
        LocalDate publishedDate,
        String thumbnailUrl
    ) {
    }
}
