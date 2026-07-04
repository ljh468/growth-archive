package com.growtharchive.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "member_interest_tags")
public class MemberInterestTag {

    @EmbeddedId
    private MemberInterestTagId id;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected MemberInterestTag() {
    }

    public MemberInterestTag(Long memberId, Long interestTagId) {
        this.id = new MemberInterestTagId(memberId, interestTagId);
        this.createdAt = OffsetDateTime.now();
    }
}
