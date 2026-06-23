package com.growtharchive.repository;

import com.growtharchive.service.book.BookDetail;
import com.growtharchive.service.book.BookSearchResult;
import com.growtharchive.service.book.BookSummary;
import com.growtharchive.service.book.LibraryBook;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;

    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BookSummary> searchInternal(String query, int size) {
        String normalized = "%" + query.trim() + "%";
        return jdbcTemplate.query(
            """
                SELECT id, title, authors_text, publisher, published_date, thumbnail_url, status
                FROM books
                WHERE title ILIKE ? OR authors_text ILIKE ? OR coalesce(isbn13, '') ILIKE ?
                ORDER BY created_at DESC
                LIMIT ?
                """,
            this::mapSummary,
            normalized,
            normalized,
            normalized,
            size
        );
    }

    public Optional<BookSummary> findSummaryById(Long bookId) {
        return jdbcTemplate.query(
            """
                SELECT id, title, authors_text, publisher, published_date, thumbnail_url, status
                FROM books
                WHERE id = ?
                """,
            rs -> rs.next() ? Optional.of(mapSummary(rs, 0)) : Optional.empty(),
            bookId
        );
    }

    public Optional<BookDetail> findDetailById(Long bookId) {
        return jdbcTemplate.query(
            """
                SELECT b.id, b.title, b.authors_text, b.publisher, b.published_date, b.thumbnail_url, b.status,
                       count(rr.id) FILTER (WHERE rr.status = 'ACTIVE') AS reading_record_count,
                       count(DISTINCT rr.member_id) FILTER (WHERE rr.status = 'ACTIVE') AS reader_count,
                       avg(rr.rating) FILTER (WHERE rr.status = 'ACTIVE' AND rr.rating IS NOT NULL) AS average_rating
                FROM books b
                LEFT JOIN reading_records rr ON rr.book_id = b.id
                WHERE b.id = ?
                GROUP BY b.id
                """,
            rs -> rs.next() ? Optional.of(mapDetail(rs)) : Optional.empty(),
            bookId
        );
    }

    public List<LibraryBook> findPopularBooks(int size) {
        return jdbcTemplate.query(
            """
                SELECT b.id, b.title, b.authors_text, b.publisher, b.thumbnail_url,
                       count(rr.id) AS reading_record_count,
                       avg(rr.rating) FILTER (WHERE rr.rating IS NOT NULL) AS average_rating,
                       max(rr.recorded_at) AS latest_recorded_at
                FROM books b
                JOIN reading_records rr ON rr.book_id = b.id
                WHERE rr.status = 'ACTIVE'
                GROUP BY b.id
                ORDER BY reading_record_count DESC, average_rating DESC NULLS LAST, latest_recorded_at DESC
                LIMIT ?
                """,
            (rs, rowNum) -> new LibraryBook(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("authors_text"),
                rs.getString("publisher"),
                rs.getString("thumbnail_url"),
                rs.getLong("reading_record_count"),
                readDouble(rs, "average_rating")
            ),
            size
        );
    }

    public Long createManualBook(
        Long memberId,
        String title,
        String authorsText,
        String publisher,
        LocalDate publishedDate,
        String thumbnailUrl
    ) {
        return jdbcTemplate.queryForObject(
            """
                INSERT INTO books (
                    source, title, authors_text, publisher, published_date, thumbnail_url,
                    status, created_by_member_id, created_at, updated_at
                )
                VALUES ('MANUAL', ?, ?, ?, ?, ?, 'UNVERIFIED', ?, now(), now())
                RETURNING id
                """,
            Long.class,
            title,
            authorsText,
            publisher,
            publishedDate == null ? null : Date.valueOf(publishedDate),
            thumbnailUrl,
            memberId
        );
    }

    public Long importVerifiedBook(BookSearchResult result) {
        Optional<Long> existing = findReusableBookId(result.isbn13(), result.isbn10());
        if (existing.isPresent()) {
            return existing.get();
        }
        return jdbcTemplate.queryForObject(
            """
                INSERT INTO books (
                    source, source_book_id, isbn10, isbn13, title, authors_text, publisher,
                    published_date, thumbnail_url, status, source_payload, created_at, updated_at
                )
                VALUES ('KAKAO', ?, ?, ?, ?, ?, ?, ?, ?, 'VERIFIED', ?::jsonb, now(), now())
                RETURNING id
                """,
            Long.class,
            firstNonBlank(result.isbn13(), result.isbn10(), result.title()),
            result.isbn10(),
            result.isbn13(),
            result.title(),
            result.authorsText(),
            result.publisher(),
            result.publishedDate() == null ? null : Date.valueOf(result.publishedDate()),
            result.thumbnailUrl(),
            result.sourcePayload()
        );
    }

    private Optional<Long> findReusableBookId(String isbn13, String isbn10) {
        if (isbn13 != null && !isbn13.isBlank()) {
            Optional<Long> bookId = queryIdByIsbn("isbn13", isbn13);
            if (bookId.isPresent()) {
                return bookId;
            }
        }
        if (isbn10 != null && !isbn10.isBlank()) {
            return queryIdByIsbn("isbn10", isbn10);
        }
        return Optional.empty();
    }

    private Optional<Long> queryIdByIsbn(String column, String isbn) {
        return jdbcTemplate.query(
            "SELECT id FROM books WHERE " + column + " = ? LIMIT 1",
            rs -> rs.next() ? Optional.of(rs.getLong("id")) : Optional.empty(),
            isbn
        );
    }

    private BookSummary mapSummary(ResultSet rs, int rowNum) throws SQLException {
        Date publishedDate = rs.getDate("published_date");
        return new BookSummary(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("authors_text"),
            rs.getString("publisher"),
            publishedDate == null ? null : publishedDate.toLocalDate(),
            rs.getString("thumbnail_url"),
            rs.getString("status")
        );
    }

    private BookDetail mapDetail(ResultSet rs) throws SQLException {
        Date publishedDate = rs.getDate("published_date");
        return new BookDetail(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("authors_text"),
            rs.getString("publisher"),
            publishedDate == null ? null : publishedDate.toLocalDate(),
            rs.getString("thumbnail_url"),
            rs.getString("status"),
            rs.getLong("reading_record_count"),
            rs.getLong("reader_count"),
            readDouble(rs, "average_rating")
        );
    }

    private Double readDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
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
