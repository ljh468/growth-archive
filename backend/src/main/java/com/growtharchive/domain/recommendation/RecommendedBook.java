package com.growtharchive.domain.recommendation;

import com.growtharchive.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "recommended_books")
public class RecommendedBook extends BaseEntity {

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "target_month", nullable = false)
    private LocalDate targetMonth;

    @Column(nullable = false, columnDefinition = "text")
    private String reason;

    @Column(name = "recommended_by_member_id")
    private Long recommendedByMemberId;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false, length = 30)
    private String status;

    protected RecommendedBook() {
    }

    public RecommendedBook(Long bookId, LocalDate targetMonth, String reason, Long recommendedByMemberId, int displayOrder) {
        this.bookId = bookId;
        this.targetMonth = targetMonth;
        this.reason = reason;
        this.recommendedByMemberId = recommendedByMemberId;
        this.displayOrder = displayOrder;
        this.status = "ACTIVE";
    }
}
