package com.growtharchive.repository;

import com.growtharchive.domain.image.QImageAsset;
import com.growtharchive.domain.member.Member;
import com.growtharchive.domain.member.QInterestTag;
import com.growtharchive.domain.member.QMember;
import com.growtharchive.domain.member.QMemberInterestTag;
import com.growtharchive.security.MemberPrincipal;
import com.growtharchive.support.KstDateTimes;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
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
public class MemberRepository {

    private static final QMember member = QMember.member;
    private static final QImageAsset profileImage = new QImageAsset("profileImage");
    private static final QMemberInterestTag memberInterestTag = QMemberInterestTag.memberInterestTag;
    private static final QInterestTag interestTag = QInterestTag.interestTag;

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    public MemberRepository(JPAQueryFactory queryFactory, EntityManager entityManager) {
        this.queryFactory = queryFactory;
        this.entityManager = entityManager;
    }

    @Transactional
    public Long createOnboardedMember(
        String role,
        String nickname,
        String oneLineIntro,
        String displayType,
        String realName,
        LocalDate birthDate,
        Long profileImageId,
        String kakaoProfileImageUrl,
        String fiftyYearOldMe,
        String joinReason,
        String currentConcern,
        String threeYearGoal
    ) {
        Member created = new Member(
            role,
            nickname,
            oneLineIntro,
            displayType,
            realName,
            birthDate,
            profileImageId,
            kakaoProfileImageUrl,
            fiftyYearOldMe,
            joinReason,
            currentConcern,
            threeYearGoal
        );
        entityManager.persist(created);
        entityManager.flush();
        return created.getId();
    }

    public Optional<MemberPrincipal> findPrincipalById(Long memberId) {
        Tuple row = queryFactory
            .select(
                member.id,
                member.role,
                member.displayType,
                member.realName,
                member.nickname,
                member.kakaoProfileImageUrl,
                member.inviteVerifiedAt,
                member.termsAgreedAt,
                member.privacyAgreedAt,
                member.onboardingCompletedAt,
                member.deactivatedAt
            )
            .from(member)
            .where(member.id.eq(memberId))
            .fetchOne();
        return row == null ? Optional.empty() : Optional.of(mapPrincipal(row));
    }

    @Transactional
    public void updateKakaoProfileImageUrl(Long memberId, String kakaoProfileImageUrl) {
        if (kakaoProfileImageUrl == null || kakaoProfileImageUrl.isBlank()) {
            return;
        }
        queryFactory
            .update(member)
            .set(member.kakaoProfileImageUrl, kakaoProfileImageUrl)
            .set(member.updatedAt, OffsetDateTime.now())
            .where(member.id.eq(memberId))
            .execute();
    }

    public boolean existsNickname(String nickname) {
        Long count = queryFactory
            .select(member.count())
            .from(member)
            .where(member.nickname.lower().eq(nickname.toLowerCase()))
            .fetchOne();
        return count != null && count > 0;
    }

    public boolean existsNicknameForOtherMember(String nickname, Long memberId) {
        Long count = queryFactory
            .select(member.count())
            .from(member)
            .where(member.nickname.lower().eq(nickname.toLowerCase()), member.id.ne(memberId))
            .fetchOne();
        return count != null && count > 0;
    }

    @Transactional
    public void markInviteVerified(Long memberId) {
        OffsetDateTime now = OffsetDateTime.now();
        queryFactory
            .update(member)
            .set(member.inviteVerifiedAt, now)
            .set(member.updatedAt, now)
            .where(member.id.eq(memberId), member.inviteVerifiedAt.isNull())
            .execute();
    }

    @Transactional
    public void agreeTerms(Long memberId) {
        OffsetDateTime now = OffsetDateTime.now();
        queryFactory
            .update(member)
            .set(member.termsAgreedAt, now)
            .set(member.privacyAgreedAt, now)
            .set(member.updatedAt, now)
            .where(member.id.eq(memberId), member.termsAgreedAt.isNull().or(member.privacyAgreedAt.isNull()))
            .execute();
    }

