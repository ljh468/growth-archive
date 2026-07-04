package com.growtharchive.repository;

import com.growtharchive.domain.monthly.MonthlyActionPlan;
import com.growtharchive.domain.monthly.MonthlyReflection;
import com.growtharchive.domain.monthly.QMonthlyActionPlan;
import com.growtharchive.domain.monthly.QMonthlyReflection;
import com.growtharchive.service.monthly.MonthlyActionPlanView;
import com.growtharchive.service.monthly.MonthlyReflectionView;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MonthlyRepository {

    private static final QMonthlyActionPlan actionPlan = QMonthlyActionPlan.monthlyActionPlan;
    private static final QMonthlyReflection reflection = QMonthlyReflection.monthlyReflection;

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public MonthlyRepository(EntityManager entityManager, JPAQueryFactory queryFactory) {
        this.entityManager = entityManager;
        this.queryFactory = queryFactory;
    }

    public Optional<MonthlyActionPlanView> findActionPlan(Long memberId, LocalDate targetMonth) {
        return Optional.ofNullable(actionPlanSelect()
            .where(actionPlan.memberId.eq(memberId), actionPlan.targetMonth.eq(targetMonth), actionPlan.status.ne("DELETED"))
            .fetchOne());
    }

    public Optional<MonthlyActionPlanView> findActionPlanById(Long actionPlanId) {
        return Optional.ofNullable(actionPlanSelect()
            .where(actionPlan.id.eq(actionPlanId), actionPlan.status.ne("DELETED"))
            .fetchOne());
    }

    public Long upsertActionPlan(Long memberId, LocalDate targetMonth, String title, String content) {
        Optional<MonthlyActionPlanView> existing = findActionPlan(memberId, targetMonth);
        if (existing.isPresent()) {
            updateActionPlan(existing.get().id(), title, content);
            queryFactory
                .update(actionPlan)
                .set(actionPlan.status, "ACTIVE")
                .setNull(actionPlan.deletedAt)
                .set(actionPlan.updatedAt, OffsetDateTime.now())
                .where(actionPlan.id.eq(existing.get().id()))
                .execute();
            return existing.get().id();
        }
        MonthlyActionPlan entity = new MonthlyActionPlan(memberId, targetMonth, title, content);
        entityManager.persist(entity);
        entityManager.flush();
        return entity.getId();
    }

    public void updateActionPlan(Long actionPlanId, String title, String content) {
        queryFactory
            .update(actionPlan)
            .set(actionPlan.title, title)
            .set(actionPlan.content, content)
            .set(actionPlan.updatedAt, OffsetDateTime.now())
            .where(actionPlan.id.eq(actionPlanId), actionPlan.status.ne("DELETED"))
            .execute();
    }

    public void deleteActionPlan(Long actionPlanId) {
        queryFactory
            .update(actionPlan)
            .set(actionPlan.status, "DELETED")
            .set(actionPlan.deletedAt, OffsetDateTime.now())
            .set(actionPlan.updatedAt, OffsetDateTime.now())
            .where(actionPlan.id.eq(actionPlanId), actionPlan.status.ne("DELETED"))
            .execute();
    }

    public Optional<MonthlyReflectionView> findReflection(Long memberId, LocalDate targetMonth) {
        return Optional.ofNullable(reflectionSelect()
            .where(reflection.memberId.eq(memberId), reflection.targetMonth.eq(targetMonth), reflection.status.ne("DELETED"))
            .fetchOne());
    }

    public Long upsertReflection(Long memberId, LocalDate targetMonth, String wellDone, String regret, String nextFocus) {
        Optional<MonthlyReflectionView> existing = findReflection(memberId, targetMonth);
        if (existing.isPresent()) {
            queryFactory
                .update(reflection)
                .set(reflection.wellDone, wellDone)
                .set(reflection.regret, regret)
                .set(reflection.nextFocus, nextFocus)
                .set(reflection.status, "ACTIVE")
                .setNull(reflection.deletedAt)
                .set(reflection.updatedAt, OffsetDateTime.now())
                .where(reflection.id.eq(existing.get().id()))
                .execute();
            return existing.get().id();
        }
        MonthlyReflection entity = new MonthlyReflection(memberId, targetMonth, wellDone, regret, nextFocus);
        entityManager.persist(entity);
        entityManager.flush();
        return entity.getId();
    }

    public Optional<MonthlyReflectionView> findReflectionById(Long reflectionId) {
        return Optional.ofNullable(reflectionSelect()
            .where(reflection.id.eq(reflectionId), reflection.status.ne("DELETED"))
            .fetchOne());
    }

    private JPAQuery<MonthlyActionPlanView> actionPlanSelect() {
        return queryFactory.select(Projections.constructor(
            MonthlyActionPlanView.class,
            actionPlan.id,
            actionPlan.memberId,
            actionPlan.targetMonth,
            actionPlan.title,
            actionPlan.content,
            actionPlan.status,
            actionPlan.createdAt,
            actionPlan.updatedAt
        )).from(actionPlan);
    }

    private JPAQuery<MonthlyReflectionView> reflectionSelect() {
        return queryFactory.select(Projections.constructor(
            MonthlyReflectionView.class,
            reflection.id,
            reflection.memberId,
            reflection.targetMonth,
            reflection.wellDone,
            reflection.regret,
            reflection.nextFocus,
            reflection.status,
            reflection.createdAt,
            reflection.updatedAt
        )).from(reflection);
    }
}
