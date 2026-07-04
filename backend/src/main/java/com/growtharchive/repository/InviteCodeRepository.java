package com.growtharchive.repository;

import com.growtharchive.domain.invite.InviteCode;
import com.growtharchive.domain.invite.QInviteCode;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class InviteCodeRepository {

    private static final QInviteCode inviteCode = QInviteCode.inviteCode;

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public InviteCodeRepository(EntityManager entityManager, JPAQueryFactory queryFactory) {
        this.entityManager = entityManager;
        this.queryFactory = queryFactory;
    }

    public Optional<String> findActiveHash() {
        return findActiveHash("MEMBER");
    }

    public Optional<String> findActiveHash(String role) {
        return Optional.ofNullable(queryFactory
            .select(inviteCode.codeHash)
            .from(inviteCode)
            .where(inviteCode.role.eq(role), inviteCode.active.isTrue())
            .limit(1)
            .fetchOne());
    }

    public Optional<String> findActiveRoleByHash(String codeHash) {
        return Optional.ofNullable(queryFactory
            .select(inviteCode.role)
            .from(inviteCode)
            .where(inviteCode.codeHash.eq(codeHash), inviteCode.active.isTrue())
            .limit(1)
            .fetchOne());
    }

    public Optional<String> findActivePreview() {
        return findActivePreview("MEMBER");
    }

    public Optional<String> findActivePreview(String role) {
        return Optional.ofNullable(queryFactory
            .select(inviteCode.codePreview)
            .from(inviteCode)
            .where(inviteCode.role.eq(role), inviteCode.active.isTrue())
            .limit(1)
            .fetchOne());
    }

    public ActiveInviteCodePreviews findActivePreviews() {
        return new ActiveInviteCodePreviews(
            findActivePreview("MEMBER").orElse(null),
            findActivePreview("ADMIN").orElse(null)
        );
    }

    @Transactional
    public void replaceActiveCode(String codeHash, String codePreview, Long createdByMemberId) {
        replaceActiveCode("MEMBER", codeHash, codePreview, createdByMemberId);
    }

    @Transactional
    public void replaceActiveCode(String role, String codeHash, String codePreview, Long createdByMemberId) {
        queryFactory
            .update(inviteCode)
            .set(inviteCode.active, false)
            .set(inviteCode.deactivatedAt, OffsetDateTime.now())
            .where(inviteCode.role.eq(role), inviteCode.active.isTrue())
            .execute();
        entityManager.persist(new InviteCode(codeHash, codePreview, role, createdByMemberId));
    }

    public record ActiveInviteCodePreviews(String memberCodePreview, String adminCodePreview) {
    }
}
