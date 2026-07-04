package com.growtharchive.domain.meeting;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "meeting_attendances")
public class MeetingAttendance {

    @EmbeddedId
    private MeetingAttendanceId id;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected MeetingAttendance() {
    }

    public MeetingAttendance(Long meetingId, Long memberId) {
        OffsetDateTime now = OffsetDateTime.now();
        this.id = new MeetingAttendanceId(meetingId, memberId);
        this.status = "JOINED";
        this.createdAt = now;
        this.updatedAt = now;
    }
}
