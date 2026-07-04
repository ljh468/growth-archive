package com.growtharchive.repository;

import com.growtharchive.domain.admin.QParticipationAdminNote;
import com.growtharchive.domain.book.QBook;
import com.growtharchive.domain.image.QImageAsset;
import com.growtharchive.domain.meeting.QMeeting;
import com.growtharchive.domain.meeting.QMeetingReview;
import com.growtharchive.domain.member.QInterestTag;
import com.growtharchive.domain.member.QMember;
import com.growtharchive.domain.member.QMemberInterestTag;
import com.growtharchive.domain.monthly.QMonthlyActionPlan;
import com.growtharchive.domain.monthly.QMonthlyReflection;
import com.growtharchive.domain.reading.QReadingRecord;
import com.growtharchive.service.profile.ActivitySummary;
import com.growtharchive.service.profile.GrowthStats;
import com.growtharchive.service.profile.MyDashboard;
import com.growtharchive.service.profile.MyProfile;
import com.growtharchive.service.profile.MonthlyActionPlanShowcase;
import com.growtharchive.service.profile.ProfileCard;
import com.growtharchive.service.profile.ProfileDetail;
import com.growtharchive.service.reading.ReadingRecordDetail;
import com.growtharchive.support.KstDateTimes;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProfileRepository {

    private static final QMember member = QMember.member;
    private static final QImageAsset profileImage = new QImageAsset("profileImage");
    private static final QImageAsset recordImage = new QImageAsset("recordImage");
    private static final QMemberInterestTag memberInterestTag = QMemberInterestTag.memberInterestTag;
    private static final QInterestTag interestTag = QInterestTag.interestTag;
    private static final QReadingRecord readingRecord = QReadingRecord.readingRecord;
    private static final QBook book = QBook.book;
    private static final QMonthlyActionPlan actionPlan = QMonthlyActionPlan.monthlyActionPlan;
    private static final QMonthlyReflection reflection = QMonthlyReflection.monthlyReflection;
    private static final QMeetingReview meetingReview = QMeetingReview.meetingReview;
    private static final QMeeting meeting = QMeeting.meeting;
    private static final QParticipationAdminNote adminNote = QParticipationAdminNote.participationAdminNote;

    private final JPAQueryFactory queryFactory;

    public ProfileRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<ProfileCard> findPeople(Long interestTagId, int limit, int offset, boolean revealPrivateProfile) {
        BooleanBuilder where = activeMemberWhere();
        if (interestTagId != null) {
            where.and(JPAExpressions
                .selectOne()
                .from(memberInterestTag)
                .where(
                    memberInterestTag.id.memberId.eq(member.id),
                    memberInterestTag.id.interestTagId.eq(interestTagId)
                )
                .exists());
        }
        return queryFactory
            .select(member.id, member.role, member.nickname, member.realName, member.oneLineIntro, member.fiftyYearOldMe,
                profileImage.publicUrl, member.kakaoProfileImageUrl)
            .from(member)
            .leftJoin(profileImage).on(profileImage.id.eq(member.profileImageId))
            .where(where)
            .orderBy(
                new OrderSpecifier<>(
                    Order.DESC,
                    JPAExpressions
                    .select(readingRecord.recordedAt.max())
                    .from(readingRecord)
                    .where(readingRecord.memberId.eq(member.id), readingRecord.status.eq("ACTIVE")),
                    OrderSpecifier.NullHandling.NullsLast
                ),
                member.id.desc()
            )
            .limit(limit)
            .offset(offset)
            .fetch()
            .stream()
            .map(row -> toProfileCard(row, revealPrivateProfile))
            .toList();
    }

    public Optional<ProfileDetail> findProfileDetail(Long memberId, boolean includeMemberOnly) {
        Tuple row = queryFactory
            .select(member.id, member.nickname, member.realName, member.oneLineIntro, member.joinReason,
                member.currentConcern, member.threeYearGoal, member.fiftyYearOldMe,
                profileImage.publicUrl, member.kakaoProfileImageUrl)
            .from(member)
            .leftJoin(profileImage).on(profileImage.id.eq(member.profileImageId))
            .where(member.id.eq(memberId), activeMemberWhere())
            .fetchOne();
        if (row == null) {
            return Optional.empty();
        }
        ProfileDetail.MemberOnlyProfile memberOnly = includeMemberOnly
            ? new ProfileDetail.MemberOnlyProfile(
                row.get(member.joinReason),
                row.get(member.currentConcern),
                row.get(member.threeYearGoal),
                findActionPlans(memberId, 3),
                findReflections(memberId, 3)
            )
            : null;
        return Optional.of(new ProfileDetail(
            memberId,
            displayName(row, includeMemberOnly),
            includeMemberOnly ? coalesce(row.get(profileImage.publicUrl), row.get(member.kakaoProfileImageUrl)) : null,
            row.get(member.oneLineIntro),
            findInterestTagNames(memberId),
            row.get(member.fiftyYearOldMe),
            growthStats(memberId),
            findRecentReadingRecords(memberId, 3, includeMemberOnly),
            findMeetingReviews(memberId, 3),
            findPublicActivities(memberId, 5),
            memberOnly
        ));
    }

    public Optional<MyProfile> findMyProfile(Long memberId) {
        Tuple row = queryFactory
            .select(member.id, member.role, member.nickname, member.realName, member.displayType, member.oneLineIntro,
                member.birthDate, member.profileImageId, profileImage.publicUrl, member.kakaoProfileImageUrl,
                member.fiftyYearOldMe, member.joinReason, member.currentConcern, member.threeYearGoal)
            .from(member)
            .leftJoin(profileImage).on(profileImage.id.eq(member.profileImageId))
            .where(member.id.eq(memberId))
            .fetchOne();
        if (row == null) {
            return Optional.empty();
        }
        return Optional.of(new MyProfile(
            memberId,
            row.get(member.role),
            row.get(member.nickname),
            row.get(member.realName),
            row.get(member.displayType),
            myDisplayName(row),
            coalesce(row.get(profileImage.publicUrl), row.get(member.kakaoProfileImageUrl)),
            row.get(member.profileImageId),
            row.get(member.oneLineIntro),
            row.get(member.birthDate),
            findInterestTagIds(memberId),
            row.get(member.fiftyYearOldMe),
            row.get(member.joinReason),
            row.get(member.currentConcern),
            row.get(member.threeYearGoal)
        ));
    }

    @Transactional
    public void updateMyProfile(
        Long memberId,
        String nickname,
        String oneLineIntro,
        String displayType,
        String realName,
        LocalDate birthDate,
        String futureMeAt50,
        String joinReason,
        String currentConcern,
        String threeYearGoal,
        Long profileImageId
    ) {
        queryFactory
            .update(member)
            .set(member.nickname, nickname)
            .set(member.oneLineIntro, oneLineIntro)
            .set(member.displayType, displayType)
            .set(member.realName, realName)
            .set(member.birthDate, birthDate)
            .set(member.fiftyYearOldMe, futureMeAt50)
            .set(member.joinReason, joinReason)
            .set(member.currentConcern, currentConcern)
            .set(member.threeYearGoal, threeYearGoal)
            .set(member.profileImageId, profileImageId)
            .set(member.updatedAt, OffsetDateTime.now())
            .where(member.id.eq(memberId))
            .execute();
    }

    public MyDashboard findDashboard(Long memberId, LocalDate month) {
        MyProfile profile = findMyProfile(memberId).orElseThrow();
        Long readingRecordCount = countReadingRecordsForMonth(memberId, month);
        Long actionPlanCount = countActionPlansForMonth(memberId, month);
        boolean calculationTarget = isParticipationTarget(memberId, month);
        boolean completed = calculationTarget && (readingRecordCount > 0 || actionPlanCount > 0 || hasManualCompletion(memberId, month));
        return new MyDashboard(
            new MyDashboard.DashboardProfile(profile.memberId(), profile.role(), profile.displayName(), profile.profileImageUrl(), profile.oneLineIntro()),
            new MyDashboard.DashboardParticipation(
                month.toString().substring(0, 7),
                readingRecordCount,
                actionPlanCount,
                completed,
                calculationTarget && !completed
            ),
            findDashboardReadingRecord(memberId, month),
            findDashboardReadingRecords(memberId, month),
            findDashboardActionPlan(memberId, month),
            growthStats(memberId),
            findPublicActivities(memberId, 5)
        );
    }

    public List<MonthlyActionPlanShowcase> findMonthlyActionPlans(LocalDate month, int limit) {
        return queryFactory
            .select(actionPlan.id, actionPlan.memberId, member.nickname, member.realName, profileImage.publicUrl,
                member.kakaoProfileImageUrl, actionPlan.targetMonth, actionPlan.title, actionPlan.content, actionPlan.updatedAt)
            .from(actionPlan)
            .join(member).on(member.id.eq(actionPlan.memberId))
            .leftJoin(profileImage).on(profileImage.id.eq(member.profileImageId))
            .where(actionPlan.targetMonth.eq(month), actionPlan.status.eq("ACTIVE"), activeMemberWhere())
            .orderBy(actionPlan.updatedAt.desc(), actionPlan.id.desc())
            .limit(limit)
            .fetch()
            .stream()
            .map(row -> new MonthlyActionPlanShowcase(
                row.get(actionPlan.id),
                row.get(actionPlan.memberId),
                displayName(row, true),
                coalesce(row.get(profileImage.publicUrl), row.get(member.kakaoProfileImageUrl)),
                row.get(actionPlan.targetMonth).toString().substring(0, 7),
                row.get(actionPlan.title),
                row.get(actionPlan.content),
                row.get(actionPlan.updatedAt)
            ))
            .toList();
    }

    private ProfileCard toProfileCard(Tuple row, boolean revealPrivateProfile) {
        Long memberId = row.get(member.id);
        return new ProfileCard(
            memberId,
            row.get(member.role),
            displayName(row, revealPrivateProfile),
            revealPrivateProfile ? coalesce(row.get(profileImage.publicUrl), row.get(member.kakaoProfileImageUrl)) : null,
            row.get(member.oneLineIntro),
            findInterestTagNames(memberId),
            summarize(row.get(member.fiftyYearOldMe), 90),
            growthStats(memberId),
            latestReadingActivity(memberId)
        );
    }

    private boolean isParticipationTarget(Long memberId, LocalDate month) {
        OffsetDateTime onboardingCompletedAt = queryFactory
            .select(member.onboardingCompletedAt)
            .from(member)
            .where(member.id.eq(memberId), member.deactivatedAt.isNull())
            .fetchOne();
        if (onboardingCompletedAt == null) {
            return false;
        }
        LocalDate joinedMonth = KstDateTimes.monthOf(onboardingCompletedAt);
        return !joinedMonth.isAfter(month);
    }

    private boolean hasManualCompletion(Long memberId, LocalDate month) {
        OffsetDateTime manuallyCompletedAt = queryFactory
            .select(adminNote.manuallyCompletedAt)
            .from(adminNote)
            .where(
                adminNote.memberId.eq(memberId),
                adminNote.targetMonth.eq(month),
                adminNote.manuallyCompletedAt.isNotNull()
            )
            .fetchOne();
        return manuallyCompletedAt != null;
    }

    private Long countReadingRecordsForMonth(Long memberId, LocalDate month) {
        Long count = queryFactory
            .select(readingRecord.count())
            .from(readingRecord)
            .where(
                readingRecord.memberId.eq(memberId),
                readingRecord.status.eq("ACTIVE"),
                readingRecord.recordedAt.goe(KstDateTimes.startOfMonth(month)),
                readingRecord.recordedAt.lt(KstDateTimes.startOfNextMonth(month))
            )
            .fetchOne();
        return zeroIfNull(count);
    }

    private Long countActionPlansForMonth(Long memberId, LocalDate month) {
        Long count = queryFactory
            .select(actionPlan.count())
            .from(actionPlan)
            .where(actionPlan.memberId.eq(memberId), actionPlan.status.eq("ACTIVE"), actionPlan.targetMonth.eq(month))
            .fetchOne();
        return zeroIfNull(count);
    }

    private MyDashboard.DashboardReadingRecord findDashboardReadingRecord(Long memberId, LocalDate month) {
        Tuple row = queryFactory
            .select(readingRecord.id, readingRecord.bookId, book.title, readingRecord.oneLineReview, readingRecord.blogUrl, readingRecord.recordedAt)
            .from(readingRecord)
            .join(book).on(book.id.eq(readingRecord.bookId))
            .where(
                readingRecord.memberId.eq(memberId),
                readingRecord.status.eq("ACTIVE"),
                readingRecord.recordedAt.goe(KstDateTimes.startOfMonth(month)),
                readingRecord.recordedAt.lt(KstDateTimes.startOfNextMonth(month))
            )
            .orderBy(readingRecord.recordedAt.desc(), readingRecord.id.desc())
            .limit(1)
            .fetchOne();
        if (row == null) {
            return null;
        }
        return new MyDashboard.DashboardReadingRecord(
            row.get(readingRecord.id),
            row.get(readingRecord.bookId),
            row.get(book.title),
            row.get(readingRecord.oneLineReview),
            row.get(readingRecord.blogUrl),
            row.get(readingRecord.recordedAt)
        );
    }

    private List<MyDashboard.DashboardReadingRecord> findDashboardReadingRecords(Long memberId, LocalDate month) {
        return queryFactory
            .select(readingRecord.id, readingRecord.bookId, book.title, readingRecord.oneLineReview, readingRecord.blogUrl, readingRecord.recordedAt)
            .from(readingRecord)
            .join(book).on(book.id.eq(readingRecord.bookId))
            .where(
                readingRecord.memberId.eq(memberId),
                readingRecord.status.eq("ACTIVE"),
                readingRecord.recordedAt.goe(KstDateTimes.startOfMonth(month)),
                readingRecord.recordedAt.lt(KstDateTimes.startOfNextMonth(month))
            )
            .orderBy(readingRecord.recordedAt.desc(), readingRecord.id.desc())
            .fetch()
            .stream()
            .map(row -> new MyDashboard.DashboardReadingRecord(
                row.get(readingRecord.id),
                row.get(readingRecord.bookId),
                row.get(book.title),
                row.get(readingRecord.oneLineReview),
                row.get(readingRecord.blogUrl),
                row.get(readingRecord.recordedAt)
            ))
            .toList();
    }

    private MyDashboard.DashboardActionPlan findDashboardActionPlan(Long memberId, LocalDate month) {
        Tuple row = queryFactory
            .select(actionPlan.id, actionPlan.targetMonth, actionPlan.title, actionPlan.content, actionPlan.updatedAt)
            .from(actionPlan)
            .where(actionPlan.memberId.eq(memberId), actionPlan.status.eq("ACTIVE"), actionPlan.targetMonth.eq(month))
            .fetchOne();
        if (row == null) {
            return null;
        }
        return new MyDashboard.DashboardActionPlan(
            row.get(actionPlan.id),
            row.get(actionPlan.targetMonth).toString().substring(0, 7),
            row.get(actionPlan.title),
            row.get(actionPlan.content),
            row.get(actionPlan.updatedAt)
        );
    }

    private GrowthStats growthStats(Long memberId) {
        return new GrowthStats(
            countReadingRecords(memberId),
            countActionPlans(memberId),
            countReflections(memberId),
            countMeetingReviews(memberId),
            countSmallMeetings(memberId)
        );
    }

    private List<ReadingRecordDetail> findRecentReadingRecords(Long memberId, int limit, boolean includeMemberOnly) {
        return queryFactory
            .select(
                readingRecord.id,
                readingRecord.memberId,
                member.nickname,
                member.realName,
                profileImage.publicUrl,
                member.kakaoProfileImageUrl,
                readingRecord.bookId,
                book.title,
                book.authorsText,
                book.thumbnailUrl,
                readingRecord.rating,
                readingRecord.oneLineReview,
                readingRecord.blogUrl,
                readingRecord.representativeImageId,
                recordImage.publicUrl,
                readingRecord.status,
                readingRecord.recordedAt,
                readingRecord.createdAt
            )
            .from(readingRecord)
            .join(book).on(book.id.eq(readingRecord.bookId))
            .join(member).on(member.id.eq(readingRecord.memberId))
            .leftJoin(recordImage).on(recordImage.id.eq(readingRecord.representativeImageId))
            .leftJoin(profileImage).on(profileImage.id.eq(member.profileImageId))
            .where(readingRecord.memberId.eq(memberId), readingRecord.status.eq("ACTIVE"))
            .orderBy(readingRecord.recordedAt.desc())
            .limit(limit)
            .fetch()
            .stream()
            .map(row -> new ReadingRecordDetail(
                row.get(readingRecord.id),
                row.get(readingRecord.memberId),
                displayName(row, includeMemberOnly),
                includeMemberOnly ? coalesce(row.get(profileImage.publicUrl), row.get(member.kakaoProfileImageUrl)) : null,
                row.get(readingRecord.bookId),
                row.get(book.title),
                row.get(book.authorsText),
                row.get(book.thumbnailUrl),
                row.get(readingRecord.rating) == null ? null : row.get(readingRecord.rating).intValue(),
                row.get(readingRecord.oneLineReview),
                row.get(readingRecord.blogUrl),
                row.get(readingRecord.representativeImageId),
                row.get(recordImage.publicUrl),
                row.get(readingRecord.status),
                row.get(readingRecord.recordedAt),
                row.get(readingRecord.createdAt)
            ))
            .toList();
    }

    private List<ActivitySummary> findMeetingReviews(Long memberId, int limit) {
        return queryFactory
            .select(meetingReview.id, meetingReview.title, meetingReview.createdAt)
            .from(meetingReview)
            .where(meetingReview.memberId.eq(memberId), meetingReview.status.eq("ACTIVE"))
            .orderBy(meetingReview.createdAt.desc())
            .limit(limit)
            .fetch()
            .stream()
            .map(row -> new ActivitySummary(
                "MEETING_REVIEW",
                row.get(meetingReview.title),
                "/reviews/" + row.get(meetingReview.id),
                row.get(meetingReview.createdAt)
            ))
            .toList();
    }

    private List<ActivitySummary> findActionPlans(Long memberId, int limit) {
        return queryFactory
            .select(actionPlan.title, actionPlan.targetMonth, actionPlan.createdAt)
            .from(actionPlan)
            .where(actionPlan.memberId.eq(memberId), actionPlan.status.eq("ACTIVE"))
            .orderBy(actionPlan.targetMonth.desc())
            .limit(limit)
            .fetch()
            .stream()
            .map(row -> {
                String title = row.get(actionPlan.title);
                LocalDate targetMonth = row.get(actionPlan.targetMonth);
                return new ActivitySummary(
                    "ACTION_PLAN",
                    title == null || title.isBlank() ? targetMonth.toString().substring(0, 7) + " 액션플랜" : title,
                    "/mypage/action-plans",
                    row.get(actionPlan.createdAt)
                );
            })
            .toList();
    }

    private List<ActivitySummary> findReflections(Long memberId, int limit) {
        return queryFactory
            .select(reflection.targetMonth, reflection.createdAt)
            .from(reflection)
            .where(reflection.memberId.eq(memberId), reflection.status.eq("ACTIVE"))
            .orderBy(reflection.targetMonth.desc())
            .limit(limit)
            .fetch()
            .stream()
            .map(row -> new ActivitySummary(
                "MONTHLY_REFLECTION",
                row.get(reflection.targetMonth).toString().substring(0, 7) + " 회고",
                "/mypage/reflections",
                row.get(reflection.createdAt)
            ))
            .toList();
    }

    private List<ActivitySummary> findPublicActivities(Long memberId, int limit) {
        List<ActivitySummary> readingActivities = queryFactory
            .select(readingRecord.bookId, book.title, readingRecord.recordedAt)
            .from(readingRecord)
            .join(book).on(book.id.eq(readingRecord.bookId))
            .where(readingRecord.memberId.eq(memberId), readingRecord.status.eq("ACTIVE"))
            .fetch()
            .stream()
            .map(row -> new ActivitySummary(
                "READING_RECORD",
                row.get(book.title),
                "/books/" + row.get(readingRecord.bookId),
                row.get(readingRecord.recordedAt)
            ))
            .toList();
        List<ActivitySummary> reviewActivities = queryFactory
            .select(meetingReview.id, meetingReview.title, meetingReview.createdAt)
            .from(meetingReview)
            .where(meetingReview.memberId.eq(memberId), meetingReview.status.eq("ACTIVE"))
            .fetch()
            .stream()
            .map(row -> new ActivitySummary(
                "MEETING_REVIEW",
                row.get(meetingReview.title),
                "/reviews/" + row.get(meetingReview.id),
                row.get(meetingReview.createdAt)
            ))
            .toList();
        return Stream.concat(readingActivities.stream(), reviewActivities.stream())
            .sorted(Comparator.comparing(ActivitySummary::occurredAt).reversed())
            .limit(limit)
            .toList();
    }

    private ActivitySummary latestReadingActivity(Long memberId) {
        Tuple row = queryFactory
            .select(readingRecord.bookId, book.title, readingRecord.recordedAt)
            .from(readingRecord)
            .join(book).on(book.id.eq(readingRecord.bookId))
            .where(readingRecord.memberId.eq(memberId), readingRecord.status.eq("ACTIVE"))
            .orderBy(readingRecord.recordedAt.desc())
            .limit(1)
            .fetchOne();
        if (row == null) {
            return null;
        }
        return new ActivitySummary(
            "READING_RECORD",
            row.get(book.title),
            "/books/" + row.get(readingRecord.bookId),
            row.get(readingRecord.recordedAt)
        );
    }

    private List<String> findInterestTagNames(Long memberId) {
        return queryFactory
            .select(interestTag.name)
            .from(memberInterestTag)
            .join(interestTag).on(interestTag.id.eq(memberInterestTag.id.interestTagId))
            .where(memberInterestTag.id.memberId.eq(memberId), interestTag.active.isTrue())
            .orderBy(interestTag.displayOrder.asc())
            .fetch();
    }

    private List<Long> findInterestTagIds(Long memberId) {
        return queryFactory
            .select(interestTag.id)
            .from(memberInterestTag)
            .join(interestTag).on(interestTag.id.eq(memberInterestTag.id.interestTagId))
            .where(memberInterestTag.id.memberId.eq(memberId), interestTag.active.isTrue())
            .orderBy(interestTag.displayOrder.asc())
            .fetch();
    }

    private Long countReadingRecords(Long memberId) {
        Long count = queryFactory
            .select(readingRecord.count())
            .from(readingRecord)
            .where(readingRecord.memberId.eq(memberId), readingRecord.status.eq("ACTIVE"))
            .fetchOne();
        return zeroIfNull(count);
    }

    private Long countActionPlans(Long memberId) {
        Long count = queryFactory
            .select(actionPlan.count())
            .from(actionPlan)
            .where(actionPlan.memberId.eq(memberId), actionPlan.status.eq("ACTIVE"))
            .fetchOne();
        return zeroIfNull(count);
    }

    private Long countReflections(Long memberId) {
        Long count = queryFactory
            .select(reflection.count())
            .from(reflection)
            .where(reflection.memberId.eq(memberId), reflection.status.eq("ACTIVE"))
            .fetchOne();
        return zeroIfNull(count);
    }

    private Long countMeetingReviews(Long memberId) {
        Long count = queryFactory
            .select(meetingReview.count())
            .from(meetingReview)
            .where(meetingReview.memberId.eq(memberId), meetingReview.status.eq("ACTIVE"))
            .fetchOne();
        return zeroIfNull(count);
    }

    private Long countSmallMeetings(Long memberId) {
        Long count = queryFactory
            .select(meeting.count())
            .from(meeting)
            .where(meeting.hostMemberId.eq(memberId), meeting.meetingType.eq("SMALL"), meeting.status.ne("DELETED"))
            .fetchOne();
        return zeroIfNull(count);
    }

    private BooleanBuilder activeMemberWhere() {
        return new BooleanBuilder(member.onboardingCompletedAt.isNotNull())
            .and(member.deactivatedAt.isNull());
    }

    private String displayName(Tuple row, boolean revealPrivateProfile) {
        if (!revealPrivateProfile) {
            return row.get(member.nickname);
        }
        String realName = row.get(member.realName);
        return realName != null && !realName.isBlank() ? realName : row.get(member.nickname);
    }

    private String myDisplayName(Tuple row) {
        String realName = row.get(member.realName);
        return "REAL_NAME".equals(row.get(member.displayType)) && realName != null && !realName.isBlank()
            ? realName
            : row.get(member.nickname);
    }

    private String summarize(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private Long zeroIfNull(Long count) {
        return count == null ? 0 : count;
    }

    private String coalesce(String first, String second) {
        return first == null || first.isBlank() ? second : first;
    }
}
