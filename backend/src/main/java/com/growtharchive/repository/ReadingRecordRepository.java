package com.growtharchive.repository;

import com.growtharchive.domain.book.QBook;
import com.growtharchive.domain.image.QImageAsset;
import com.growtharchive.domain.member.QMember;
import com.growtharchive.domain.reading.QReadingRecord;
import com.growtharchive.domain.reading.ReadingRecord;
import com.growtharchive.service.reading.ReadingRecordDetail;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ReadingRecordRepository {

    private static final QReadingRecord readingRecord = QReadingRecord.readingRecord;
    private static final QBook book = QBook.book;
    private static final QMember member = QMember.member;
    private static final QImageAsset recordImage = new QImageAsset("recordImage");
    private static final QImageAsset profileImage = new QImageAsset("profileImage");

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public ReadingRecordRepository(EntityManager entityManager, JPAQueryFactory queryFactory) {
        this.entityManager = entityManager;
        this.queryFactory = queryFactory;
    }

    public Long create(Long memberId, Long bookId, Integer rating, String oneLineReview, String blogUrl, Long imageId) {
        ReadingRecord entity = new ReadingRecord(memberId, bookId, rating, oneLineReview, blogUrl, imageId);
        entityManager.persist(entity);
        entityManager.flush();
        return entity.getId();
    }

    public void updateContent(Long recordId, Long memberId, Long bookId, Integer rating, String oneLineReview, String blogUrl, Long imageId) {
        var update = queryFactory
            .update(readingRecord)
            .set(readingRecord.bookId, bookId)
            .set(readingRecord.oneLineReview, oneLineReview)
            .set(readingRecord.blogUrl, blogUrl)
            .set(readingRecord.representativeImageId, imageId)
            .set(readingRecord.updatedAt, OffsetDateTime.now());
        if (rating == null) {
            update.setNull(readingRecord.rating);
        } else {
            update.set(readingRecord.rating, rating.shortValue());
        }
        update
            .where(readingRecord.id.eq(recordId), readingRecord.memberId.eq(memberId), readingRecord.status.ne("DELETED"))
            .execute();
    }

    public void softDeleteByAuthor(Long recordId, Long memberId) {
        queryFactory
            .update(readingRecord)
            .set(readingRecord.status, "DELETED")
            .set(readingRecord.deletedAt, OffsetDateTime.now())
            .set(readingRecord.updatedAt, OffsetDateTime.now())
            .where(readingRecord.id.eq(recordId), readingRecord.memberId.eq(memberId), readingRecord.status.ne("DELETED"))
            .execute();
    }

    public void hideByAdmin(Long recordId) {
        queryFactory
            .update(readingRecord)
            .set(readingRecord.status, "HIDDEN")
            .set(readingRecord.hiddenAt, OffsetDateTime.now())
            .set(readingRecord.updatedAt, OffsetDateTime.now())
            .where(readingRecord.id.eq(recordId), readingRecord.status.ne("DELETED"))
            .execute();
    }

    public void restoreByAdmin(Long recordId) {
        queryFactory
            .update(readingRecord)
            .set(readingRecord.status, "ACTIVE")
            .setNull(readingRecord.hiddenAt)
            .set(readingRecord.updatedAt, OffsetDateTime.now())
            .where(readingRecord.id.eq(recordId), readingRecord.status.eq("HIDDEN"))
            .execute();
    }

    public void deleteByAdmin(Long recordId) {
        queryFactory
            .update(readingRecord)
            .set(readingRecord.status, "DELETED")
            .set(readingRecord.deletedAt, OffsetDateTime.now())
            .set(readingRecord.updatedAt, OffsetDateTime.now())
            .where(readingRecord.id.eq(recordId), readingRecord.status.ne("DELETED"))
            .execute();
    }

    public Optional<ReadingRecordDetail> findById(Long recordId, boolean publicOnly) {
        BooleanBuilder where = new BooleanBuilder(readingRecord.id.eq(recordId));
        if (publicOnly) {
            where.and(readingRecord.status.eq("ACTIVE"));
        }
        return Optional.ofNullable(baseSelect().where(where).fetchOne());
    }

    public List<ReadingRecordDetail> findPublic(Long bookId, Long memberId, OffsetDateTime monthStart, OffsetDateTime monthEnd, int limit, int offset) {
        BooleanBuilder where = new BooleanBuilder(readingRecord.status.eq("ACTIVE"));
        if (bookId != null) {
            where.and(readingRecord.bookId.eq(bookId));
        }
        if (memberId != null) {
            where.and(readingRecord.memberId.eq(memberId));
        }
        if (monthStart != null && monthEnd != null) {
            where.and(readingRecord.recordedAt.goe(monthStart));
            where.and(readingRecord.recordedAt.lt(monthEnd));
        }
        return baseSelect()
            .where(where)
            .orderBy(readingRecord.recordedAt.desc())
            .limit(limit)
            .offset(offset)
            .fetch();
    }

    public List<ReadingRecordDetail> findRecentPublic(int limit) {
        return baseSelect()
            .where(readingRecord.status.eq("ACTIVE"))
            .orderBy(readingRecord.createdAt.desc(), readingRecord.id.desc())
            .limit(limit)
            .fetch();
    }

    public boolean existsById(Long recordId) {
        Long count = queryFactory.select(readingRecord.count()).from(readingRecord).where(readingRecord.id.eq(recordId)).fetchOne();
        return count != null && count > 0;
    }

    public boolean existsBook(Long bookId) {
        Long count = queryFactory.select(book.count()).from(book).where(book.id.eq(bookId)).fetchOne();
        return count != null && count > 0;
    }

    private JPAQuery<ReadingRecordDetail> baseSelect() {
        StringExpression displayName = new CaseBuilder()
            .when(member.displayType.eq("REAL_NAME").and(member.realName.isNotNull()).and(member.realName.ne("")))
            .then(member.realName)
            .otherwise(member.nickname);
        return queryFactory
            .select(Projections.constructor(
                ReadingRecordDetail.class,
                readingRecord.id,
                readingRecord.memberId,
                displayName,
                profileImage.publicUrl.coalesce(member.kakaoProfileImageUrl),
                readingRecord.bookId,
                book.title,
                book.authorsText,
                book.thumbnailUrl,
                readingRecord.rating.intValue(),
                readingRecord.oneLineReview,
                readingRecord.blogUrl,
                readingRecord.representativeImageId,
                recordImage.publicUrl,
                readingRecord.status,
                readingRecord.recordedAt,
                readingRecord.createdAt
            ))
            .from(readingRecord)
            .join(book).on(book.id.eq(readingRecord.bookId))
            .join(member).on(member.id.eq(readingRecord.memberId))
            .leftJoin(recordImage).on(recordImage.id.eq(readingRecord.representativeImageId))
            .leftJoin(profileImage).on(profileImage.id.eq(member.profileImageId));
    }
}
