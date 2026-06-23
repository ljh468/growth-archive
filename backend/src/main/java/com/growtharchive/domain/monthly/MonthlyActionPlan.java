package com.growtharchive.domain.monthly;

import com.growtharchive.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "monthly_action_plans")
public class MonthlyActionPlan extends BaseEntity {

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "target_month", nullable = false)
    private LocalDate targetMonth;

    @Column(length = 120)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected MonthlyActionPlan() {
    }
}
