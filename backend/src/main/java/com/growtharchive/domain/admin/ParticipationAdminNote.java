package com.growtharchive.domain.admin;

import com.growtharchive.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "participation_admin_notes")
public class ParticipationAdminNote extends BaseEntity {

    @Column(name = "target_month", nullable = false)
    private LocalDate targetMonth;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(columnDefinition = "text")
    private String note;

    @Column(name = "created_by_member_id", nullable = false)
    private Long createdByMemberId;

    @Column(name = "manually_completed_at")
    private OffsetDateTime manuallyCompletedAt;

    @Column(name = "manually_completed_by_member_id")
    private Long manuallyCompletedByMemberId;

    protected ParticipationAdminNote() {
    }

    public ParticipationAdminNote(Long memberId, LocalDate targetMonth, String note, Long createdByMemberId) {
        this.memberId = memberId;
        this.targetMonth = targetMonth;
        this.note = note;
        this.createdByMemberId = createdByMemberId;
    }

    public void markManuallyCompleted(Long adminMemberId, OffsetDateTime completedAt) {
        this.manuallyCompletedByMemberId = adminMemberId;
        this.manuallyCompletedAt = completedAt;
    }
}
