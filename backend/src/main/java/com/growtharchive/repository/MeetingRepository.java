package com.growtharchive.repository;

import com.growtharchive.domain.image.QImageAsset;
import com.growtharchive.domain.meeting.Meeting;
import com.growtharchive.domain.meeting.MeetingAttendance;
import com.growtharchive.domain.meeting.QMeeting;
import com.growtharchive.domain.meeting.QMeetingAttendance;
import com.growtharchive.domain.member.QMember;
import com.growtharchive.service.meeting.MeetingAttendee;
import com.growtharchive.service.meeting.MeetingCommand;
import com.growtharchive.service.meeting.MeetingDetail;
import com.growtharchive.service.meeting.MeetingSummary;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MeetingRepository {

    private static final QMeeting meeting = QMeeting.meeting;
    private static final QMeetingAttendance attendance = QMeetingAttendance.meetingAttendance;
    private static final QImageAsset coverImage = new QImageAsset("coverImage");
    private static final QImageAsset profileImage = new QImageAsset("profileImage");
    private static final QMember host = new QMember("host");
    private static final QMember attendee = new QMember("attendee");

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    public MeetingRepository(JPAQueryFactory queryFactory, EntityManager entityManager) {
        this.queryFactory = queryFactory;
        this.entityManager = entityManager;
    }

    public List<MeetingSummary> findPublic(String type, int limit, int offset) {
        BooleanBuilder where = publicMeetingWhere(type)
            .and(meeting.status.in("SCHEDULED", "HELD", "CANCELED"));
        OrderSpecifier<Integer> upcomingFirst = new CaseBuilder()
            .when(meeting.meetingAt.goe(OffsetDateTime.now()))
            .then(0)
            .otherwise(1)
            .asc();
        return findSummaries(where, limit, offset, upcomingFirst, meeting.meetingAt.asc());
    }

    public List<MeetingSummary> findPublicInMonth(
        String type,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        boolean descending,
        int limit,
        int offset
    ) {
        BooleanBuilder where = publicMeetingWhere(type)
            .and(meeting.status.in("SCHEDULED", "HELD", "CANCELED"))
            .and(meeting.meetingAt.goe(startAt))
            .and(meeting.meetingAt.lt(endAt));
        return findSummaries(where, limit, offset, descending ? meeting.meetingAt.desc() : meeting.meetingAt.asc());
    }

    public List<MeetingSummary> findAdmin(String type, int limit, int offset) {
        BooleanBuilder where = publicMeetingWhere(type).and(meeting.status.ne("DELETED"));
        return findSummaries(where, limit, offset, meeting.meetingAt.desc());
    }

    public List<MeetingSummary> findCreatedSmallByMemberId(Long memberId, int limit, int offset) {
        BooleanBuilder where = new BooleanBuilder()
            .and(meeting.meetingType.eq("SMALL"))
            .and(meeting.hostMemberId.eq(memberId))
            .and(meeting.status.ne("DELETED"));
        return findSummaries(where, limit, offset, meeting.meetingAt.desc(), meeting.id.desc());
    }

    public Optional<MeetingDetail> findById(Long meetingId, boolean publicOnly, Long viewerMemberId) {
        BooleanBuilder where = new BooleanBuilder(meeting.id.eq(meetingId));
        if (publicOnly) {
            where.and(meeting.status.in("SCHEDULED", "HELD", "CANCELED"));
        } else {
            where.and(meeting.status.ne("DELETED"));
        }
        Tuple row = queryFactory
            .select(detailFields())
            .from(meeting)
            .leftJoin(coverImage).on(coverImage.id.eq(meeting.coverImageId))
            .leftJoin(host).on(host.id.eq(meeting.hostMemberId))
            .where(where)
            .fetchOne();
        return row == null ? Optional.empty() : Optional.of(mapDetail(row, viewerMemberId, List.of()));
    }

    public Optional<MeetingDetail> findEditableById(Long meetingId) {
        return findById(meetingId, false, null);
    }

    public List<MeetingAttendee> findAttendees(Long meetingId) {
        return queryFactory
            .select(attendee.id, attendee.displayType, attendee.realName, attendee.nickname, profileImage.publicUrl, attendee.kakaoProfileImageUrl)
            .from(attendance)
            .join(attendee).on(attendee.id.eq(attendance.id.memberId))
            .leftJoin(profileImage).on(profileImage.id.eq(attendee.profileImageId))
            .where(attendance.id.meetingId.eq(meetingId), attendance.status.eq("JOINED"))
            .orderBy(attendance.createdAt.asc())
            .fetch()
            .stream()
            .map(row -> new MeetingAttendee(
                row.get(attendee.id),
                displayName(row, attendee),
                coalesce(row.get(profileImage.publicUrl), row.get(attendee.kakaoProfileImageUrl)),
                "/people/" + row.get(attendee.id)
            ))
            .toList();
    }

    public List<String> previewImageUrls(Long meetingId, int limit) {
        return queryFactory
            .select(profileImage.publicUrl, attendee.kakaoProfileImageUrl)
            .from(attendance)
            .join(attendee).on(attendee.id.eq(attendance.id.memberId))
            .leftJoin(profileImage).on(profileImage.id.eq(attendee.profileImageId))
            .where(attendance.id.meetingId.eq(meetingId), attendance.status.eq("JOINED"))
            .orderBy(attendance.createdAt.asc())
            .limit(limit)
            .fetch()
            .stream()
            .map(row -> coalesce(row.get(profileImage.publicUrl), row.get(attendee.kakaoProfileImageUrl)))
            .toList();
    }

    @Transactional
    public Long createSmall(Long memberId, MeetingCommand command) {
        Meeting created = new Meeting(
            "SMALL",
            command.title(),
            command.description(),
            command.meetingAt(),
            command.locationRegion(),
            command.exactLocation(),
            command.capacity(),
            command.feeAmount(),
            command.thumbnailImageId(),
            memberId,
            null,
            false
        );
        entityManager.persist(created);
        entityManager.flush();
        return created.getId();
    }

    @Transactional
    public void updateSmall(Long meetingId, MeetingCommand command) {
        queryFactory
            .update(meeting)
            .set(meeting.title, command.title())
            .set(meeting.description, command.description())
            .set(meeting.meetingAt, command.meetingAt())
            .set(meeting.regionText, command.locationRegion())
            .set(meeting.detailAddress, command.exactLocation())
            .set(meeting.capacity, command.capacity())
            .set(meeting.costAmount, command.feeAmount())
            .set(meeting.coverImageId, command.thumbnailImageId())
            .set(meeting.updatedAt, OffsetDateTime.now())
            .where(meeting.id.eq(meetingId), meeting.meetingType.eq("SMALL"), meeting.status.ne("DELETED"))
            .execute();
    }

    @Transactional
    public void updateRegular(Long meetingId, MeetingCommand command) {
        queryFactory
            .update(meeting)
            .set(meeting.title, command.title())
            .set(meeting.description, command.description())
            .set(meeting.meetingAt, command.meetingAt())
            .set(meeting.regionText, command.locationRegion())
            .set(meeting.detailAddress, command.exactLocation())
            .set(meeting.capacity, command.capacity())
            .set(meeting.costAmount, command.feeAmount())
            .set(meeting.coverImageId, command.thumbnailImageId())
            .set(meeting.status, command.status())
            .set(meeting.updatedAt, OffsetDateTime.now())
            .where(
                meeting.id.eq(meetingId),
                meeting.meetingType.in("REGULAR_READING", "REGULAR_ACTION"),
                meeting.status.ne("DELETED")
            )
            .execute();
    }

    @Transactional
    public void hide(Long meetingId) {
        queryFactory
            .update(meeting)
            .set(meeting.status, "HIDDEN")
            .set(meeting.updatedAt, OffsetDateTime.now())
            .where(meeting.id.eq(meetingId), meeting.status.ne("DELETED"))
            .execute();
    }

    @Transactional
    public void restore(Long meetingId) {
        queryFactory
            .update(meeting)
            .set(meeting.status, "SCHEDULED")
            .set(meeting.updatedAt, OffsetDateTime.now())
            .where(meeting.id.eq(meetingId), meeting.status.eq("HIDDEN"))
            .execute();
    }

    @Transactional
    public void delete(Long meetingId) {
        OffsetDateTime now = OffsetDateTime.now();
        queryFactory
            .update(meeting)
            .set(meeting.status, "DELETED")
            .set(meeting.deletedAt, now)
            .set(meeting.updatedAt, now)
            .where(meeting.id.eq(meetingId), meeting.status.ne("DELETED"))
            .execute();
    }

    @Transactional
    public void join(Long meetingId, Long memberId) {
        if (attendanceExists(meetingId, memberId)) {
            queryFactory
                .update(attendance)
                .set(attendance.status, "JOINED")
                .set(attendance.updatedAt, OffsetDateTime.now())
                .where(attendance.id.meetingId.eq(meetingId), attendance.id.memberId.eq(memberId))
                .execute();
            return;
        }
        entityManager.persist(new MeetingAttendance(meetingId, memberId));
    }

    @Transactional
    public void cancel(Long meetingId, Long memberId) {
        queryFactory
            .update(attendance)
            .set(attendance.status, "CANCELED")
            .set(attendance.updatedAt, OffsetDateTime.now())
            .where(attendance.id.meetingId.eq(meetingId), attendance.id.memberId.eq(memberId))
            .execute();
    }

    public boolean isJoined(Long meetingId, Long memberId) {
        Long count = queryFactory
            .select(attendance.count())
            .from(attendance)
            .where(
                attendance.id.meetingId.eq(meetingId),
                attendance.id.memberId.eq(memberId),
                attendance.status.eq("JOINED")
            )
            .fetchOne();
        return count != null && count > 0;
    }

    public int joinedCount(Long meetingId) {
        Long count = queryFactory
            .select(attendance.count())
            .from(attendance)
            .where(attendance.id.meetingId.eq(meetingId), attendance.status.eq("JOINED"))
            .fetchOne();
        return count == null ? 0 : count.intValue();
    }

    @Transactional
    public boolean createRegularIfMissing(
        String meetingType,
        String title,
        OffsetDateTime meetingAt,
        String regionText,
        String detailAddress,
        Integer capacity,
        Integer costAmount,
        String description,
        LocalDate targetMonth
    ) {
        Long existing = queryFactory
            .select(meeting.count())
            .from(meeting)
            .where(meeting.meetingType.eq(meetingType), meeting.targetMonth.eq(targetMonth), meeting.status.ne("DELETED"))
            .fetchOne();
        if (existing != null && existing > 0) {
            return false;
        }
        entityManager.persist(new Meeting(
            meetingType,
            title,
            description,
            meetingAt,
            regionText,
            detailAddress,
            capacity,
            costAmount,
            null,
            null,
            targetMonth,
            true
        ));
        return true;
    }

    private List<MeetingSummary> findSummaries(BooleanBuilder where, int limit, int offset, OrderSpecifier<?>... orderSpecifiers) {
        List<Tuple> rows = queryFactory
            .select(summaryFields())
            .from(meeting)
            .leftJoin(coverImage).on(coverImage.id.eq(meeting.coverImageId))
            .where(where)
            .orderBy(orderSpecifiers)
            .limit(limit)
            .offset(offset)
            .fetch();
        List<Long> meetingIds = rows.stream()
            .map(row -> row.get(meeting.id))
            .toList();
        Map<Long, Integer> attendeeCounts = joinedCounts(meetingIds);
        Map<Long, List<String>> previewImageUrls = previewImageUrls(meetingIds, 5);
        return rows.stream()
            .map(row -> mapSummary(row, attendeeCounts, previewImageUrls))
            .toList();
    }

    private Map<Long, Integer> joinedCounts(List<Long> meetingIds) {
        if (meetingIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Integer> counts = new LinkedHashMap<>();
        queryFactory
            .select(attendance.id.meetingId, attendance.count())
            .from(attendance)
            .where(attendance.id.meetingId.in(meetingIds), attendance.status.eq("JOINED"))
            .groupBy(attendance.id.meetingId)
            .fetch()
            .forEach(row -> {
                Long meetingId = row.get(attendance.id.meetingId);
                Long count = row.get(attendance.count());
                counts.put(meetingId, count == null ? 0 : count.intValue());
            });
        return counts;
    }

    private Map<Long, List<String>> previewImageUrls(List<Long> meetingIds, int limit) {
        if (meetingIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<String>> urlsByMeetingId = new LinkedHashMap<>();
        queryFactory
            .select(attendance.id.meetingId, profileImage.publicUrl, attendee.kakaoProfileImageUrl)
            .from(attendance)
            .join(attendee).on(attendee.id.eq(attendance.id.memberId))
            .leftJoin(profileImage).on(profileImage.id.eq(attendee.profileImageId))
            .where(attendance.id.meetingId.in(meetingIds), attendance.status.eq("JOINED"))
            .orderBy(attendance.id.meetingId.asc(), attendance.createdAt.asc())
            .fetch()
            .forEach(row -> {
                Long meetingId = row.get(attendance.id.meetingId);
                List<String> urls = urlsByMeetingId.computeIfAbsent(meetingId, ignored -> new ArrayList<>());
                if (urls.size() < limit) {
                    urls.add(coalesce(row.get(profileImage.publicUrl), row.get(attendee.kakaoProfileImageUrl)));
                }
            });
        return urlsByMeetingId;
    }

    private BooleanBuilder publicMeetingWhere(String type) {
        BooleanBuilder where = new BooleanBuilder();
        if (type != null && !type.isBlank()) {
            where.and(meeting.meetingType.eq(type));
        }
        return where;
    }

    private com.querydsl.core.types.Expression<?>[] summaryFields() {
        return new com.querydsl.core.types.Expression<?>[] {
            meeting.id,
            meeting.meetingType,
            meeting.title,
            meeting.description,
            meeting.meetingAt,
            meeting.regionText,
            meeting.capacity,
            meeting.costAmount,
            meeting.status,
            coverImage.publicUrl
        };
    }

    private com.querydsl.core.types.Expression<?>[] detailFields() {
        return new com.querydsl.core.types.Expression<?>[] {
            meeting.id,
            meeting.meetingType,
            meeting.title,
            meeting.description,
            meeting.meetingAt,
            meeting.regionText,
            meeting.detailAddress,
            meeting.capacity,
            meeting.costAmount,
            meeting.coverImageId,
            coverImage.publicUrl,
            meeting.hostMemberId,
            host.displayType,
            host.realName,
            host.nickname,
            meeting.status,
            meeting.targetMonth,
            meeting.autoGenerated
        };
    }

    private MeetingSummary mapSummary(Tuple row, Map<Long, Integer> attendeeCounts, Map<Long, List<String>> previewImageUrls) {
        Long meetingId = row.get(meeting.id);
        return new MeetingSummary(
            meetingId,
            row.get(meeting.meetingType),
            row.get(meeting.title),
            row.get(meeting.description),
            row.get(meeting.meetingAt),
            row.get(meeting.regionText),
            row.get(meeting.capacity),
            row.get(meeting.costAmount),
            row.get(coverImage.publicUrl),
            row.get(meeting.status),
            attendeeCounts.getOrDefault(meetingId, 0),
            previewImageUrls.getOrDefault(meetingId, List.of())
        );
    }

    private MeetingDetail mapDetail(Tuple row, Long viewerMemberId, List<MeetingAttendee> attendees) {
        Long meetingId = row.get(meeting.id);
        Long hostMemberId = row.get(meeting.hostMemberId);
        return new MeetingDetail(
            meetingId,
            row.get(meeting.meetingType),
            row.get(meeting.title),
            row.get(meeting.description),
            row.get(meeting.meetingAt),
            row.get(meeting.regionText),
            row.get(meeting.detailAddress),
            row.get(meeting.capacity),
            row.get(meeting.costAmount),
            row.get(coverImage.publicUrl),
            row.get(meeting.coverImageId),
            hostMemberId,
            displayName(row, host),
            row.get(meeting.status),
            row.get(meeting.targetMonth),
            Boolean.TRUE.equals(row.get(meeting.autoGenerated)),
            joinedCount(meetingId),
            viewerMemberId != null && isJoined(meetingId, viewerMemberId),
            hostMemberId != null && hostMemberId.equals(viewerMemberId),
            previewImageUrls(meetingId, 5),
            attendees
        );
    }

    private boolean attendanceExists(Long meetingId, Long memberId) {
        Long count = queryFactory
            .select(attendance.count())
            .from(attendance)
            .where(attendance.id.meetingId.eq(meetingId), attendance.id.memberId.eq(memberId))
            .fetchOne();
        return count != null && count > 0;
    }

    private String displayName(Tuple row, QMember memberAlias) {
        String realName = row.get(memberAlias.realName);
        String nickname = row.get(memberAlias.nickname);
        return "REAL_NAME".equals(row.get(memberAlias.displayType)) && realName != null && !realName.isBlank()
            ? realName
            : nickname;
    }

    private String coalesce(String first, String second) {
        return first == null || first.isBlank() ? second : first;
    }
}
