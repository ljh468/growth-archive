package com.growtharchive.repository;

import com.growtharchive.service.reading.ReadingRecordDetail;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReadingRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReadingRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(Long memberId, Long bookId, Integer rating, String oneLineReview, String blogUrl, Long imageId) {
        return jdbcTemplate.queryForObject(
            """
                INSERT INTO reading_records (
                    member_id, book_id, rating, one_line_review, blog_url, representative_image_id,
                    status, recorded_at, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE', now(), now(), now())
                RETURNING id
                """,
            Long.class,
            memberId,
            bookId,
            rating,
            oneLineReview,
            blogUrl,
            imageId
        );
    }

    public void updateContent(Long recordId, Long memberId, Long bookId, Integer rating, String oneLineReview, String blogUrl, Long imageId) {
        jdbcTemplate.update(
            """
                UPDATE reading_records
                SET book_id = ?, rating = ?, one_line_review = ?, blog_url = ?, representative_image_id = ?, updated_at = now()
                WHERE id = ? AND member_id = ? AND status <> 'DELETED'
                """,
            bookId,
            rating,
            oneLineReview,
            blogUrl,
            imageId,
            recordId,
            memberId
        );
    }

    public void softDeleteByAuthor(Long recordId, Long memberId) {
        jdbcTemplate.update(
            """
                UPDATE reading_records
                SET status = 'DELETED', deleted_at = coalesce(deleted_at, now()), updated_at = now()
                WHERE id = ? AND member_id = ? AND status <> 'DELETED'
                """,
            recordId,
            memberId
        );
    }

    public void hideByAdmin(Long recordId) {
        jdbcTemplate.update(
            """
                UPDATE reading_records
                SET status = 'HIDDEN', hidden_at = coalesce(hidden_at, now()), updated_at = now()
                WHERE id = ? AND status <> 'DELETED'
                """,
            recordId
        );
    }

    public void restoreByAdmin(Long recordId) {
        jdbcTemplate.update(
            """
                UPDATE reading_records
                SET status = 'ACTIVE', hidden_at = null, updated_at = now()
                WHERE id = ? AND status = 'HIDDEN'
                """,
            recordId
        );
    }

    public void deleteByAdmin(Long recordId) {
        jdbcTemplate.update(
            """
                UPDATE reading_records
                SET status = 'DELETED', deleted_at = coalesce(deleted_at, now()), updated_at = now()
                WHERE id = ? AND status <> 'DELETED'
                """,
            recordId
        );
    }

    public Optional<ReadingRecordDetail> findById(Long recordId, boolean publicOnly) {
        String visibility = publicOnly ? "AND rr.status = 'ACTIVE'" : "";
        return jdbcTemplate.query(
            baseSelect() + " WHERE rr.id = ? " + visibility,
            rs -> rs.next() ? Optional.of(mapDetail(rs)) : Optional.empty(),
            recordId
        );
    }

    public List<ReadingRecordDetail> findPublic(Long bookId, Long memberId, int limit, int offset) {
        StringBuilder sql = new StringBuilder(baseSelect())
            .append(" WHERE rr.status = 'ACTIVE'");
        List<Object> args = new ArrayList<>();
        if (bookId != null) {
            sql.append(" AND rr.book_id = ?");
            args.add(bookId);
        }
        if (memberId != null) {
            sql.append(" AND rr.member_id = ?");
            args.add(memberId);
        }
        sql.append(" ORDER BY rr.recorded_at DESC LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);
        return jdbcTemplate.query(
            sql.toString(),
            (rs, rowNum) -> mapDetail(rs),
            args.toArray()
        );
    }

    public List<ReadingRecordDetail> findRecentPublic(int limit) {
        return jdbcTemplate.query(
            baseSelect() + " WHERE rr.status = 'ACTIVE' ORDER BY rr.recorded_at DESC LIMIT ?",
            (rs, rowNum) -> mapDetail(rs),
            limit
        );
    }

    public boolean existsById(Long recordId) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM reading_records WHERE id = ?", Integer.class, recordId);
        return count != null && count > 0;
    }

    public boolean existsBook(Long bookId) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM books WHERE id = ?", Integer.class, bookId);
        return count != null && count > 0;
    }

    private String baseSelect() {
        return """
            SELECT rr.id, rr.member_id, rr.book_id, rr.rating, rr.one_line_review, rr.blog_url, rr.status,
                   rr.recorded_at, rr.created_at, rr.representative_image_id,
                   b.title AS book_title, b.authors_text, b.thumbnail_url AS book_thumbnail_url,
                   m.nickname, m.real_name, m.display_type, m.kakao_profile_image_url,
                   ia.public_url AS record_image_url
            FROM reading_records rr
            JOIN books b ON b.id = rr.book_id
            JOIN members m ON m.id = rr.member_id
            LEFT JOIN image_assets ia ON ia.id = rr.representative_image_id
            """;
    }

    private ReadingRecordDetail mapDetail(ResultSet rs) throws SQLException {
        String displayType = rs.getString("display_type");
        String realName = rs.getString("real_name");
        String nickname = rs.getString("nickname");
        return new ReadingRecordDetail(
            rs.getLong("id"),
            rs.getLong("member_id"),
            "REAL_NAME".equals(displayType) && realName != null && !realName.isBlank() ? realName : nickname,
            rs.getString("kakao_profile_image_url"),
            rs.getLong("book_id"),
            rs.getString("book_title"),
            rs.getString("authors_text"),
            rs.getString("book_thumbnail_url"),
            readNullableInteger(rs, "rating"),
            rs.getString("one_line_review"),
            rs.getString("blog_url"),
            readNullableLong(rs, "representative_image_id"),
            rs.getString("record_image_url"),
            rs.getString("status"),
            rs.getObject("recorded_at", OffsetDateTime.class),
            rs.getObject("created_at", OffsetDateTime.class)
        );
    }

    private Integer readNullableInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private Long readNullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
