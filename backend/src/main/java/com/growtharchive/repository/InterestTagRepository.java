package com.growtharchive.repository;

import com.growtharchive.domain.member.InterestTag;
import com.growtharchive.domain.member.MemberInterestTag;
import com.growtharchive.domain.member.QInterestTag;
import com.growtharchive.domain.member.QMemberInterestTag;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InterestTagRepository {

    private static final QInterestTag interestTag = QInterestTag.interestTag;
    private static final QMemberInterestTag memberInterestTag = QMemberInterestTag.memberInterestTag;

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public InterestTagRepository(EntityManager entityManager, JPAQueryFactory queryFactory) {
        this.entityManager = entityManager;
        this.queryFactory = queryFactory;
    }

    public int countActiveIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        Long count = queryFactory
            .select(interestTag.count())
            .from(interestTag)
            .where(interestTag.active.isTrue(), interestTag.id.in(ids))
            .fetchOne();
        return count == null ? 0 : count.intValue();
    }

    public void replaceMemberTags(Long memberId, List<Long> ids) {
        queryFactory
            .delete(memberInterestTag)
            .where(memberInterestTag.id.memberId.eq(memberId))
            .execute();
        for (Long id : ids) {
            entityManager.persist(new MemberInterestTag(memberId, id));
        }
    }

    public List<InterestTagRow> findAllForAdmin() {
        return baseSelect()
            .from(interestTag)
            .orderBy(interestTag.displayOrder.asc(), interestTag.id.asc())
            .fetch();
    }

    public List<InterestTagRow> findActive() {
        return baseSelect()
            .from(interestTag)
            .where(interestTag.active.isTrue())
            .orderBy(interestTag.displayOrder.asc(), interestTag.id.asc())
            .fetch();
    }

    public Optional<InterestTagRow> findById(Long tagId) {
        return Optional.ofNullable(baseSelect()
            .from(interestTag)
            .where(interestTag.id.eq(tagId))
            .fetchOne());
    }

    public Long create(String name, String slug, int displayOrder) {
        InterestTag tag = new InterestTag(name, slug, displayOrder);
        entityManager.persist(tag);
        entityManager.flush();
        return tag.getId();
    }

    public void update(Long tagId, String name, String slug, int displayOrder, boolean active) {
        queryFactory
            .update(interestTag)
            .set(interestTag.name, name)
            .set(interestTag.slug, slug)
            .set(interestTag.displayOrder, displayOrder)
            .set(interestTag.active, active)
            .where(interestTag.id.eq(tagId))
            .execute();
    }

    public void deactivate(Long tagId) {
        queryFactory
            .update(interestTag)
            .set(interestTag.active, false)
            .where(interestTag.id.eq(tagId))
            .execute();
    }

    private com.querydsl.jpa.impl.JPAQuery<InterestTagRow> baseSelect() {
        return queryFactory.select(Projections.constructor(
            InterestTagRow.class,
            interestTag.id,
            interestTag.name,
            interestTag.slug,
            interestTag.displayOrder,
            interestTag.active
        ));
    }

    public record InterestTagRow(Long id, String name, String slug, int displayOrder, boolean active) {
    }
}