    @Transactional
    public void completeOnboarding(
        Long memberId,
        String nickname,
        String oneLineIntro,
        String displayType,
        String realName,
        LocalDate birthDate,
        Long profileImageId,
        String fiftyYearOldMe,
        String joinReason,
        String currentConcern,
        String threeYearGoal
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        queryFactory
            .update(member)
            .set(member.nickname, nickname)
            .set(member.oneLineIntro, oneLineIntro)
            .set(member.displayType, displayType)
            .set(member.realName, realName)
            .set(member.birthDate, birthDate)
            .set(member.profileImageId, profileImageId)
            .set(member.fiftyYearOldMe, fiftyYearOldMe)
            .set(member.joinReason, joinReason)
            .set(member.currentConcern, currentConcern)
            .set(member.threeYearGoal, threeYearGoal)
            .set(member.participationStartMonth, KstDateTimes.currentMonth().plusMonths(1))
            .set(member.onboardingCompletedAt, now)
            .set(member.updatedAt, now)
            .where(member.id.eq(memberId))
            .execute();
    }

    @Transactional
    public void deactivate(Long memberId) {
        OffsetDateTime now = OffsetDateTime.now();
        queryFactory
            .update(member)
            .set(member.deactivatedAt, now)
            .set(member.updatedAt, now)
            .where(member.id.eq(memberId), member.deactivatedAt.isNull())
            .execute();
    }

    @Transactional
    public void reactivate(Long memberId) {
        queryFactory
            .update(member)
            .setNull(member.deactivatedAt)
            .set(member.updatedAt, OffsetDateTime.now())
            .where(member.id.eq(memberId))
            .execute();
    }

    @Transactional
    public void withdrawAndRedact(Long memberId) {
        OffsetDateTime now = OffsetDateTime.now();
        queryFactory
            .update(member)
            .set(member.realName, "탈퇴회원")
            .setNull(member.birthDate)
            .setNull(member.profileImageId)
            .setNull(member.kakaoProfileImageUrl)
            .set(member.deactivatedAt, now)
            .set(member.withdrawnAt, now)
            .set(member.personalDataRedactedAt, now)
            .set(member.updatedAt, now)
            .where(member.id.eq(memberId))
            .execute();
    }

    public boolean isWithdrawn(Long memberId) {
        OffsetDateTime withdrawnAt = queryFactory
            .select(member.withdrawnAt)
            .from(member)
            .where(member.id.eq(memberId))
            .fetchOne();
        return withdrawnAt != null;
    }

    public Optional<OnboardingDraftRow> findWithdrawnOnboardingDraft(Long memberId) {
        Tuple row = queryFactory
            .select(
                member.nickname,
                member.oneLineIntro,
                member.displayType,
                member.fiftyYearOldMe,
                member.joinReason,
                member.currentConcern,
                member.threeYearGoal
            )
            .from(member)
            .where(member.id.eq(memberId), member.withdrawnAt.isNotNull())
            .fetchOne();
        if (row == null) {
            return Optional.empty();
        }
        List<Long> interestTagIds = queryFactory
            .select(memberInterestTag.id.interestTagId)
            .from(memberInterestTag)
            .join(interestTag).on(interestTag.id.eq(memberInterestTag.id.interestTagId))
            .where(memberInterestTag.id.memberId.eq(memberId), interestTag.active.isTrue())
            .orderBy(interestTag.displayOrder.asc())
            .fetch();
        return Optional.of(new OnboardingDraftRow(
            row.get(member.nickname),
            row.get(member.oneLineIntro),
            row.get(member.displayType),
            row.get(member.fiftyYearOldMe),
            row.get(member.joinReason),
            row.get(member.currentConcern),
            row.get(member.threeYearGoal),
            interestTagIds
        ));
    }

    public int countOtherActiveAdmins(Long memberId) {
        Long count = queryFactory
            .select(member.count())
            .from(member)
            .where(
                member.id.ne(memberId),
                member.role.eq("ADMIN"),
                member.onboardingCompletedAt.isNotNull(),
                member.deactivatedAt.isNull()
            )
            .fetchOne();
        return count == null ? 0 : count.intValue();
    }

