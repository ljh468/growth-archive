package com.growtharchive.service.reading;

import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import com.growtharchive.repository.ReadingRecordRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReadingRecordService {

    private final CurrentMemberResolver currentMemberResolver;
    private final ReadingRecordRepository readingRecordRepository;

    public ReadingRecordService(CurrentMemberResolver currentMemberResolver, ReadingRecordRepository readingRecordRepository) {
        this.currentMemberResolver = currentMemberResolver;
        this.readingRecordRepository = readingRecordRepository;
    }

    public List<ReadingRecordDetail> getPublic(Long bookId, Long memberId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int offset = Math.max(page, 0) * safeSize;
        return readingRecordRepository.findPublic(bookId, memberId, safeSize, offset);
    }

    @Transactional
    public ReadingRecordDetail create(HttpServletRequest request, ReadingRecordCommand command) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        validate(command);
        if (!readingRecordRepository.existsBook(command.bookId())) {
            throw new ApiException(ErrorCode.BOOK_NOT_FOUND);
        }
        Long id = readingRecordRepository.create(
            member.memberId(),
            command.bookId(),
            command.rating(),
            command.oneLineReview().trim(),
            command.blogUrl().trim(),
            command.imageId()
        );
        return readingRecordRepository.findById(id, false)
            .orElseThrow(() -> new ApiException(ErrorCode.READING_RECORD_NOT_FOUND));
    }

    @Transactional
    public ReadingRecordDetail update(HttpServletRequest request, Long recordId, ReadingRecordCommand command) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        validate(command);
        if (!readingRecordRepository.existsBook(command.bookId())) {
            throw new ApiException(ErrorCode.BOOK_NOT_FOUND);
        }
        ReadingRecordDetail current = readingRecordRepository.findById(recordId, false)
            .orElseThrow(() -> new ApiException(ErrorCode.READING_RECORD_NOT_FOUND));
        if (!current.memberId().equals(member.memberId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "작성자만 독서기록을 수정할 수 있습니다.");
        }
        readingRecordRepository.updateContent(
            recordId,
            member.memberId(),
            command.bookId(),
            command.rating(),
            command.oneLineReview().trim(),
            command.blogUrl().trim(),
            command.imageId()
        );
        return readingRecordRepository.findById(recordId, false)
            .orElseThrow(() -> new ApiException(ErrorCode.READING_RECORD_NOT_FOUND));
    }

    @Transactional
    public void deleteByAuthor(HttpServletRequest request, Long recordId) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        ReadingRecordDetail current = readingRecordRepository.findById(recordId, false)
            .orElseThrow(() -> new ApiException(ErrorCode.READING_RECORD_NOT_FOUND));
        if (!current.memberId().equals(member.memberId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "작성자만 독서기록을 삭제할 수 있습니다.");
        }
        readingRecordRepository.softDeleteByAuthor(recordId, member.memberId());
    }

    @Transactional
    public void hideByAdmin(HttpServletRequest request, Long recordId) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        ensureRecordExists(recordId);
        readingRecordRepository.hideByAdmin(recordId);
    }

    @Transactional
    public void restoreByAdmin(HttpServletRequest request, Long recordId) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        ensureRecordExists(recordId);
        readingRecordRepository.restoreByAdmin(recordId);
    }

    @Transactional
    public void deleteByAdmin(HttpServletRequest request, Long recordId) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        ensureRecordExists(recordId);
        readingRecordRepository.deleteByAdmin(recordId);
    }

    private void ensureRecordExists(Long recordId) {
        if (!readingRecordRepository.existsById(recordId)) {
            throw new ApiException(ErrorCode.READING_RECORD_NOT_FOUND);
        }
    }

    private void validate(ReadingRecordCommand command) {
        if (command.bookId() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "책을 선택해 주세요.");
        }
        if (command.rating() != null && (command.rating() < 1 || command.rating() > 5)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "평점은 1~5 정수만 입력할 수 있습니다.");
        }
        if (command.oneLineReview() == null || command.oneLineReview().isBlank() || command.oneLineReview().length() > 300) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "한줄평은 1~300자로 입력해 주세요.");
        }
        if (command.blogUrl() == null || command.blogUrl().isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "블로그 URL을 입력해 주세요.");
        }
        try {
            URI uri = URI.create(command.blogUrl().trim());
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
        } catch (RuntimeException exception) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "블로그 URL 형식을 확인해 주세요.");
        }
    }

    public record ReadingRecordCommand(
        Long bookId,
        Integer rating,
        String oneLineReview,
        String blogUrl,
        Long imageId
    ) {
    }
}
