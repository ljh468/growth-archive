package com.growtharchive.repository;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class InviteCodeRepository {

    private final JdbcTemplate jdbcTemplate;

    public InviteCodeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<String> findActiveHash() {
        return jdbcTemplate.query(
            "SELECT code_hash FROM invite_codes WHERE is_active = true LIMIT 1",
            rs -> rs.next() ? Optional.of(rs.getString("code_hash")) : Optional.empty()
        );
    }

    public Optional<String> findActivePreview() {
        return jdbcTemplate.query(
            "SELECT code_preview FROM invite_codes WHERE is_active = true LIMIT 1",
            rs -> rs.next() ? Optional.ofNullable(rs.getString("code_preview")) : Optional.empty()
        );
    }

    @Transactional
    public void replaceActiveCode(String codeHash, String codePreview, Long createdByMemberId) {
        jdbcTemplate.update(
            "UPDATE invite_codes SET is_active = false, deactivated_at = coalesce(deactivated_at, now()) WHERE is_active = true"
        );
        jdbcTemplate.update(
            """
                INSERT INTO invite_codes (code_hash, code_preview, is_active, created_by_member_id, created_at)
                VALUES (?, ?, true, ?, now())
                """,
            codeHash,
            codePreview,
            createdByMemberId
        );
    }
}