    @Transactional
    public void updateRole(Long memberId, String role) {
        queryFactory
            .update(member)
            .set(member.role, role)
            .set(member.updatedAt, OffsetDateTime.now())
            .where(member.id.eq(memberId))
            .execute();
    }

    @Transactional
    public void reactivateWithdrawnMember(
        Long memberId,
        String role,
        String nickname,
        String oneLineIntro,
        String displayType,
        String realName,
        LocalDate birthDate,
        Long profileImageId,
        String kakaoProfileImageUrl,
        String fiftyYearOldMe,
        String joinReason,
        String currentConcern,
        String threeYearGoal
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        queryFactory
            .update(member)
            .set(member.role, role)
            .set(member.nickname, nickname)
            .set(member.oneLineIntro, oneLineIntro)
            .set(member.displayType, displayType)
            .set(member.realName, realName)
            .set(member.birthDate, birthDate)
            .set(member.profileImageId, profileImageId)
            .set(member.kakaoProfileImageUrl, kakaoProfileImageUrl)
            .set(member.fiftyYearOldMe, fiftyYearOldMe)
            .set(member.joinReason, joinReason)
            .set(member.currentConcern, currentConcern)
            .set(member.threeYearGoal, threeYearGoal)
            .set(member.participationStartMonth, KstDateTimes.currentMonth().plusMonths(1))
            .set(member.inviteVerifiedAt, now)
            .set(member.termsAgreedAt, now)
            .set(member.privacyAgreedAt, now)
            .set(member.onboardingCompletedAt, now)
            .setNull(member.deactivatedAt)
            .setNull(member.withdrawnAt)
            .setNull(member.personalDataRedactedAt)
            .set(member.updatedAt, now)
            .where(member.id.eq(memberId), member.withdrawnAt.isNotNull())
            .execute();
    }

    public List<AdminMemberRow> findAdminMembers(String keyword, int limit, int offset) {
        BooleanBuilder where = new BooleanBuilder();
        if (keyword != null && !keyword.isBlank()) {
            String normalized = keyword.trim();
            where.and(
                member.nickname.containsIgnoreCase(normalized)
                    .or(member.realName.containsIgnoreCase(normalized))
                    .or(member.job.containsIgnoreCase(normalized))
            );
        }
        List<Tuple> rows = queryFactory
            .select(adminMemberFields())
            .from(member)
            .leftJoin(profileImage).on(profileImage.id.eq(member.profileImageId))
            .where(where)
            .orderBy(member.createdAt.desc())
            .limit(limit)
            .offset(offset)
            .fetch();
        return mapAdminMembers(rows);
    }

    public Optional<AdminMemberRow> findAdminMemberById(Long memberId) {
        Tuple row = queryFactory
            .select(adminMemberFields())
            .from(member)
            .leftJoin(profileImage).on(profileImage.id.eq(member.profileImageId))
            .where(member.id.eq(memberId))
            .fetchOne();
        return row == null ? Optional.empty() : Optional.of(mapAdminMembers(List.of(row)).get(0));
    }

    @Transactional
    public void updateParticipationStartMonth(Long memberId, LocalDate participationStartMonth) {
        queryFactory
            .update(member)
            .set(member.participationStartMonth, participationStartMonth.withDayOfMonth(1))
            .set(member.updatedAt, OffsetDateTime.now())
            .where(member.id.eq(memberId))
            .execute();
    }

    public int countActiveMembers() {
        Long count = queryFactory
            .select(member.count())
            .from(member)
            .where(member.onboardingCompletedAt.isNotNull(), member.deactivatedAt.isNull())
            .fetchOne();
        return count == null ? 0 : count.intValue();
    }

