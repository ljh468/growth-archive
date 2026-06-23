package com.growtharchive.repository;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InterestTagRepository {

    private final JdbcTemplate jdbcTemplate;

    public InterestTagRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int countActiveIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        String placeholders = String.join(",", ids.stream().map(id -> "?").toList());
        Object[] args = ids.toArray();
        Integer count = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM interest_tags WHERE is_active = true AND id IN (" + placeholders + ")",
            Integer.class,
            args
        );
        return count == null ? 0 : count;
    }

    public void replaceMemberTags(Long memberId, List<Long> ids) {
        jdbcTemplate.update("DELETE FROM member_interest_tags WHERE member_id = ?", memberId);
        for (Long id : ids) {
            jdbcTemplate.update(
                "INSERT INTO member_interest_tags (member_id, interest_tag_id, created_at) VALUES (?, ?, now())",
                memberId,
                id
            );
        }
    }
}
