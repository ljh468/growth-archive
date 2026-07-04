package com.growtharchive.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MemberInterestTagId implements Serializable {

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "interest_tag_id")
    private Long interestTagId;

    protected MemberInterestTagId() {
    }

    public MemberInterestTagId(Long memberId, Long interestTagId) {
        this.memberId = memberId;
        this.interestTagId = interestTagId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MemberInterestTagId that)) {
            return false;
        }
        return Objects.equals(memberId, that.memberId)
            && Objects.equals(interestTagId, that.interestTagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, interestTagId);
    }
}
