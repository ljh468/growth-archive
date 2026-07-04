package com.growtharchive.domain.monthly;

import com.growtharchive.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "monthly_reflections")
public class MonthlyReflection extends BaseEntity {

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "target_month", nullable = false)
    private LocalDate targetMonth;

    @Column(name = "well_done", columnDefinition = "text")
    private String wellDone;

    @Column(columnDefinition = "text")
    private String regret;

    @Column(name = "next_focus", columnDefinition = "text")
    private String nextFocus;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected MonthlyReflection() {
    }

    public MonthlyReflection(Long memberId, LocalDate targetMonth, String wellDone, String regret, String nextFocus) {
        this.memberId = memberId;
        this.targetMonth = targetMonth;
        this.wellDone = wellDone;
        this.regret = regret;
        this.nextFocus = nextFocus;
        this.status = "ACTIVE";
    }
}
