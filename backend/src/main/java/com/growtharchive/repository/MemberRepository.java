package com.growtharchive.repository;

import com.growtharchive.security.MemberPrincipal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

    private final JdbcTemplate jdbcTemplate;

    public MemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

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
        LocalDate startMonth = LocalDate.now().withDayOfMonth(1).plusMonths(1);
        return jdbcTemplate.queryForObject(
            """
                INSERT INTO members (
                    role, display_type, nickname, one_line_intro, real_name, birth_date,
                    profile_image_id, kakao_profile_image_url, fifty_year_old_me, join_reason,
                    current_concern, three_year_goal, participation_start_month,
                    invite_verified_at, terms_agreed_at, privacy_agreed_at, onboarding_completed_at,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now(), now(), now(), now(), now())
                RETURNING id
                """,
            Long.class,
            role,
            displayType,
            nickname,
            oneLineIntro,
            realName,
            birthDate == null ? null : Date.valueOf(birthDate),
            profileImageId,
            kakaoProfileImageUrl,
            fiftyYearOldMe,
            joinReason,
            currentConcern,
            threeYearGoal,
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
        LocalDate birthDate,
        Long profileImageId,
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
                    birth_date = ?,
                    profile_image_id = ?,
                    fifty_year_old_me = ?,
                    join_reason = ?,
                    current_concern = ?,
                    three_year_goal = ?,
                    participation_start_month = (date_trunc('month', now()) + interval '1 month')::date,
                    onboarding_completed_at = coalesce(onboarding_completed_at, now()),
                    updated_at = now()
                WHERE id = ?
                """,
            nickname,
            oneLineIntro,
            displayType,
            realName,
            birthDate == null ? null : Date.valueOf(birthDate),
            profileImageId,
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

    public List<AdminMemberRow> findAdminMembers(String keyword, int limit, int offset) {
        List<Object> args = new ArrayList<>();
        String keywordFilter = "";
        if (keyword != null && !keyword.isBlank()) {
            keywordFilter = """
                 AND (
                    m.nickname ILIKE ?
                    OR coalesce(m.real_name, '') ILIKE ?
                    OR coalesce(m.job, '') ILIKE ?
                 )
                """;
            String normalized = "%" + keyword.trim() + "%";
            args.add(normalized);
            args.add(normalized);
            args.add(normalized);
        }
        args.add(limit);
        args.add(offset);
        return jdbcTemplate.query(
            adminMemberSelect() + " WHERE 1 = 1 " + keywordFilter + adminMemberGroupBy() + " ORDER BY m.created_at DESC LIMIT ? OFFSET ?",
            (rs, rowNum) -> mapAdminMember(rs),
            args.toArray()
        );
    }

    public Optional<AdminMemberRow> findAdminMemberById(Long memberId) {
        return jdbcTemplate.query(
            adminMemberSelect() + " WHERE m.id = ?" + adminMemberGroupBy(),
            rs -> rs.next() ? Optional.of(mapAdminMember(rs)) : Optional.empty(),
            memberId
        );
    }

    public void updateParticipationStartMonth(Long memberId, LocalDate participationStartMonth) {
        jdbcTemplate.update(
            "UPDATE members SET participation_start_month = ?, updated_at = now() WHERE id = ?",
            Date.valueOf(participationStartMonth.withDayOfMonth(1)),
            memberId
        );
    }

    public int countActiveMembers() {
        Integer count = jdbcTemplate.queryForObject(
            """
                SELECT count(*)
                FROM members
                WHERE onboarding_completed_at IS NOT NULL AND deactivated_at IS NULL
                """,
            Integer.class
        );
        return count == null ? 0 : count;
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

    private String adminMemberSelect() {
        return """
            SELECT m.id, m.role, m.display_type, m.real_name, m.nickname, m.one_line_intro, m.job,
                   coalesce(profile_image.public_url, m.kakao_profile_image_url) AS profile_image_url,
                   m.participation_start_month, m.invite_verified_at, m.terms_agreed_at, m.privacy_agreed_at,
                   m.onboarding_completed_at, m.deactivated_at, m.created_at,
                   coalesce(array_remove(array_agg(it.name ORDER BY it.display_order), null), '{}') AS interest_tags
            FROM members m
            LEFT JOIN image_assets profile_image ON profile_image.id = m.profile_image_id
            LEFT JOIN member_interest_tags mit ON mit.member_id = m.id
            LEFT JOIN interest_tags it ON it.id = mit.interest_tag_id
            """;
    }

    private String adminMemberGroupBy() {
        return """
             GROUP BY m.id, profile_image.public_url
            """;
    }

    private AdminMemberRow mapAdminMember(ResultSet rs) throws SQLException {
        String displayType = rs.getString("display_type");
        String realName = rs.getString("real_name");
        String nickname = rs.getString("nickname");
        String[] tags = (String[]) rs.getArray("interest_tags").getArray();
        return new AdminMemberRow(
            rs.getLong("id"),
            rs.getString("role"),
            "REAL_NAME".equals(displayType) && realName != null && !realName.isBlank() ? realName : nickname,
            realName,
            nickname,
            rs.getString("one_line_intro"),
            rs.getString("job"),
            rs.getString("profile_image_url"),
            rs.getDate("participation_start_month").toLocalDate(),
            List.of(tags),
            rs.getObject("invite_verified_at", OffsetDateTime.class),
            rs.getObject("terms_agreed_at", OffsetDateTime.class),
            rs.getObject("privacy_agreed_at", OffsetDateTime.class),
            rs.getObject("onboarding_completed_at", OffsetDateTime.class),
            rs.getObject("deactivated_at", OffsetDateTime.class),
            rs.getObject("created_at", OffsetDateTime.class)
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
        OffsetDateTime createdAt
    ) {
    }
}
