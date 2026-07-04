package com.growtharchive.repository;

import com.growtharchive.domain.image.ImageAsset;
import com.growtharchive.domain.image.QImageAsset;
import com.growtharchive.domain.book.QBook;
import com.growtharchive.domain.meeting.QMeeting;
import com.growtharchive.domain.meeting.QMeetingReview;
import com.growtharchive.domain.meeting.QMeetingReviewImage;
import com.growtharchive.domain.member.QMember;
import com.growtharchive.domain.reading.QReadingRecord;
import com.growtharchive.service.storage.StoredImage;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ImageAssetRepository {

    private static final QImageAsset imageAsset = QImageAsset.imageAsset;
    private static final QMember member = QMember.member;
    private static final QReadingRecord readingRecord = QReadingRecord.readingRecord;
    private static final QMeeting meeting = QMeeting.meeting;
    private static final QMeetingReview meetingReview = QMeetingReview.meetingReview;
    private static final QMeetingReviewImage meetingReviewImage = QMeetingReviewImage.meetingReviewImage;
    private static final QBook book = QBook.book;

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public ImageAssetRepository(EntityManager entityManager, JPAQueryFactory queryFactory) {
        this.entityManager = entityManager;
        this.queryFactory = queryFactory;
    }

    public Long create(Long memberId, String imageType, StoredImage image) {
        ImageAsset asset = new ImageAsset(
            memberId,
            image.bucket(),
            image.objectKey(),
            image.publicUrl(),
            imageType,
            image.mimeType(),
            image.width(),
            image.height(),
            image.sizeBytes()
        );
        entityManager.persist(asset);
        entityManager.flush();
        return asset.getId();
    }

    public boolean isOwnedImage(Long memberId, Long imageId, String imageType) {
        if (imageId == null) {
            return true;
        }
        Long count = queryFactory
            .select(imageAsset.count())
            .from(imageAsset)
            .where(
                imageAsset.ownerMemberId.eq(memberId),
                imageAsset.id.eq(imageId),
                imageAsset.imageType.eq(imageType)
            )
            .fetchOne();
        return count != null && count > 0;
    }

    public boolean isUnownedImage(Long imageId, String imageType) {
        if (imageId == null) {
            return true;
        }
        Long count = queryFactory
            .select(imageAsset.count())
            .from(imageAsset)
            .where(
                imageAsset.ownerMemberId.isNull(),
                imageAsset.id.eq(imageId),
                imageAsset.imageType.eq(imageType)
            )
            .fetchOne();
        return count != null && count > 0;
    }

    public void assignOwner(Long imageId, Long memberId) {
        if (imageId == null) {
            return;
        }
        queryFactory
            .update(imageAsset)
            .set(imageAsset.ownerMemberId, memberId)
            .where(imageAsset.id.eq(imageId), imageAsset.ownerMemberId.isNull())
            .execute();
    }

    public int countOwnedImages(Long memberId, List<Long> imageIds, String imageType) {
        if (imageIds == null || imageIds.isEmpty()) {
            return 0;
        }
        Long count = queryFactory
            .select(imageAsset.count())
            .from(imageAsset)
            .where(
                imageAsset.ownerMemberId.eq(memberId),
                imageAsset.imageType.eq(imageType),
                imageAsset.id.in(imageIds)
            )
            .fetchOne();
        return count == null ? 0 : count.intValue();
    }

    public List<OrphanImageAsset> findUnlinkedImagesCreatedBefore(OffsetDateTime threshold, int limit) {
        return queryFactory
            .select(imageAsset.id, imageAsset.bucket, imageAsset.objectKey)
            .from(imageAsset)
            .where(
                imageAsset.createdAt.lt(threshold),
                JPAExpressions.selectOne().from(member).where(member.profileImageId.eq(imageAsset.id)).notExists(),
                JPAExpressions.selectOne().from(readingRecord).where(readingRecord.representativeImageId.eq(imageAsset.id)).notExists(),
                JPAExpressions.selectOne().from(meeting).where(meeting.coverImageId.eq(imageAsset.id)).notExists(),
                JPAExpressions.selectOne().from(meetingReview).where(meetingReview.representativeImageId.eq(imageAsset.id)).notExists(),
                JPAExpressions.selectOne().from(meetingReviewImage).where(meetingReviewImage.imageAssetId.eq(imageAsset.id)).notExists(),
                JPAExpressions.selectOne().from(book).where(book.thumbnailUrl.eq(imageAsset.publicUrl)).notExists()
            )
            .orderBy(imageAsset.id.asc())
            .limit(limit)
            .fetch()
            .stream()
            .map(row -> new OrphanImageAsset(row.get(imageAsset.id), row.get(imageAsset.bucket), row.get(imageAsset.objectKey)))
            .toList();
    }

    public void delete(Long imageId) {
        queryFactory
            .delete(imageAsset)
            .where(imageAsset.id.eq(imageId))
            .execute();
    }

    public record OrphanImageAsset(Long id, String bucket, String objectKey) {
    }
}
