package com.growtharchive.repository;

import com.growtharchive.domain.book.Book;
import com.growtharchive.domain.book.QBook;
import com.growtharchive.domain.reading.QReadingRecord;
import com.growtharchive.service.book.BookDetail;
import com.growtharchive.service.book.BookSearchResult;
import com.growtharchive.service.book.BookSummary;
import com.growtharchive.service.book.LibraryBook;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepository {

    private static final QBook book = QBook.book;
    private static final QReadingRecord readingRecord = QReadingRecord.readingRecord;

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public BookRepository(EntityManager entityManager, JPAQueryFactory queryFactory) {
        this.entityManager = entityManager;
        this.queryFactory = queryFactory;
    }

    public List<BookSummary> searchInternal(String query, int size) {
        String normalized = query.trim();
        return summarySelect()
            .where(
                book.title.containsIgnoreCase(normalized)
                    .or(book.authorsText.containsIgnoreCase(normalized))
                    .or(book.isbn13.containsIgnoreCase(normalized))
            )
            .orderBy(book.createdAt.desc())
            .limit(size)
            .fetch();
    }

    public Optional<BookSummary> findSummaryById(Long bookId) {
        return Optional.ofNullable(summarySelect()
            .where(book.id.eq(bookId))
            .fetchOne());
    }

    public List<BookSummary> findAdminBooks(String verificationStatus, int limit, int offset) {
        BooleanBuilder where = new BooleanBuilder();
        if (verificationStatus != null) {
            where.and(book.status.eq(verificationStatus));
        }
        return summarySelect()
            .where(where)
            .orderBy(book.createdAt.desc(), book.id.desc())
            .limit(limit)
            .offset(offset)
            .fetch();
    }

    public Optional<BookDetail> findDetailById(Long bookId) {
        return Optional.ofNullable(queryFactory
            .select(Projections.constructor(
                BookDetail.class,
                book.id,
                book.title,
                book.authorsText,
                book.publisher,
                book.publishedDate,
                book.thumbnailUrl,
                book.status,
                readingRecord.id.count().coalesce(0L),
                readingRecord.memberId.countDistinct().coalesce(0L),
                readingRecord.rating.avg()
            ))
            .from(book)
            .leftJoin(readingRecord).on(readingRecord.bookId.eq(book.id), readingRecord.status.eq("ACTIVE"))
            .where(book.id.eq(bookId))
            .groupBy(book.id)
            .fetchOne());
    }

    public List<LibraryBook> findPopularBooks(int size) {
        return queryFactory
            .select(Projections.constructor(
                LibraryBook.class,
                book.id,
                book.title,
                book.authorsText,
                book.publisher,
                book.thumbnailUrl,
                readingRecord.id.count(),
                readingRecord.rating.avg()
            ))
            .from(readingRecord)
            .join(book).on(book.id.eq(readingRecord.bookId))
            .where(readingRecord.status.eq("ACTIVE"))
            .groupBy(book.id)
            .orderBy(
                readingRecord.id.count().desc(),
                nullsLast(readingRecord.rating.avg().desc()),
                readingRecord.recordedAt.max().desc()
            )
            .limit(size)
            .fetch();
    }

    public Long createManualBook(
        Long memberId,
        String title,
        String authorsText,
        String publisher,
        LocalDate publishedDate,
        String thumbnailUrl
    ) {
        Book entity = new Book(
            "MANUAL",
            null,
            null,
            null,
            title,
            authorsText,
            publisher,
            publishedDate,
            thumbnailUrl,
            "UNVERIFIED",
            memberId,
            null
        );
        entityManager.persist(entity);
        entityManager.flush();
        return entity.getId();
    }

    public Long importVerifiedBook(BookSearchResult result) {
        Optional<Long> existing = findReusableBookId(result.isbn13(), result.isbn10());
        if (existing.isPresent()) {
            return existing.get();
        }
        Optional<Long> manualCandidate = findManualMergeCandidate(result);
        if (manualCandidate.isPresent()) {
            enrichManualBook(manualCandidate.get(), result);
            return manualCandidate.get();
        }
        Book entity = new Book(
            "KAKAO",
            firstNonBlank(result.isbn13(), result.isbn10(), result.title()),
            result.isbn10(),
            result.isbn13(),
            result.title(),
            result.authorsText(),
            result.publisher(),
            result.publishedDate(),
            result.thumbnailUrl(),
            "VERIFIED",
            null,
            result.sourcePayload()
        );
        entityManager.persist(entity);
        entityManager.flush();
        return entity.getId();
    }

    public Optional<Long> findManualMergeCandidate(BookSearchResult result) {
        List<Tuple> candidates = queryFactory
            .select(book.id, book.title, book.authorsText, book.publisher, book.publishedDate)
            .from(book)
            .where(
                book.source.eq("MANUAL"),
                book.status.eq("UNVERIFIED"),
                book.isbn10.isNull(),
                book.isbn13.isNull()
            )
            .fetch();
        return BookMergeMatcher.findSingleStrongMatch(
            result,
            candidates.stream()
                .map(row -> new BookMergeMatcher.ManualBookCandidate(
                    row.get(book.id),
                    row.get(book.title),
                    row.get(book.authorsText),
                    row.get(book.publisher),
                    row.get(book.publishedDate)
                ))
                .toList()
        );
    }

    public void enrichManualBook(Long bookId, BookSearchResult result) {
        queryFactory
            .update(book)
            .set(book.source, "KAKAO")
            .set(book.sourceBookId, firstNonBlank(result.isbn13(), result.isbn10(), result.title()))
            .set(book.isbn10, blankToNull(result.isbn10()))
            .set(book.isbn13, blankToNull(result.isbn13()))
            .set(book.title, result.title())
            .set(book.authorsText, result.authorsText())
            .set(book.publisher, blankToNull(result.publisher()))
            .set(book.publishedDate, result.publishedDate())
            .set(book.thumbnailUrl, blankToNull(result.thumbnailUrl()))
            .set(book.status, "VERIFIED")
            .set(book.sourcePayload, result.sourcePayload())
            .set(book.updatedAt, OffsetDateTime.now())
            .where(book.id.eq(bookId), book.source.eq("MANUAL"), book.status.eq("UNVERIFIED"))
            .execute();
    }

    public void updateBasicInfo(Long bookId, String title, String authorsText, String publisher, LocalDate publishedDate, String thumbnailUrl) {
        queryFactory
            .update(book)
            .set(book.title, title)
            .set(book.authorsText, authorsText)
            .set(book.publisher, publisher)
            .set(book.publishedDate, publishedDate)
            .set(book.thumbnailUrl, thumbnailUrl)
            .set(book.updatedAt, OffsetDateTime.now())
            .where(book.id.eq(bookId))
            .execute();
    }

    public void verify(Long bookId) {
        queryFactory
            .update(book)
            .set(book.status, "VERIFIED")
            .set(book.updatedAt, OffsetDateTime.now())
            .where(book.id.eq(bookId))
            .execute();
    }

    private Optional<Long> findReusableBookId(String isbn13, String isbn10) {
        if (isbn13 != null && !isbn13.isBlank()) {
            Optional<Long> bookId = queryIdByIsbn13(isbn13);
            if (bookId.isPresent()) {
                return bookId;
            }
        }
        if (isbn10 != null && !isbn10.isBlank()) {
            return queryIdByIsbn10(isbn10);
        }
        return Optional.empty();
    }

    private Optional<Long> queryIdByIsbn13(String isbn) {
        return Optional.ofNullable(queryFactory.select(book.id).from(book).where(book.isbn13.eq(isbn)).limit(1).fetchOne());
    }

    private Optional<Long> queryIdByIsbn10(String isbn) {
        return Optional.ofNullable(queryFactory.select(book.id).from(book).where(book.isbn10.eq(isbn)).limit(1).fetchOne());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private com.querydsl.jpa.impl.JPAQuery<BookSummary> summarySelect() {
        return queryFactory.select(Projections.constructor(
            BookSummary.class,
            book.id,
            book.title,
            book.authorsText,
            book.publisher,
            book.publishedDate,
            book.thumbnailUrl,
            book.status
        )).from(book);
    }

    private <T extends Comparable<?>> OrderSpecifier<T> nullsLast(OrderSpecifier<T> orderSpecifier) {
        return new OrderSpecifier<>(orderSpecifier.getOrder(), orderSpecifier.getTarget(), OrderSpecifier.NullHandling.NullsLast);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
