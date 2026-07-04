package com.growtharchive.repository;

import com.growtharchive.domain.activity.ActivityEvent;
import com.growtharchive.domain.image.ImageAsset;
import com.growtharchive.domain.image.QImageAsset;
import com.growtharchive.domain.meeting.MeetingReview;
import com.growtharchive.domain.meeting.MeetingReviewImage;
import com.growtharchive.domain.meeting.QMeeting;
import com.growtharchive.domain.meeting.QMeetingReview;
import com.growtharchive.domain.meeting.QMeetingReviewImage;
import com.growtharchive.domain.member.QMember;
import com.growtharchive.service.review.MeetingReviewDetail;
import com.growtharchive.service.review.MeetingReviewSummary;
import com.growtharchive.service.review.ReviewImageView;
import com.growtharchive.service.storage.StoredImage;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MeetingReviewRepository {

    private static final QMeetingReview review = QMeetingReview.meetingReview;
    private static final QMeeting meeting = QMeeting.meeting;
    private static final QMember member = QMember.member;
    private static final QImageAsset profileImage = new QImageAsset("profileImage");
    private static final QImageAsset representativeImage = new QImageAsset("representativeImage");
    private static final QImageAsset reviewImageAsset = new QImageAsset("reviewImageAsset");
    private static final QMeetingReviewImage reviewImage = QMeetingReviewImage.meetingReviewImage;

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    public MeetingReviewRepository(JPAQueryFactory queryFactory, EntityManager entityManager) {
        this.queryFactory = queryFactory;
        this.entityManager = entityManager;
    }

    public List<MeetingReviewSummary> findPublic(Long meetingId, int limit, int offset) {
        BooleanBuilder where = new BooleanBuilder(review.status.eq("ACTIVE"));
        if (meetingId != null) {
            where.and(review.meetingId.eq(meetingId));
        }
        return queryFactory
            .select(summaryFields())
            .from(review)
            .join(meeting).on(meeting.id.eq(review.meetingId))
            .join(member).on(member.id.eq(review.memberId))
            .leftJoin(profileImage).on(profileImage.id.eq(member.profileImageId))
            .leftJoin(representativeImage).on(representativeImage.id.eq(review.representativeImageId))
            .where(where)
            .orderBy(review.createdAt.desc())
            .limit(limit)
            .offset(offset)
            .fetch()
            .stream()
            .map(this::mapSummary)
            .toList();
    }

    public List<MeetingReviewSummary> findAdmin(int limit, int offset) {
        return queryFactory
            .select(summaryFields())
            .from(review)
            .join(meeting).on(meeting.id.eq(review.meetingId))
            .join(member).on(member.id.eq(review.memberId))
            .leftJoin(profileImage).on(profileImage.id.eq(member.profileImageId))
            .leftJoin(representativeImage).on(representativeImage.id.eq(review.representativeImageId))
            .where(review.status.ne("DELETED"))
            .orderBy(review.createdAt.desc())
            .limit(limit)
            .offset(offset)
            .fetch()
            .stream()
            .map(this::mapSummary)
            .toList();
    }

    public Optional<MeetingReviewDetail> findById(Long reviewId, boolean publicOnly, Long viewerMemberId) {
        BooleanBuilder where = new BooleanBuilder(review.id.eq(reviewId));
        where.and(publicOnly ? review.status.eq("ACTIVE") : review.status.ne("DELETED"));
        Tuple row = queryFactory
            .select(detailFields())
            .from(review)
            .join(meeting).on(meeting.id.eq(review.meetingId))
            .join(member).on(member.id.eq(review.memberId))
            .leftJoin(profileImage).on(profileImage.id.eq(member.profileImageId))
            .where(where)
            .fetchOne();
        return row == null ? Optional.empty() : Optional.of(mapDetail(row, viewerMemberId, findImages(reviewId)));
    }

    @Transactional
    public Long create(Long memberId, Long meetingId, String title, String content, Long representativeImageId) {
        MeetingReview created = new MeetingReview(meetingId, memberId, title, content, representativeImageId);
        entityManager.persist(created);
        entityManager.flush();
        return created.getId();
    }

    @Transactional
    public void update(Long reviewId, String title, String content, Long representativeImageId) {
        queryFactory
            .update(review)
            .set(review.title, title)
            .set(review.content, content)
            .set(review.representativeImageId, representativeImageId)
            .set(review.updatedAt, OffsetDateTime.now())
            .where(review.id.eq(reviewId), review.status.ne("DELETED"))
            .execute();
    }

    @Transactional
    public void replaceImages(Long reviewId, List<Long> imageIds) {
        queryFactory
            .delete(reviewImage)
            .where(reviewImage.meetingReviewId.eq(reviewId))
            .execute();
        for (int i = 0; i < imageIds.size(); i++) {
            entityManager.persist(new MeetingReviewImage(reviewId, imageIds.get(i), i + 1));
        }
    }

    @Transactional
    public void deleteByAuthor(Long reviewId, Long memberId) {
        OffsetDateTime now = OffsetDateTime.now();
        queryFactory
            .update(review)
            .set(review.status, "DELETED")
            .set(review.deletedAt, now)
            .set(review.updatedAt, now)
            .where(review.id.eq(reviewId), review.memberId.eq(memberId), review.status.ne("DELETED"))
            .execute();
    }

    @Transactional
    public void hide(Long reviewId) {
        OffsetDateTime now = OffsetDateTime.now();
        queryFactory
            .update(review)
            .set(review.status, "HIDDEN")
            .set(review.hiddenAt, now)
            .set(review.updatedAt, now)
            .where(review.id.eq(reviewId), review.status.ne("DELETED"))
            .execute();
    }

    @Transactional
    public void restore(Long reviewId) {
        queryFactory
            .update(review)
            .set(review.status, "ACTIVE")
            .setNull(review.hiddenAt)
            .set(review.updatedAt, OffsetDateTime.now())
            .where(review.id.eq(reviewId), review.status.eq("HIDDEN"))
            .execute();
    }

    @Transactional
    public void deleteByAdmin(Long reviewId) {
        OffsetDateTime now = OffsetDateTime.now();
        queryFactory
            .update(review)
            .set(review.status, "DELETED")
            .set(review.deletedAt, now)
            .set(review.updatedAt, now)
            .where(review.id.eq(reviewId), review.status.ne("DELETED"))
            .execute();
    }

    public boolean meetingCanReceiveReview(Long meetingId) {
        Long count = queryFactory
            .select(meeting.count())
            .from(meeting)
            .where(meeting.id.eq(meetingId), meeting.status.in("SCHEDULED", "HELD"))
            .fetchOne();
        return count != null && count > 0;
    }

    public int countOwnedReviewImages(Long memberId, List<Long> imageIds) {
        if (imageIds.isEmpty()) {
            return 0;
        }
        Long count = queryFactory
            .select(reviewImageAsset.count())
            .from(reviewImageAsset)
            .where(
                reviewImageAsset.ownerMemberId.eq(memberId),
                reviewImageAsset.imageType.eq("MEETING_REVIEW"),
                reviewImageAsset.id.in(imageIds)
            )
            .fetchOne();
        return count == null ? 0 : count.intValue();
    }

    @Transactional
    public Long createImageAsset(Long memberId, StoredImage image) {
        ImageAsset created = new ImageAsset(
            memberId,
            image.bucket(),
            image.objectKey(),
            image.publicUrl(),
            "MEETING_REVIEW",
            image.mimeType(),
            null,
            null,
            image.sizeBytes()
        );
        entityManager.persist(created);
        entityManager.flush();
        return created.getId();
    }

    @Transactional
    public void insertActivityEvent(Long memberId, Long reviewId, String title) {
        entityManager.persist(new ActivityEvent(
            memberId,
            "MEETING_REVIEW_CREATED",
            "MEETING_REVIEW",
            reviewId,
            "PUBLIC",
            title
        ));
    }

    public List<ReviewImageView> findImages(Long reviewId) {
        return queryFactory
            .select(reviewImage.imageAssetId, reviewImageAsset.publicUrl, reviewImage.displayOrder)
            .from(reviewImage)
            .join(reviewImageAsset).on(reviewImageAsset.id.eq(reviewImage.imageAssetId))
            .where(reviewImage.meetingReviewId.eq(reviewId))
            .orderBy(reviewImage.displayOrder.asc())
            .fetch()
            .stream()
            .map(row -> new ReviewImageView(
                row.get(reviewImage.imageAssetId),
                row.get(reviewImageAsset.publicUrl),
                row.get(reviewImage.displayOrder)
            ))
            .toList();
    }

    private com.querydsl.core.types.Expression<?>[] summaryFields() {
        return new com.querydsl.core.types.Expression<?>[] {
            review.id,
            review.meetingId,
            meeting.title,
            review.memberId,
            member.displayType,
            member.realName,
            member.nickname,
            profileImage.publicUrl,
            member.kakaoProfileImageUrl,
            review.title,
            review.content,
            representativeImage.publicUrl,
            review.status,
            review.createdAt
        };
    }

    private com.querydsl.core.types.Expression<?>[] detailFields() {
        return new com.querydsl.core.types.Expression<?>[] {
            review.id,
            review.meetingId,
            meeting.title,
            review.memberId,
            member.displayType,
            member.realName,
            member.nickname,
            profileImage.publicUrl,
            member.kakaoProfileImageUrl,
            review.title,
            review.content,
            review.status,
            review.createdAt,
            review.updatedAt
        };
    }

    private MeetingReviewSummary mapSummary(Tuple row) {
        return new MeetingReviewSummary(
            row.get(review.id),
            row.get(review.meetingId),
            row.get(meeting.title),
            row.get(review.memberId),
            displayName(row),
            coalesce(row.get(profileImage.publicUrl), row.get(member.kakaoProfileImageUrl)),
            row.get(review.title),
            contentSummary(row.get(review.content)),
            row.get(representativeImage.publicUrl),
            row.get(review.status),
            row.get(review.createdAt)
        );
    }

    private MeetingReviewDetail mapDetail(Tuple row, Long viewerMemberId, List<ReviewImageView> images) {
        Long memberId = row.get(review.memberId);
        return new MeetingReviewDetail(
            row.get(review.id),
            row.get(review.meetingId),
            row.get(meeting.title),
            memberId,
            displayName(row),
            coalesce(row.get(profileImage.publicUrl), row.get(member.kakaoProfileImageUrl)),
            row.get(review.title),
            row.get(review.content),
            row.get(review.status),
            row.get(review.createdAt),
            row.get(review.updatedAt),
            viewerMemberId != null && memberId.equals(viewerMemberId),
            images
        );
    }

    private String displayName(Tuple row) {
        String realName = row.get(member.realName);
        String nickname = row.get(member.nickname);
        return "REAL_NAME".equals(row.get(member.displayType)) && realName != null && !realName.isBlank()
            ? realName
            : nickname;
    }

    private String contentSummary(String content) {
        if (content == null || content.length() <= 120) {
            return content;
        }
        return content.substring(0, 120);
    }

    private String coalesce(String first, String second) {
        return first == null || first.isBlank() ? second : first;
    }
}
