package com.growtharchive.repository;

import com.growtharchive.domain.admin.ParticipationAdminNote;
import com.growtharchive.domain.admin.QParticipationAdminNote;
import com.growtharchive.domain.image.QImageAsset;
import com.growtharchive.domain.member.QMember;
import com.growtharchive.domain.monthly.QMonthlyActionPlan;
import com.growtharchive.domain.reading.QReadingRecord;
import com.growtharchive.service.participation.AdminParticipationMemberView;
import com.growtharchive.service.participation.ParticipationStatusView;
import com.growtharchive.support.KstDateTimes;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ParticipationRepository {

    public static final String COFFEE_SUPPORT_ITEM = "투썸 아메리카노 1잔";

    private static final QMember member = QMember.member;
    private static final QImageAsset profileImage = new QImageAsset("profileImage");
    private static final QReadingRecord readingRecord = QReadingRecord.readingRecord;
    private static final QMonthlyActionPlan actionPlan = QMonthlyActionPlan.monthlyActionPlan;
    private static final QParticipationAdminNote adminNote = QParticipationAdminNote.participationAdminNote;

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    public ParticipationRepository(JPAQueryFactory queryFactory, EntityManager entityManager) {
        this.queryFactory = queryFactory;
        this.entityManager = entityManager;
    }

    public ParticipationStatusView getMemberStatus(Long memberId, LocalDate targetMonth) {
        Tuple row = queryFactory
            .select(member.id, member.onboardingCompletedAt)
            .from(member)
            .where(member.id.eq(memberId), member.deactivatedAt.isNull())
            .fetchOne();
        if (row == null) {
            return null;
        }
        long readingRecordCount = readingRecordCount(memberId, targetMonth);
        long actionPlanCount = actionPlanCount(memberId, targetMonth);
        boolean calculationTarget = isCalculationTarget(row.get(member.onboardingCompletedAt), targetMonth);
        boolean manuallyCompleted = hasManualCompletion(memberId, targetMonth);
        return toStatus(targetMonth, readingRecordCount, actionPlanCount, calculationTarget, manuallyCompleted);
    }

    public List<AdminParticipationMemberView> getAdminMembers(LocalDate targetMonth) {
        List<Tuple> rows = queryFactory
            .select(
                member.id,
                member.displayType,
                member.realName,
                member.nickname,
                profileImage.publicUrl,
                member.kakaoProfileImageUrl,
                member.onboardingCompletedAt,
                adminNote.manuallyCompletedAt,
                adminNote.note
            )
            .from(member)
            .leftJoin(profileImage).on(profileImage.id.eq(member.profileImageId))
            .leftJoin(adminNote).on(adminNote.memberId.eq(member.id), adminNote.targetMonth.eq(targetMonth))
            .where(member.onboardingCompletedAt.isNotNull(), member.deactivatedAt.isNull())
            .orderBy(member.onboardingCompletedAt.asc(), member.id.asc())
            .fetch();
        List<Long> memberIds = rows.stream()
            .map(row -> row.get(member.id))
            .toList();
        Map<Long, Long> readingRecordCounts = readingRecordCounts(memberIds, targetMonth);
        Map<Long, Long> actionPlanCounts = actionPlanCounts(memberIds, targetMonth);
        return rows.stream()
            .map(row -> toAdminMember(row, targetMonth, readingRecordCounts, actionPlanCounts))
            .toList();
    }

    @Transactional
    public void saveNote(Long adminMemberId, Long memberId, LocalDate targetMonth, String note) {
        Long existingId = queryFactory
            .select(adminNote.id)
            .from(adminNote)
            .where(adminNote.memberId.eq(memberId), adminNote.targetMonth.eq(targetMonth))
            .fetchOne();
        if (existingId == null) {
            entityManager.persist(new ParticipationAdminNote(memberId, targetMonth, note, adminMemberId));
            return;
        }
        queryFactory
            .update(adminNote)
            .set(adminNote.note, note)
            .set(adminNote.createdByMemberId, adminMemberId)
            .set(adminNote.updatedAt, OffsetDateTime.now())
            .where(adminNote.id.eq(existingId))
            .execute();
    }

    @Transactional
    public void markManualCompletion(Long adminMemberId, Long memberId, LocalDate targetMonth) {
        Long existingId = queryFactory
            .select(adminNote.id)
            .from(adminNote)
            .where(adminNote.memberId.eq(memberId), adminNote.targetMonth.eq(targetMonth))
            .fetchOne();
        OffsetDateTime now = OffsetDateTime.now(KstDateTimes.KST);
        if (existingId == null) {
            ParticipationAdminNote created = new ParticipationAdminNote(memberId, targetMonth, "", adminMemberId);
            created.markManuallyCompleted(adminMemberId, now);
            entityManager.persist(created);
            return;
        }
        queryFactory
            .update(adminNote)
            .set(adminNote.manuallyCompletedAt, now)
            .set(adminNote.manuallyCompletedByMemberId, adminMemberId)
            .set(adminNote.updatedAt, now)
            .where(adminNote.id.eq(existingId))
            .execute();
    }

    @Transactional
    public void clearManualCompletion(Long memberId, LocalDate targetMonth) {
        queryFactory
            .update(adminNote)
            .setNull(adminNote.manuallyCompletedAt)
            .setNull(adminNote.manuallyCompletedByMemberId)
            .set(adminNote.updatedAt, OffsetDateTime.now(KstDateTimes.KST))
            .where(adminNote.memberId.eq(memberId), adminNote.targetMonth.eq(targetMonth))
            .execute();
    }

    private ParticipationStatusView toStatus(
        LocalDate targetMonth,
        long readingRecordCount,
        long actionPlanCount,
        boolean calculationTarget,
        boolean manuallyCompleted
    ) {
        boolean completed = calculationTarget && (readingRecordCount > 0 || actionPlanCount > 0 || manuallyCompleted);
        return new ParticipationStatusView(
            targetMonth.toString().substring(0, 7),
            readingRecordCount,
            actionPlanCount,
            calculationTarget,
            completed,
            calculationTarget && !completed,
            COFFEE_SUPPORT_ITEM
        );
    }

    private AdminParticipationMemberView toAdminMember(
        Tuple row,
        LocalDate targetMonth,
        Map<Long, Long> readingRecordCounts,
        Map<Long, Long> actionPlanCounts
    ) {
        Long memberId = row.get(member.id);
        long readingRecordCount = readingRecordCounts.getOrDefault(memberId, 0L);
        boolean hasActionPlan = actionPlanCounts.getOrDefault(memberId, 0L) > 0;
        boolean calculationTarget = isCalculationTarget(row.get(member.onboardingCompletedAt), targetMonth);
        boolean manuallyCompleted = row.get(adminNote.manuallyCompletedAt) != null;
        boolean completed = calculationTarget && (readingRecordCount > 0 || hasActionPlan || manuallyCompleted);
        String profileImageUrl = row.get(profileImage.publicUrl) == null
            ? row.get(member.kakaoProfileImageUrl)
            : row.get(profileImage.publicUrl);
        return new AdminParticipationMemberView(
            memberId,
            displayName(row),
            row.get(member.nickname),
            profileImageUrl,
            readingRecordCount,
            hasActionPlan,
            calculationTarget,
            completed,
            manuallyCompleted,
            calculationTarget && !completed,
            COFFEE_SUPPORT_ITEM,
            row.get(adminNote.note)
        );
    }

    private long readingRecordCount(Long memberId, LocalDate targetMonth) {
        Long count = queryFactory
            .select(readingRecord.count())
            .from(readingRecord)
            .where(
                readingRecord.memberId.eq(memberId),
                readingRecord.status.eq("ACTIVE"),
                readingRecord.recordedAt.goe(KstDateTimes.startOfMonth(targetMonth)),
                readingRecord.recordedAt.lt(KstDateTimes.startOfNextMonth(targetMonth))
            )
            .fetchOne();
        return count == null ? 0 : count;
    }

    private Map<Long, Long> readingRecordCounts(List<Long> memberIds, LocalDate targetMonth) {
        Map<Long, Long> counts = new LinkedHashMap<>();
        if (memberIds.isEmpty()) {
            return counts;
        }
        queryFactory
            .select(readingRecord.memberId, readingRecord.count())
            .from(readingRecord)
            .where(
                readingRecord.memberId.in(memberIds),
                readingRecord.status.eq("ACTIVE"),
                readingRecord.recordedAt.goe(KstDateTimes.startOfMonth(targetMonth)),
                readingRecord.recordedAt.lt(KstDateTimes.startOfNextMonth(targetMonth))
            )
            .groupBy(readingRecord.memberId)
            .fetch()
            .forEach(row -> counts.put(row.get(readingRecord.memberId), zeroIfNull(row.get(readingRecord.count()))));
        return counts;
    }

    private long actionPlanCount(Long memberId, LocalDate targetMonth) {
        Long count = queryFactory
            .select(actionPlan.count())
            .from(actionPlan)
            .where(
                actionPlan.memberId.eq(memberId),
                actionPlan.status.eq("ACTIVE"),
                actionPlan.targetMonth.eq(targetMonth)
            )
            .fetchOne();
        return count == null ? 0 : count;
    }

    private Map<Long, Long> actionPlanCounts(List<Long> memberIds, LocalDate targetMonth) {
        Map<Long, Long> counts = new LinkedHashMap<>();
        if (memberIds.isEmpty()) {
            return counts;
        }
        queryFactory
            .select(actionPlan.memberId, actionPlan.count())
            .from(actionPlan)
            .where(
                actionPlan.memberId.in(memberIds),
                actionPlan.status.eq("ACTIVE"),
                actionPlan.targetMonth.eq(targetMonth)
            )
            .groupBy(actionPlan.memberId)
            .fetch()
            .forEach(row -> counts.put(row.get(actionPlan.memberId), zeroIfNull(row.get(actionPlan.count()))));
        return counts;
    }

    private String displayName(Tuple row) {
        String realName = row.get(member.realName);
        return realName != null && !realName.isBlank()
            ? realName
            : row.get(member.nickname);
    }

    private boolean hasManualCompletion(Long memberId, LocalDate targetMonth) {
        OffsetDateTime manuallyCompletedAt = queryFactory
            .select(adminNote.manuallyCompletedAt)
            .from(adminNote)
            .where(
                adminNote.memberId.eq(memberId),
                adminNote.targetMonth.eq(targetMonth),
                adminNote.manuallyCompletedAt.isNotNull()
            )
            .fetchOne();
        return manuallyCompletedAt != null;
    }

    private boolean isCalculationTarget(OffsetDateTime onboardingCompletedAt, LocalDate targetMonth) {
        if (onboardingCompletedAt == null) {
            return false;
        }
        LocalDate joinedMonth = KstDateTimes.monthOf(onboardingCompletedAt);
        return !joinedMonth.isAfter(targetMonth);
    }

    private long zeroIfNull(Long count) {
        return count == null ? 0 : count;
    }
}
