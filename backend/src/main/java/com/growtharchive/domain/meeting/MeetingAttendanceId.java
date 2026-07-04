package com.growtharchive.domain.meeting;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MeetingAttendanceId implements Serializable {

    @Column(name = "meeting_id")
    private Long meetingId;

    @Column(name = "member_id")
    private Long memberId;

    protected MeetingAttendanceId() {
    }

    public MeetingAttendanceId(Long meetingId, Long memberId) {
        this.meetingId = meetingId;
        this.memberId = memberId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MeetingAttendanceId that)) {
            return false;
        }
        return Objects.equals(meetingId, that.meetingId)
            && Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meetingId, memberId);
    }
}
