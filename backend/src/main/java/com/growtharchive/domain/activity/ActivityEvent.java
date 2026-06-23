package com.growtharchive.domain.activity;

import com.growtharchive.domain.common.CreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "activity_events")
public class ActivityEvent extends CreatedAtEntity {

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "reference_type", nullable = false, length = 50)
    private String referenceType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(nullable = false, length = 30)
    private String visibility;

    @Column(length = 255)
    private String summary;

    @Column(name = "happened_at", nullable = false)
    private OffsetDateTime happenedAt;

    protected ActivityEvent() {
    }
}
