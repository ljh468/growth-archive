package com.growtharchive.repository;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OauthAccountRepository {

    private final JdbcTemplate jdbcTemplate;

    public OauthAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Long> findMemberId(String provider, String providerUserId) {
        return jdbcTemplate.query(
            "SELECT member_id FROM oauth_accounts WHERE provider = ? AND provider_user_id = ?",
            rs -> rs.next() ? Optional.ofNullable((Long) rs.getObject("member_id")) : Optional.empty(),
            provider,
            providerUserId
        );
    }

    public void upsert(String provider, String providerUserId, Long memberId, String email, String nickname, String profileImageUrl) {
        jdbcTemplate.update(
            """
                INSERT INTO oauth_accounts (
                    provider, provider_user_id, member_id, email, profile_nickname,
                    profile_image_url, last_login_at, created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, now(), now())
                ON CONFLICT (provider, provider_user_id)
                DO UPDATE SET
                    member_id = excluded.member_id,
                    email = excluded.email,
                    profile_nickname = excluded.profile_nickname,
                    profile_image_url = excluded.profile_image_url,
                    last_login_at = now()
                """,
            provider,
            providerUserId,
            memberId,
            email,
            nickname,
            profileImageUrl
        );
    }
}
