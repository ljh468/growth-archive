package com.growtharchive.repository;

import com.growtharchive.security.MemberPrincipal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

    private final JdbcTemplate jdbcTemplate;

    public MemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createPreOnboardingMember(String providerUserId, String kakaoNickname, String kakaoProfileImageUrl, boolean admin) {
        String seedNickname = uniqueSeedNickname(providerUserId);
        LocalDate startMonth = LocalDate.now().withDayOfMonth(1);
        return jdbcTemplate.queryForObject(
            """
                INSERT INTO members (
                    role, display_type, nickname, one_line_intro, kakao_profile_image_url,
                    fifty_year_old_me, participation_start_month, created_at, updated_at
                )
                VALUES (?, 'NICKNAME', ?, ?, ?, ?, ?, now(), now())
                RETURNING id
                """,
            Long.class,
            admin ? "ADMIN" : "MEMBER",
            seedNickname,
            "온보딩을 진행 중입니다.",
            kakaoProfileImageUrl,
            "온보딩을 진행 중입니다.",
            Date.valueOf(startMonth)
        );
    }

    public Optional<MemberPrincipal> findPrincipalById(Long memberId) {
        return queryPrincipal("WHERE id = ?", memberId);
    }

    public boolean existsNickname(String nickname) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM members WHERE lower(nickname) = lower(?)",
            Integer.class,
            nickname
        );
        return count != null && count > 0;
    }

    public boolean existsNicknameForOtherMember(String nickname, Long memberId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM members WHERE lower(nickname) = lower(?) AND id <> ?",
            Integer.class,
            nickname,
            memberId
        );
        return count != null && count > 0;
    }

    public void markInviteVerified(Long memberId) {
        jdbcTemplate.update(
            "UPDATE members SET invite_verified_at = coalesce(invite_verified_at, now()), updated_at = now() WHERE id = ?",
            memberId
        );
    }

    public void agreeTerms(Long memberId) {
        jdbcTemplate.update(
            """
                UPDATE members
                SET terms_agreed_at = coalesce(terms_agreed_at, now()),
                    privacy_agreed_at = coalesce(privacy_agreed_at, now()),
                    updated_at = now()
                WHERE id = ?
                """,
            memberId
        );
    }

    public void completeOnboarding(
        Long memberId,
        String nickname,
        String oneLineIntro,
        String displayType,
        String realName,
        String job,
        String fiftyYearOldMe,
        String joinReason,
        String currentConcern,
        String threeYearGoal
    ) {
        jdbcTemplate.update(
            """
                UPDATE members
                SET nickname = ?,
                    one_line_intro = ?,
                    display_type = ?,
                    real_name = ?,
                    job = ?,
                    fifty_year_old_me = ?,
                    join_reason = ?,
                    current_concern = ?,
                    three_year_goal = ?,
                    onboarding_completed_at = coalesce(onboarding_completed_at, now()),
                    updated_at = now()
                WHERE id = ?
                """,
            nickname,
            oneLineIntro,
            displayType,
            realName,
            job,
            fiftyYearOldMe,
            joinReason,
            currentConcern,
            threeYearGoal,
            memberId
        );
    }

    public void deactivate(Long memberId) {
        jdbcTemplate.update(
            "UPDATE members SET deactivated_at = coalesce(deactivated_at, now()), updated_at = now() WHERE id = ?",
            memberId
        );
    }

    public void reactivate(Long memberId) {
        jdbcTemplate.update(
            "UPDATE members SET deactivated_at = null, updated_at = now() WHERE id = ?",
            memberId
        );
    }

    private Optional<MemberPrincipal> queryPrincipal(String whereClause, Object... args) {
        return jdbcTemplate.query(
            """
                SELECT id, role, display_type, real_name, nickname, kakao_profile_image_url,
                       invite_verified_at, terms_agreed_at, privacy_agreed_at,
                       onboarding_completed_at, deactivated_at
                FROM members
                """ + whereClause,
            rs -> rs.next() ? Optional.of(mapPrincipal(rs)) : Optional.empty(),
            args
        );
    }

    private MemberPrincipal mapPrincipal(ResultSet rs) throws SQLException {
        return new MemberPrincipal(
            rs.getLong("id"),
            rs.getString("role"),
            rs.getString("display_type"),
            rs.getString("real_name"),
            rs.getString("nickname"),
            rs.getString("kakao_profile_image_url"),
            rs.getObject("invite_verified_at", OffsetDateTime.class),
            rs.getObject("terms_agreed_at", OffsetDateTime.class),
            rs.getObject("privacy_agreed_at", OffsetDateTime.class),
            rs.getObject("onboarding_completed_at", OffsetDateTime.class),
            rs.getObject("deactivated_at", OffsetDateTime.class)
        );
    }

    private String uniqueSeedNickname(String providerUserId) {
        String suffix = providerUserId.replaceAll("[^A-Za-z0-9]", "");
        if (suffix.length() > 10) {
            suffix = suffix.substring(suffix.length() - 10);
        }
        String nickname = "kakao_" + suffix;
        if (nickname.length() < 2 || existsNickname(nickname)) {
            nickname = "kakao_" + System.nanoTime();
        }
        if (nickname.length() > 20) {
            nickname = nickname.substring(0, 20);
        }
        return nickname;
    }
}
