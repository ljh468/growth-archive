package com.growtharchive.domain.member;

import com.growtharchive.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "member_join_intro_sources")
public class MemberJoinIntroSource extends BaseEntity {

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "migration_key", nullable = false, length = 100)
    private String migrationKey;

    @Column(name = "member_alias", length = 100)
    private String memberAlias;

    @Column(name = "raw_intro_text", nullable = false, columnDefinition = "text")
    private String rawIntroText;

    @Column(name = "parsed_join_reason", columnDefinition = "text")
    private String parsedJoinReason;

    @Column(name = "parsed_current_concern", columnDefinition = "text")
    private String parsedCurrentConcern;

    @Column(name = "parsed_interests_text", columnDefinition = "text")
    private String parsedInterestsText;

    @Column(name = "parsed_three_year_goal", columnDefinition = "text")
    private String parsedThreeYearGoal;

    @Column(name = "source_created_at")
    private OffsetDateTime sourceCreatedAt;

    @Column(name = "import_status", nullable = false, length = 30)
    private String importStatus;

    protected MemberJoinIntroSource() {
    }
}
