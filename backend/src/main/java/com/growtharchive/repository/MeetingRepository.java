package com.growtharchive.repository;

import com.growtharchive.service.meeting.MeetingAttendee;
import com.growtharchive.service.meeting.MeetingCommand;
import com.growtharchive.service.meeting.MeetingDetail;
import com.growtharchive.service.meeting.MeetingSummary;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MeetingRepository {

    private final JdbcTemplate jdbcTemplate;

    public MeetingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MeetingSummary> findPublic(String type, int limit, int offset) {
        String typeFilter = type == null || type.isBlank() ? "" : " AND mt.meeting_type = ?";
        Object[] args = type == null || type.isBlank()
            ? new Object[] { limit, offset }
            : new Object[] { type, limit, offset };
        return jdbcTemplate.query(
            summarySelect() + " WHERE mt.status IN ('SCHEDULED', 'HELD', 'CANCELED')" + typeFilter
                + summaryGroupBy()
                + " ORDER BY CASE WHEN mt.meeting_at >= now() THEN 0 ELSE 1 END, mt.meeting_at ASC LIMIT ? OFFSET ?",
            (rs, rowNum) -> mapSummary(rs, previewImageUrls(rs.getLong("id"), 5)),
            args
        );
    }

    public List<MeetingSummary> findPublicInMonth(String type, OffsetDateTime startAt, OffsetDateTime endAt, boolean descending, int limit, int offset) {
        String typeFilter = type == null || type.isBlank() ? "" : " AND mt.meeting_type = ?";
        String orderBy = descending ? " ORDER BY mt.meeting_at DESC LIMIT ? OFFSET ?" : " ORDER BY mt.meeting_at ASC LIMIT ? OFFSET ?";
        Object[] args = type == null || type.isBlank()
            ? new Object[] { Timestamp.from(startAt.toInstant()), Timestamp.from(endAt.toInstant()), limit, offset }
            : new Object[] { Timestamp.from(startAt.toInstant()), Timestamp.from(endAt.toInstant()), type, limit, offset };
        return jdbcTemplate.query(
            summarySelect() + """
                WHERE mt.status IN ('SCHEDULED', 'HELD', 'CANCELED')
                  AND mt.meeting_at >= ?
                  AND mt.meeting_at < ?
                """ + typeFilter
                + summaryGroupBy()
                + orderBy,
            (rs, rowNum) -> mapSummary(rs, previewImageUrls(rs.getLong("id"), 5)),
            args
        );
    }

    public List<MeetingSummary> findAdmin(String type, int limit, int offset) {
        String typeFilter = type == null || type.isBlank() ? "" : " AND mt.meeting_type = ?";
        Object[] args = type == null || type.isBlank()
            ? new Object[] { limit, offset }
            : new Object[] { type, limit, offset };
        return jdbcTemplate.query(
            summarySelect() + " WHERE mt.status <> 'DELETED'" + typeFilter
                + summaryGroupBy()
                + " ORDER BY mt.meeting_at DESC LIMIT ? OFFSET ?",
            (rs, rowNum) -> mapSummary(rs, previewImageUrls(rs.getLong("id"), 5)),
            args
        );
    }

    public Optional<MeetingDetail> findById(Long meetingId, boolean publicOnly, Long viewerMemberId) {
        String visibility = publicOnly ? " AND mt.status IN ('SCHEDULED', 'HELD', 'CANCELED')" : " AND mt.status <> 'DELETED'";
        return jdbcTemplate.query(
            detailSelect() + " WHERE mt.id = ?" + visibility + detailGroupBy(),
            rs -> rs.next()
                ? Optional.of(mapDetail(rs, viewerMemberId, previewImageUrls(meetingId, 5), List.of()))
                : Optional.empty(),
            meetingId
        );
    }

    public Optional<MeetingDetail> findEditableById(Long meetingId) {
        return findById(meetingId, false, null);
    }

    public List<MeetingAttendee> findAttendees(Long meetingId) {
        return jdbcTemplate.query(
            """
                SELECT m.id AS member_id,
                       CASE WHEN m.display_type = 'REAL_NAME' AND m.real_name IS NOT NULL AND m.real_name <> ''
                            THEN m.real_name ELSE m.nickname END AS display_name,
                       coalesce(profile_image.public_url, m.kakao_profile_image_url) AS profile_image_url
                FROM meeting_attendances ma
                JOIN members m ON m.id = ma.member_id
                LEFT JOIN image_assets profile_image ON profile_image.id = m.profile_image_id
                WHERE ma.meeting_id = ? AND ma.status = 'JOINED'
                ORDER BY ma.created_at ASC
                """,
            (rs, rowNum) -> new MeetingAttendee(
                rs.getLong("member_id"),
                rs.getString("display_name"),
                rs.getString("profile_image_url"),
                "/people/" + rs.getLong("member_id")
            ),
            meetingId
        );
    }

    public List<String> previewImageUrls(Long meetingId, int limit) {
        return jdbcTemplate.query(
            """
                SELECT coalesce(profile_image.public_url, m.kakao_profile_image_url) AS profile_image_url
                FROM meeting_attendances ma
                JOIN members m ON m.id = ma.member_id
                LEFT JOIN image_assets profile_image ON profile_image.id = m.profile_image_id
                WHERE ma.meeting_id = ? AND ma.status = 'JOINED'
                ORDER BY ma.created_at ASC
                LIMIT ?
                """,
            (rs, rowNum) -> rs.getString("profile_image_url"),
            meetingId,
            limit
        );
    }

    public Long createSmall(Long memberId, MeetingCommand command) {
        return jdbcTemplate.queryForObject(
            """
                INSERT INTO meetings (
                    meeting_type, title, description, meeting_at, region_text, detail_address, capacity,
                    cost_amount, cover_image_id, host_member_id, status, is_auto_generated, created_at, updated_at
                )
                VALUES ('SMALL', ?, ?, ?, ?, ?, ?, ?, ?, ?, 'SCHEDULED', false, now(), now())
                RETURNING id
                """,
            Long.class,
            command.title(),
            command.description(),
            Timestamp.from(command.meetingAt().toInstant()),
            command.locationRegion(),
            command.exactLocation(),
            command.capacity(),
            command.feeAmount(),
            command.thumbnailImageId(),
            memberId
        );
    }

    public void updateSmall(Long meetingId, MeetingCommand command) {
        jdbcTemplate.update(
            """
                UPDATE meetings
                SET title = ?, description = ?, meeting_at = ?, region_text = ?, detail_address = ?,
                    capacity = ?, cost_amount = ?, cover_image_id = ?, updated_at = now()
                WHERE id = ? AND meeting_type = 'SMALL' AND status <> 'DELETED'
                """,
            command.title(),
            command.description(),
            Timestamp.from(command.meetingAt().toInstant()),
            command.locationRegion(),
            command.exactLocation(),
            command.capacity(),
            command.feeAmount(),
            command.thumbnailImageId(),
            meetingId
        );
    }

    public void updateRegular(Long meetingId, MeetingCommand command) {
        jdbcTemplate.update(
            """
                UPDATE meetings
                SET title = ?, description = ?, meeting_at = ?, region_text = ?, detail_address = ?,
                    capacity = ?, cost_amount = ?, cover_image_id = ?, status = ?, updated_at = now()
                WHERE id = ? AND meeting_type IN ('REGULAR_READING', 'REGULAR_ACTION') AND status <> 'DELETED'
                """,
            command.title(),
            command.description(),
            Timestamp.from(command.meetingAt().toInstant()),
            command.locationRegion(),
            command.exactLocation(),
            command.capacity(),
            command.feeAmount(),
            command.thumbnailImageId(),
            command.status(),
            meetingId
        );
    }

    public void hide(Long meetingId) {
        jdbcTemplate.update(
            """
                UPDATE meetings
                SET status = 'HIDDEN', updated_at = now()
                WHERE id = ? AND status <> 'DELETED'
                """,
            meetingId
        );
    }

    public void restore(Long meetingId) {
        jdbcTemplate.update(
            """
                UPDATE meetings
                SET status = 'SCHEDULED', updated_at = now()
                WHERE id = ? AND status = 'HIDDEN'
                """,
            meetingId
        );
    }

    public void delete(Long meetingId) {
        jdbcTemplate.update(
            """
                UPDATE meetings
                SET status = 'DELETED', deleted_at = coalesce(deleted_at, now()), updated_at = now()
                WHERE id = ? AND status <> 'DELETED'
                """,
            meetingId
        );
    }

    public void join(Long meetingId, Long memberId) {
        jdbcTemplate.update(
            """
                INSERT INTO meeting_attendances (meeting_id, member_id, status, created_at, updated_at)
                VALUES (?, ?, 'JOINED', now(), now())
                ON CONFLICT (meeting_id, member_id)
                DO UPDATE SET status = 'JOINED', updated_at = now()
                """,
            meetingId,
            memberId
        );
    }

    public void cancel(Long meetingId, Long memberId) {
        jdbcTemplate.update(
            """
                UPDATE meeting_attendances
                SET status = 'CANCELED', updated_at = now()
                WHERE meeting_id = ? AND member_id = ?
                """,
            meetingId,
            memberId
        );
    }

    public boolean isJoined(Long meetingId, Long memberId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM meeting_attendances WHERE meeting_id = ? AND member_id = ? AND status = 'JOINED'",
            Integer.class,
            meetingId,
            memberId
        );
        return count != null && count > 0;
    }

    public int joinedCount(Long meetingId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM meeting_attendances WHERE meeting_id = ? AND status = 'JOINED'",
            Integer.class,
            meetingId
        );
        return count == null ? 0 : count;
    }

    public boolean createRegularIfMissing(
        String meetingType,
        String title,
        OffsetDateTime meetingAt,
        String regionText,
        String description,
        LocalDate targetMonth
    ) {
        try {
            Integer inserted = jdbcTemplate.queryForObject(
                """
                    INSERT INTO meetings (
                        meeting_type, title, description, meeting_at, region_text, detail_address,
                        capacity, cost_amount, status, target_month, is_auto_generated, created_at, updated_at
                    )
                    SELECT ?, ?, ?, ?, ?, null, null, 0, 'SCHEDULED', ?, true, now(), now()
                    WHERE NOT EXISTS (
                        SELECT 1 FROM meetings
                        WHERE meeting_type = ? AND target_month = ? AND status <> 'DELETED'
                    )
                    RETURNING 1
                    """,
                Integer.class,
                meetingType,
                title,
                description,
                Timestamp.from(meetingAt.toInstant()),
                regionText,
                Date.valueOf(targetMonth),
                meetingType,
                Date.valueOf(targetMonth)
            );
            return inserted != null && inserted == 1;
        } catch (EmptyResultDataAccessException exception) {
            return false;
        } catch (DuplicateKeyException exception) {
            return false;
        }
    }

    private String summarySelect() {
        return """
            SELECT mt.id, mt.meeting_type, mt.title, mt.description, mt.meeting_at, mt.region_text,
                   mt.capacity, mt.cost_amount, mt.status, cover.public_url AS cover_image_url,
                   count(ma.member_id) FILTER (WHERE ma.status = 'JOINED') AS attendee_count
            FROM meetings mt
            LEFT JOIN meeting_attendances ma ON ma.meeting_id = mt.id
            LEFT JOIN image_assets cover ON cover.id = mt.cover_image_id
            """;
    }

    private String summaryGroupBy() {
        return """
            GROUP BY mt.id, mt.meeting_type, mt.title, mt.description, mt.meeting_at, mt.region_text,
                     mt.capacity, mt.cost_amount, mt.status, cover.public_url
            """;
    }

    private String detailSelect() {
        return """
            SELECT mt.id, mt.meeting_type, mt.title, mt.description, mt.meeting_at, mt.region_text,
                   mt.detail_address, mt.capacity, mt.cost_amount, mt.cover_image_id, cover.public_url AS cover_image_url,
                   mt.host_member_id,
                   CASE WHEN host.display_type = 'REAL_NAME' AND host.real_name IS NOT NULL AND host.real_name <> ''
                        THEN host.real_name ELSE host.nickname END AS host_display_name,
                   mt.status, mt.target_month, mt.is_auto_generated,
                   count(ma.member_id) FILTER (WHERE ma.status = 'JOINED') AS attendee_count
            FROM meetings mt
            LEFT JOIN meeting_attendances ma ON ma.meeting_id = mt.id
            LEFT JOIN image_assets cover ON cover.id = mt.cover_image_id
            LEFT JOIN members host ON host.id = mt.host_member_id
            """;
    }

    private String detailGroupBy() {
        return """
            GROUP BY mt.id, mt.meeting_type, mt.title, mt.description, mt.meeting_at, mt.region_text,
                     mt.detail_address, mt.capacity, mt.cost_amount, mt.cover_image_id, cover.public_url,
                     mt.host_member_id, host.display_type, host.real_name, host.nickname,
                     mt.status, mt.target_month, mt.is_auto_generated
            """;
    }

    private MeetingSummary mapSummary(ResultSet rs, List<String> previewImageUrls) throws SQLException {
        return new MeetingSummary(
            rs.getLong("id"),
            rs.getString("meeting_type"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getObject("meeting_at", OffsetDateTime.class),
            rs.getString("region_text"),
            readNullableInteger(rs, "capacity"),
            rs.getInt("cost_amount"),
            rs.getString("cover_image_url"),
            rs.getString("status"),
            rs.getInt("attendee_count"),
            previewImageUrls
        );
    }

    private MeetingDetail mapDetail(
        ResultSet rs,
        Long viewerMemberId,
        List<String> previewImageUrls,
        List<MeetingAttendee> attendees
    ) throws SQLException {
        Long hostMemberId = readNullableLong(rs, "host_member_id");
        return new MeetingDetail(
            rs.getLong("id"),
            rs.getString("meeting_type"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getObject("meeting_at", OffsetDateTime.class),
            rs.getString("region_text"),
            rs.getString("detail_address"),
            readNullableInteger(rs, "capacity"),
            rs.getInt("cost_amount"),
            rs.getString("cover_image_url"),
            readNullableLong(rs, "cover_image_id"),
            hostMemberId,
            rs.getString("host_display_name"),
            rs.getString("status"),
            rs.getObject("target_month", LocalDate.class),
            rs.getBoolean("is_auto_generated"),
            rs.getInt("attendee_count"),
            viewerMemberId != null && isJoined(rs.getLong("id"), viewerMemberId),
            hostMemberId != null && hostMemberId.equals(viewerMemberId),
            previewImageUrls,
            attendees
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
