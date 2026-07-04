package com.growtharchive.domain.member;

import com.growtharchive.domain.common.BaseEntity;
import com.growtharchive.support.KstDateTimes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "members")
public class Member extends BaseEntity {

    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "display_type", nullable = false, length = 20)
    private String displayType;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(name = "one_line_intro", length = 80)
    private String oneLineIntro;

    @Column(length = 50)
    private String job;

    @Column(name = "profile_image_id")
    private Long profileImageId;

    @Column(name = "kakao_profile_image_url", columnDefinition = "text")
    private String kakaoProfileImageUrl;

    @Column(name = "fifty_year_old_me", columnDefinition = "text")
    private String fiftyYearOldMe;

    @Column(name = "join_reason", columnDefinition = "text")
    private String joinReason;

    @Column(name = "current_concern", columnDefinition = "text")
    private String currentConcern;

    @Column(name = "three_year_goal", columnDefinition = "text")
    private String threeYearGoal;

    @Column(name = "participation_start_month", nullable = false)
    private LocalDate participationStartMonth;

    @Column(name = "invite_verified_at")
    private OffsetDateTime inviteVerifiedAt;

    @Column(name = "terms_agreed_at")
    private OffsetDateTime termsAgreedAt;

    @Column(name = "privacy_agreed_at")
    private OffsetDateTime privacyAgreedAt;

    @Column(name = "onboarding_completed_at")
    private OffsetDateTime onboardingCompletedAt;

    @Column(name = "deactivated_at")
    private OffsetDateTime deactivatedAt;

    @Column(name = "withdrawn_at")
    private OffsetDateTime withdrawnAt;

    @Column(name = "personal_data_redacted_at")
    private OffsetDateTime personalDataRedactedAt;

    protected Member() {
    }

    public Member(
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
        this.role = role;
        this.displayType = displayType;
        this.realName = realName;
        this.birthDate = birthDate;
        this.nickname = nickname;
        this.oneLineIntro = oneLineIntro;
        this.profileImageId = profileImageId;
        this.kakaoProfileImageUrl = kakaoProfileImageUrl;
        this.fiftyYearOldMe = fiftyYearOldMe;
        this.joinReason = joinReason;
        this.currentConcern = currentConcern;
        this.threeYearGoal = threeYearGoal;
        this.participationStartMonth = KstDateTimes.currentMonth().plusMonths(1);
        this.inviteVerifiedAt = now;
        this.termsAgreedAt = now;
        this.privacyAgreedAt = now;
        this.onboardingCompletedAt = now;
    }
}
