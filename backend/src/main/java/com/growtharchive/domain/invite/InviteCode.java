package com.growtharchive.domain.invite;

import com.growtharchive.domain.common.CreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "invite_codes")
public class InviteCode extends CreatedAtEntity {

    @Column(name = "code_hash", nullable = false, length = 255)
    private String codeHash;

    @Column(name = "code_preview", length = 30)
    private String codePreview;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_by_member_id")
    private Long createdByMemberId;

    @Column(name = "deactivated_at")
    private OffsetDateTime deactivatedAt;

    protected InviteCode() {
    }
}
