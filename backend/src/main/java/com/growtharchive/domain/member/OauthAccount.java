package com.growtharchive.domain.member;

import com.growtharchive.domain.common.CreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "oauth_accounts")
public class OauthAccount extends CreatedAtEntity {

    @Column(nullable = false, length = 30)
    private String provider;

    @Column(name = "provider_user_id", nullable = false, length = 100)
    private String providerUserId;

    @Column(name = "member_id")
    private Long memberId;

    @Column(length = 255)
    private String email;

    @Column(name = "profile_nickname", length = 100)
    private String profileNickname;

    @Column(name = "profile_image_url", columnDefinition = "text")
    private String profileImageUrl;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    protected OauthAccount() {
    }
}
