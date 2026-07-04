package com.growtharchive.repository;

import com.growtharchive.domain.book.QBook;
import com.growtharchive.domain.recommendation.QRecommendedBook;
import com.growtharchive.domain.recommendation.RecommendedBook;
import com.growtharchive.service.book.RecommendedBookView;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RecommendedBookRepository {

    private static final QRecommendedBook recommendedBook = QRecommendedBook.recommendedBook;
    private static final QBook book = QBook.book;

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public RecommendedBookRepository(EntityManager entityManager, JPAQueryFactory queryFactory) {
        this.entityManager = entityManager;
        this.queryFactory = queryFactory;
    }

    public List<RecommendedBookView> findActiveForMonth(LocalDate targetMonth) {
        return baseSelect()
            .where(recommendedBook.targetMonth.eq(targetMonth), recommendedBook.status.eq("ACTIVE"))
            .orderBy(recommendedBook.displayOrder.asc(), recommendedBook.id.asc())
            .fetch();
    }

    public List<RecommendedBookView> findActiveForDisplay() {
        return baseSelect()
            .where(recommendedBook.status.eq("ACTIVE"))
            .orderBy(recommendedBook.displayOrder.asc(), recommendedBook.id.asc())
            .fetch();
    }

    public List<RecommendedBookView> findForAdminDisplay() {
        return baseSelect()
            .where(recommendedBook.status.ne("DELETED"))
            .orderBy(recommendedBook.displayOrder.asc(), recommendedBook.id.asc())
            .fetch();
    }

    public Long create(Long adminMemberId, LocalDate targetMonth, Long bookId, String reason, int displayOrder) {
        RecommendedBook entity = new RecommendedBook(bookId, targetMonth, reason, adminMemberId, displayOrder);
        entityManager.persist(entity);
        entityManager.flush();
        return entity.getId();
    }

    public Optional<RecommendedBookView> findById(Long recommendedBookId) {
        return Optional.ofNullable(baseSelect()
            .where(recommendedBook.id.eq(recommendedBookId), recommendedBook.status.ne("DELETED"))
            .fetchOne());
    }

    public void update(Long recommendedBookId, Long adminMemberId, LocalDate targetMonth, Long bookId, String reason, int displayOrder) {
        queryFactory
            .update(recommendedBook)
            .set(recommendedBook.targetMonth, targetMonth)
            .set(recommendedBook.bookId, bookId)
            .set(recommendedBook.reason, reason)
            .set(recommendedBook.displayOrder, displayOrder)
            .set(recommendedBook.recommendedByMemberId, adminMemberId)
            .set(recommendedBook.status, "ACTIVE")
            .set(recommendedBook.updatedAt, OffsetDateTime.now())
            .where(recommendedBook.id.eq(recommendedBookId), recommendedBook.status.ne("DELETED"))
            .execute();
    }

    public void delete(Long recommendedBookId) {
        queryFactory
            .update(recommendedBook)
            .set(recommendedBook.status, "DELETED")
            .set(recommendedBook.updatedAt, OffsetDateTime.now())
            .where(recommendedBook.id.eq(recommendedBookId))
            .execute();
    }

    public void hide(Long recommendedBookId) {
        queryFactory
            .update(recommendedBook)
            .set(recommendedBook.status, "HIDDEN")
            .set(recommendedBook.updatedAt, OffsetDateTime.now())
            .where(recommendedBook.id.eq(recommendedBookId), recommendedBook.status.ne("DELETED"))
            .execute();
    }

    public void restore(Long recommendedBookId) {
        queryFactory
            .update(recommendedBook)
            .set(recommendedBook.status, "ACTIVE")
            .set(recommendedBook.updatedAt, OffsetDateTime.now())
            .where(recommendedBook.id.eq(recommendedBookId), recommendedBook.status.eq("HIDDEN"))
            .execute();
    }

    private JPAQuery<RecommendedBookView> baseSelect() {
        return queryFactory
            .select(Projections.constructor(
                RecommendedBookView.class,
                recommendedBook.id,
                book.id,
                book.title,
                book.authorsText,
                book.publisher,
                book.thumbnailUrl,
                recommendedBook.reason,
                recommendedBook.displayOrder,
                recommendedBook.targetMonth,
                recommendedBook.status
            ))
            .from(recommendedBook)
            .join(book).on(book.id.eq(recommendedBook.bookId));
    }
}
