package com.growtharchive.domain.meeting;

import com.growtharchive.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "meeting_reviews")
public class MeetingReview extends BaseEntity {

    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "representative_image_id")
    private Long representativeImageId;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "hidden_at")
    private OffsetDateTime hiddenAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected MeetingReview() {
    }

    public MeetingReview(Long meetingId, Long memberId, String title, String content, Long representativeImageId) {
        this.meetingId = meetingId;
        this.memberId = memberId;
        this.title = title;
        this.content = content;
        this.representativeImageId = representativeImageId;
        this.status = "ACTIVE";
    }
}
