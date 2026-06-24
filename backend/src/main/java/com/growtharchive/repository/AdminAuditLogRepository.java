package com.growtharchive.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminAuditLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminAuditLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void record(Long adminMemberId, String action, String entityType, Long entityId, String beforeData, String afterData) {
        jdbcTemplate.update(
            """
                INSERT INTO admin_audit_logs (
                    admin_member_id, action, entity_type, entity_id, before_data, after_data, created_at
                )
                VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, now())
                """,
            adminMemberId,
            action,
            entityType,
            entityId,
            beforeData,
            afterData
        );
    }
}
