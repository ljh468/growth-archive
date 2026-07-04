package com.growtharchive.repository;

import com.growtharchive.domain.admin.AdminAuditLog;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class AdminAuditLogRepository {

    private final EntityManager entityManager;

    public AdminAuditLogRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void record(Long adminMemberId, String action, String entityType, Long entityId, String beforeData, String afterData) {
        entityManager.persist(new AdminAuditLog(adminMemberId, action, entityType, entityId, beforeData, afterData));
    }
}
