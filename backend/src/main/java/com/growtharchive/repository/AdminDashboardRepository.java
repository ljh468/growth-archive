package com.growtharchive.repository;

import com.growtharchive.domain.book.QBook;
import com.growtharchive.domain.meeting.QMeeting;
import com.growtharchive.domain.meeting.QMeetingReview;
import com.growtharchive.domain.member.QMember;
import com.growtharchive.domain.monthly.QMonthlyActionPlan;
import com.growtharchive.domain.reading.QReadingRecord;
import com.growtharchive.domain.admin.QParticipationAdminNote;
import com.growtharchive.support.KstDateTimes;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class AdminDashboardRepository {

    private static final QMember member = QMember.member;
    private static final QReadingRecord readingRecord = QReadingRecord.readingRecord;
    private static final QMeeting meeting = QMeeting.meeting;
    private static final QMeetingReview meetingReview = QMeetingReview.meetingReview;
    private static final QBook book = QBook.book;
    private static final QMonthlyActionPlan monthlyActionPlan = QMonthlyActionPlan.monthlyActionPlan;
    private static final QParticipationAdminNote adminNote = QParticipationAdminNote.participationAdminNote;

    private final JPAQueryFactory queryFactory;

    public AdminDashboardRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public AdminDashboardCounts counts(LocalDate targetMonth) {
        long activeMemberCount = count(queryFactory
            .select(member.count())
            .from(member)
            .where(member.onboardingCompletedAt.isNotNull(), member.deactivatedAt.isNull())
            .fetchOne());
        long activeReadingRecordCount = count(queryFactory
            .select(readingRecord.count())
            .from(readingRecord)
            .where(readingRecord.status.eq("ACTIVE"))
            .fetchOne());
        long meetingCount = count(queryFactory
            .select(meeting.count())
            .from(meeting)
            .where(meeting.status.ne("DELETED"))
            .fetchOne());
        long activeReviewCount = count(queryFactory
            .select(meetingReview.count())
            .from(meetingReview)
            .where(meetingReview.status.eq("ACTIVE"))
            .fetchOne());
        long unverifiedBookCount = count(queryFactory
            .select(book.count())
            .from(book)
            .where(book.status.eq("UNVERIFIED"))
            .fetchOne());
        List<Long> participationTargetMemberIds = participationTargetMemberIds(targetMonth);
        long participationTargetCount = participationTargetMemberIds.size();
        long participationCompletedCount = completedParticipationMemberIds(participationTargetMemberIds, targetMonth).size();
        return new AdminDashboardCounts(
            activeMemberCount,
            activeReadingRecordCount,
            meetingCount,
            activeReviewCount,
            unverifiedBookCount,
            participationTargetCount,
            participationCompletedCount
        );
    }

    private long count(Long value) {
        return value == null ? 0 : value;
    }

    private List<Long> participationTargetMemberIds(LocalDate targetMonth) {
        return queryFactory
            .select(member.id)
            .from(member)
            .where(
                member.onboardingCompletedAt.isNotNull(),
                member.deactivatedAt.isNull(),
                member.onboardingCompletedAt.lt(KstDateTimes.startOfNextMonth(targetMonth))
            )
            .fetch();
    }

    private Set<Long> completedParticipationMemberIds(List<Long> memberIds, LocalDate targetMonth) {
        Set<Long> completedMemberIds = new LinkedHashSet<>();
        if (memberIds.isEmpty()) {
            return completedMemberIds;
        }
        queryFactory
            .select(readingRecord.memberId)
            .from(readingRecord)
            .where(
                readingRecord.memberId.in(memberIds),
                readingRecord.status.eq("ACTIVE"),
                readingRecord.recordedAt.goe(KstDateTimes.startOfMonth(targetMonth)),
                readingRecord.recordedAt.lt(KstDateTimes.startOfNextMonth(targetMonth))
            )
            .distinct()
            .fetch()
            .forEach(completedMemberIds::add);
        queryFactory
            .select(monthlyActionPlan.memberId)
            .from(monthlyActionPlan)
            .where(
                monthlyActionPlan.memberId.in(memberIds),
                monthlyActionPlan.status.eq("ACTIVE"),
                monthlyActionPlan.targetMonth.eq(targetMonth)
            )
            .distinct()
            .fetch()
            .forEach(completedMemberIds::add);
        queryFactory
            .select(adminNote.memberId)
            .from(adminNote)
            .where(
                adminNote.memberId.in(memberIds),
                adminNote.targetMonth.eq(targetMonth),
                adminNote.manuallyCompletedAt.isNotNull()
            )
            .distinct()
            .fetch()
            .forEach(completedMemberIds::add);
        return completedMemberIds;
    }

    public record AdminDashboardCounts(
        long activeMemberCount,
        long activeReadingRecordCount,
        long meetingCount,
        long activeReviewCount,
        long unverifiedBookCount,
        long participationTargetCount,
        long participationCompletedCount
    ) {
    }
}
