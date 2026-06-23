package com.growtharchive.domain.admin;

import com.growtharchive.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

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

    protected ParticipationAdminNote() {
    }
}
