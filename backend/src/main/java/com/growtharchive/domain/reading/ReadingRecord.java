package com.growtharchive.domain.reading;

import com.growtharchive.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reading_records")
public class ReadingRecord extends BaseEntity {

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column
    private Short rating;

    @Column(name = "one_line_review", nullable = false, length = 300)
    private String oneLineReview;

    @Column(name = "blog_url", nullable = false, columnDefinition = "text")
    private String blogUrl;

    @Column(name = "representative_image_id")
    private Long representativeImageId;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    @Column(name = "hidden_at")
    private OffsetDateTime hiddenAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected ReadingRecord() {
    }

    public ReadingRecord(Long memberId, Long bookId, Integer rating, String oneLineReview, String blogUrl, Long representativeImageId) {
        this.memberId = memberId;
        this.bookId = bookId;
        this.rating = rating == null ? null : rating.shortValue();
        this.oneLineReview = oneLineReview;
        this.blogUrl = blogUrl;
        this.representativeImageId = representativeImageId;
        this.status = "ACTIVE";
        this.recordedAt = OffsetDateTime.now();
    }
}
