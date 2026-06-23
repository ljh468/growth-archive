package com.growtharchive.domain.meeting;

import com.growtharchive.domain.common.CreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "meeting_review_images")
public class MeetingReviewImage extends CreatedAtEntity {

    @Column(name = "meeting_review_id", nullable = false)
    private Long meetingReviewId;

    @Column(name = "image_asset_id", nullable = false)
    private Long imageAssetId;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected MeetingReviewImage() {
    }
}
