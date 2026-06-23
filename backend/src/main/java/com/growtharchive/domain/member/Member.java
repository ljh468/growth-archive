package com.growtharchive.domain.member;

import com.growtharchive.domain.common.BaseEntity;
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

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(name = "one_line_intro", nullable = false, length = 80)
    private String oneLineIntro;

    @Column(length = 50)
    private String job;

    @Column(name = "profile_image_id")
    private Long profileImageId;

    @Column(name = "kakao_profile_image_url", columnDefinition = "text")
    private String kakaoProfileImageUrl;

    @Column(name = "fifty_year_old_me", nullable = false, columnDefinition = "text")
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

    protected Member() {
    }
}
