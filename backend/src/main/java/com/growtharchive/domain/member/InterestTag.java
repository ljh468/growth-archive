package com.growtharchive.domain.member;

import com.growtharchive.domain.common.CreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "interest_tags")
public class InterestTag extends CreatedAtEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 80)
    private String slug;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    protected InterestTag() {
    }
}
