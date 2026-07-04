package com.growtharchive.domain.admin;

import com.growtharchive.domain.common.CreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "admin_audit_logs")
public class AdminAuditLog extends CreatedAtEntity {

    @Column(name = "admin_member_id", nullable = false)
    private Long adminMemberId;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 80)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "before_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String beforeData;

    @Column(name = "after_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String afterData;

    protected AdminAuditLog() {
    }

    public AdminAuditLog(Long adminMemberId, String action, String entityType, Long entityId, String beforeData, String afterData) {
        this.adminMemberId = adminMemberId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.beforeData = beforeData;
        this.afterData = afterData;
    }
}
