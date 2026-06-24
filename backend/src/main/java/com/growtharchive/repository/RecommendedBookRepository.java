package com.growtharchive.repository;

import com.growtharchive.service.book.RecommendedBookView;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RecommendedBookRepository {

    private final JdbcTemplate jdbcTemplate;

    public RecommendedBookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RecommendedBookView> findActiveForMonth(LocalDate targetMonth) {
        return jdbcTemplate.query(
            """
                SELECT rb.id, rb.target_month, rb.reason, rb.display_order,
                       b.id AS book_id, b.title, b.authors_text, b.publisher, b.thumbnail_url
                FROM recommended_books rb
                JOIN books b ON b.id = rb.book_id
                WHERE rb.target_month = ?
                  AND rb.status = 'ACTIVE'
                ORDER BY rb.display_order ASC, rb.id ASC
                """,
            (rs, rowNum) -> new RecommendedBookView(
                rs.getLong("id"),
                rs.getLong("book_id"),
                rs.getString("title"),
                rs.getString("authors_text"),
                rs.getString("publisher"),
                rs.getString("thumbnail_url"),
                rs.getString("reason"),
                rs.getInt("display_order")
            ),
            Date.valueOf(targetMonth)
        );
    }

    public Long create(Long adminMemberId, LocalDate targetMonth, Long bookId, String reason, int displayOrder) {
        return jdbcTemplate.queryForObject(
            """
                INSERT INTO recommended_books (
                    target_month, book_id, reason, display_order, status,
                    recommended_by_member_id, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, 'ACTIVE', ?, now(), now())
                RETURNING id
                """,
            Long.class,
            Date.valueOf(targetMonth),
            bookId,
            reason,
            displayOrder,
            adminMemberId
        );
    }

    public Optional<RecommendedBookView> findById(Long recommendedBookId) {
        return jdbcTemplate.query(
            """
                SELECT rb.id, rb.target_month, rb.reason, rb.display_order,
                       b.id AS book_id, b.title, b.authors_text, b.publisher, b.thumbnail_url
                FROM recommended_books rb
                JOIN books b ON b.id = rb.book_id
                WHERE rb.id = ? AND rb.status <> 'DELETED'
                """,
            rs -> rs.next()
                ? Optional.of(new RecommendedBookView(
                    rs.getLong("id"),
                    rs.getLong("book_id"),
                    rs.getString("title"),
                    rs.getString("authors_text"),
                    rs.getString("publisher"),
                    rs.getString("thumbnail_url"),
                    rs.getString("reason"),
                    rs.getInt("display_order")
                ))
                : Optional.empty(),
            recommendedBookId
        );
    }

    public void update(Long recommendedBookId, Long adminMemberId, LocalDate targetMonth, Long bookId, String reason, int displayOrder) {
        jdbcTemplate.update(
            """
                UPDATE recommended_books
                SET target_month = ?,
                    book_id = ?,
                    reason = ?,
                    display_order = ?,
                    recommended_by_member_id = ?,
                    status = 'ACTIVE',
                    updated_at = now()
                WHERE id = ? AND status <> 'DELETED'
                """,
            Date.valueOf(targetMonth),
            bookId,
            reason,
            displayOrder,
            adminMemberId,
            recommendedBookId
        );
    }

    public void delete(Long recommendedBookId) {
        jdbcTemplate.update(
            "UPDATE recommended_books SET status = 'DELETED', updated_at = now() WHERE id = ?",
            recommendedBookId
        );
    }
}
