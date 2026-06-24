package com.growtharchive.repository;

import com.growtharchive.service.review.MeetingReviewDetail;
import com.growtharchive.service.review.MeetingReviewSummary;
import com.growtharchive.service.review.ReviewImageView;
import com.growtharchive.service.storage.StoredImage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MeetingReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    public MeetingReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MeetingReviewSummary> findPublic(Long meetingId, int limit, int offset) {
        StringBuilder sql = new StringBuilder(summarySelect()).append(" WHERE mr.status = 'ACTIVE'");
        List<Object> args = new ArrayList<>();
        if (meetingId != null) {
            sql.append(" AND mr.meeting_id = ?");
            args.add(meetingId);
        }
        sql.append(" ORDER BY mr.created_at DESC LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapSummary(rs), args.toArray());
    }

    public List<MeetingReviewSummary> findAdmin(int limit, int offset) {
        return jdbcTemplate.query(
            summarySelect() + " WHERE mr.status <> 'DELETED' ORDER BY mr.created_at DESC LIMIT ? OFFSET ?",
            (rs, rowNum) -> mapSummary(rs),
            limit,
            offset
        );
    }

    public Optional<MeetingReviewDetail> findById(Long reviewId, boolean publicOnly, Long viewerMemberId) {
        String visibility = publicOnly ? " AND mr.status = 'ACTIVE'" : " AND mr.status <> 'DELETED'";
        return jdbcTemplate.query(
            detailSelect() + " WHERE mr.id = ?" + visibility,
            rs -> rs.next() ? Optional.of(mapDetail(rs, viewerMemberId, findImages(reviewId))) : Optional.empty(),
            reviewId
        );
    }

    public Long create(Long memberId, Long meetingId, String title, String content, Long representativeImageId) {
        return jdbcTemplate.queryForObject(
            """
                INSERT INTO meeting_reviews (
                    meeting_id, member_id, title, content, representative_image_id, status, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, 'ACTIVE', now(), now())
                RETURNING id
                """,
            Long.class,
            meetingId,
            memberId,
            title,
            content,
            representativeImageId
        );
    }

    public void update(Long reviewId, String title, String content, Long representativeImageId) {
        jdbcTemplate.update(
            """
                UPDATE meeting_reviews
                SET title = ?, content = ?, representative_image_id = ?, updated_at = now()
                WHERE id = ? AND status <> 'DELETED'
                """,
            title,
            content,
            representativeImageId,
            reviewId
        );
    }

    public void replaceImages(Long reviewId, List<Long> imageIds) {
        jdbcTemplate.update("DELETE FROM meeting_review_images WHERE meeting_review_id = ?", reviewId);
        for (int i = 0; i < imageIds.size(); i++) {
            jdbcTemplate.update(
                """
                    INSERT INTO meeting_review_images (meeting_review_id, image_asset_id, display_order, created_at)
                    VALUES (?, ?, ?, now())
                    """,
                reviewId,
                imageIds.get(i),
                i + 1
            );
        }
    }

    public void deleteByAuthor(Long reviewId, Long memberId) {
        jdbcTemplate.update(
            """
                UPDATE meeting_reviews
                SET status = 'DELETED', deleted_at = coalesce(deleted_at, now()), updated_at = now()
                WHERE id = ? AND member_id = ? AND status <> 'DELETED'
                """,
            reviewId,
            memberId
        );
    }

    public void hide(Long reviewId) {
        jdbcTemplate.update(
            """
                UPDATE meeting_reviews
                SET status = 'HIDDEN', hidden_at = coalesce(hidden_at, now()), updated_at = now()
                WHERE id = ? AND status <> 'DELETED'
                """,
            reviewId
        );
    }

    public void restore(Long reviewId) {
        jdbcTemplate.update(
            """
                UPDATE meeting_reviews
                SET status = 'ACTIVE', hidden_at = null, updated_at = now()
                WHERE id = ? AND status = 'HIDDEN'
                """,
            reviewId
        );
    }

    public void deleteByAdmin(Long reviewId) {
        jdbcTemplate.update(
            """
                UPDATE meeting_reviews
                SET status = 'DELETED', deleted_at = coalesce(deleted_at, now()), updated_at = now()
                WHERE id = ? AND status <> 'DELETED'
                """,
            reviewId
        );
    }

    public boolean meetingCanReceiveReview(Long meetingId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM meetings WHERE id = ? AND status IN ('SCHEDULED', 'HELD')",
            Integer.class,
            meetingId
        );
        return count != null && count > 0;
    }

    public int countOwnedReviewImages(Long memberId, List<Long> imageIds) {
        if (imageIds.isEmpty()) {
            return 0;
        }
        String placeholders = String.join(",", imageIds.stream().map(id -> "?").toList());
        List<Object> args = new ArrayList<>();
        args.add(memberId);
        args.addAll(imageIds);
        Integer count = jdbcTemplate.queryForObject(
            """
                SELECT count(*)
                FROM image_assets
                WHERE owner_member_id = ? AND image_type = 'MEETING_REVIEW' AND id IN (%s)
                """.formatted(placeholders),
            Integer.class,
            args.toArray()
        );
        return count == null ? 0 : count;
    }

    public Long createImageAsset(Long memberId, StoredImage image) {
        return jdbcTemplate.queryForObject(
            """
                INSERT INTO image_assets (
                    owner_member_id, bucket, object_key, public_url, image_type, mime_type, size_bytes, created_at
                )
                VALUES (?, ?, ?, ?, 'MEETING_REVIEW', ?, ?, now())
                RETURNING id
                """,
            Long.class,
            memberId,
            image.bucket(),
            image.objectKey(),
            image.publicUrl(),
            image.mimeType(),
            image.sizeBytes()
        );
    }

    public void insertActivityEvent(Long memberId, Long reviewId, String title) {
        jdbcTemplate.update(
            """
                INSERT INTO activity_events (
                    member_id, event_type, reference_type, reference_id, visibility, summary, happened_at, created_at
                )
                VALUES (?, 'MEETING_REVIEW_CREATED', 'MEETING_REVIEW', ?, 'PUBLIC', ?, now(), now())
                """,
            memberId,
            reviewId,
            title
        );
    }

    public List<ReviewImageView> findImages(Long reviewId) {
        return jdbcTemplate.query(
            """
                SELECT mri.image_asset_id, ia.public_url, mri.display_order
                FROM meeting_review_images mri
                JOIN image_assets ia ON ia.id = mri.image_asset_id
                WHERE mri.meeting_review_id = ?
                ORDER BY mri.display_order ASC
                """,
            (rs, rowNum) -> new ReviewImageView(
                rs.getLong("image_asset_id"),
                rs.getString("public_url"),
                rs.getInt("display_order")
            ),
            reviewId
        );
    }

    private String summarySelect() {
        return """
            SELECT mr.id, mr.meeting_id, mt.title AS meeting_title, mr.member_id,
                   CASE WHEN m.display_type = 'REAL_NAME' AND m.real_name IS NOT NULL AND m.real_name <> ''
                        THEN m.real_name ELSE m.nickname END AS member_display_name,
                   coalesce(profile_image.public_url, m.kakao_profile_image_url) AS member_profile_image_url,
                   mr.title, left(mr.content, 120) AS content_summary,
                   representative.public_url AS representative_image_url,
                   mr.status, mr.created_at
            FROM meeting_reviews mr
            JOIN meetings mt ON mt.id = mr.meeting_id
            JOIN members m ON m.id = mr.member_id
            LEFT JOIN image_assets profile_image ON profile_image.id = m.profile_image_id
            LEFT JOIN image_assets representative ON representative.id = mr.representative_image_id
            """;
    }

    private String detailSelect() {
        return """
            SELECT mr.id, mr.meeting_id, mt.title AS meeting_title, mr.member_id,
                   CASE WHEN m.display_type = 'REAL_NAME' AND m.real_name IS NOT NULL AND m.real_name <> ''
                        THEN m.real_name ELSE m.nickname END AS member_display_name,
                   coalesce(profile_image.public_url, m.kakao_profile_image_url) AS member_profile_image_url,
                   mr.title, mr.content, mr.status, mr.created_at, mr.updated_at
            FROM meeting_reviews mr
            JOIN meetings mt ON mt.id = mr.meeting_id
            JOIN members m ON m.id = mr.member_id
            LEFT JOIN image_assets profile_image ON profile_image.id = m.profile_image_id
            """;
    }

    private MeetingReviewSummary mapSummary(ResultSet rs) throws SQLException {
        return new MeetingReviewSummary(
            rs.getLong("id"),
            rs.getLong("meeting_id"),
            rs.getString("meeting_title"),
            rs.getLong("member_id"),
            rs.getString("member_display_name"),
            rs.getString("member_profile_image_url"),
            rs.getString("title"),
            rs.getString("content_summary"),
            rs.getString("representative_image_url"),
            rs.getString("status"),
            rs.getObject("created_at", OffsetDateTime.class)
        );
    }

    private MeetingReviewDetail mapDetail(ResultSet rs, Long viewerMemberId, List<ReviewImageView> images) throws SQLException {
        Long memberId = rs.getLong("member_id");
        return new MeetingReviewDetail(
            rs.getLong("id"),
            rs.getLong("meeting_id"),
            rs.getString("meeting_title"),
            memberId,
            rs.getString("member_display_name"),
            rs.getString("member_profile_image_url"),
            rs.getString("title"),
            rs.getString("content"),
            rs.getString("status"),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getObject("updated_at", OffsetDateTime.class),
            viewerMemberId != null && memberId.equals(viewerMemberId),
            images
        );
    }
}