    private com.querydsl.core.types.Expression<?>[] adminMemberFields() {
        return new com.querydsl.core.types.Expression<?>[] {
            member.id,
            member.role,
            member.displayType,
            member.realName,
            member.nickname,
            member.oneLineIntro,
            member.job,
            profileImage.publicUrl,
            member.kakaoProfileImageUrl,
            member.participationStartMonth,
            member.inviteVerifiedAt,
            member.termsAgreedAt,
            member.privacyAgreedAt,
            member.onboardingCompletedAt,
            member.deactivatedAt,
            member.withdrawnAt,
            member.createdAt
        };
    }

    private List<AdminMemberRow> mapAdminMembers(List<Tuple> rows) {
        Map<Long, List<String>> tagsByMemberId = findTagsByMemberId(rows.stream()
            .map(row -> row.get(member.id))
            .toList());
        List<AdminMemberRow> result = new ArrayList<>();
        for (Tuple row : rows) {
            Long memberId = row.get(member.id);
            String displayType = row.get(member.displayType);
            String realName = row.get(member.realName);
            String nickname = row.get(member.nickname);
            String profileImageUrl = row.get(profileImage.publicUrl) == null
                ? row.get(member.kakaoProfileImageUrl)
                : row.get(profileImage.publicUrl);
            result.add(new AdminMemberRow(
                memberId,
                row.get(member.role),
                "REAL_NAME".equals(displayType) && realName != null && !realName.isBlank() ? realName : nickname,
                realName,
                nickname,
                row.get(member.oneLineIntro),
                row.get(member.job),
                profileImageUrl,
                row.get(member.participationStartMonth),
                tagsByMemberId.getOrDefault(memberId, List.of()),
                row.get(member.inviteVerifiedAt),
                row.get(member.termsAgreedAt),
                row.get(member.privacyAgreedAt),
                row.get(member.onboardingCompletedAt),
                row.get(member.deactivatedAt),
                row.get(member.withdrawnAt),
                row.get(member.createdAt)
            ));
        }
        return result;
    }

    private Map<Long, List<String>> findTagsByMemberId(List<Long> memberIds) {
        Map<Long, List<String>> tagsByMemberId = new LinkedHashMap<>();
        if (memberIds.isEmpty()) {
            return tagsByMemberId;
        }
        queryFactory
            .select(memberInterestTag.id.memberId, interestTag.name)
            .from(memberInterestTag)
            .join(interestTag).on(interestTag.id.eq(memberInterestTag.id.interestTagId))
            .where(memberInterestTag.id.memberId.in(memberIds))
            .orderBy(interestTag.displayOrder.asc())
            .fetch()
            .forEach(row -> tagsByMemberId
                .computeIfAbsent(row.get(memberInterestTag.id.memberId), ignored -> new ArrayList<>())
                .add(row.get(interestTag.name)));
        return tagsByMemberId;
    }

    private MemberPrincipal mapPrincipal(Tuple row) {
        return new MemberPrincipal(
            row.get(member.id),
            row.get(member.role),
            row.get(member.displayType),
            row.get(member.realName),
            row.get(member.nickname),
            row.get(member.kakaoProfileImageUrl),
            row.get(member.inviteVerifiedAt),
            row.get(member.termsAgreedAt),
            row.get(member.privacyAgreedAt),
            row.get(member.onboardingCompletedAt),
            row.get(member.deactivatedAt)
        );
    }

    public record AdminMemberRow(
        Long memberId,
        String role,
        String displayName,
        String realName,
        String nickname,
        String oneLineIntro,
        String job,
        String profileImageUrl,
        LocalDate participationStartMonth,
        List<String> interestTags,
        OffsetDateTime inviteVerifiedAt,
        OffsetDateTime termsAgreedAt,
        OffsetDateTime privacyAgreedAt,
        OffsetDateTime onboardingCompletedAt,
        OffsetDateTime deactivatedAt,
        OffsetDateTime withdrawnAt,
        OffsetDateTime createdAt
    ) {
    }

    public record OnboardingDraftRow(
        String nickname,
        String oneLineIntro,
        String displayNameType,
        String futureMeAt50,
        String joinReason,
        String currentConcern,
        String threeYearGoal,
        List<Long> interestTagIds
    ) {
    }
}
