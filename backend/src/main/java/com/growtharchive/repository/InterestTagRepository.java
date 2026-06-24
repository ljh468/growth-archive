package com.growtharchive.repository;

import java.util.List;
import java.util.Optional;
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

    public List<InterestTagRow> findAllForAdmin() {
        return jdbcTemplate.query(
            """
                SELECT id, name, slug, display_order, is_active
                FROM interest_tags
                ORDER BY display_order ASC, id ASC
                """,
            (rs, rowNum) -> new InterestTagRow(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("slug"),
                rs.getInt("display_order"),
                rs.getBoolean("is_active")
            )
        );
    }

    public Optional<InterestTagRow> findById(Long tagId) {
        return jdbcTemplate.query(
            """
                SELECT id, name, slug, display_order, is_active
                FROM interest_tags
                WHERE id = ?
                """,
            rs -> rs.next()
                ? Optional.of(new InterestTagRow(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("slug"),
                    rs.getInt("display_order"),
                    rs.getBoolean("is_active")
                ))
                : Optional.empty(),
            tagId
        );
    }

    public Long create(String name, String slug, int displayOrder) {
        return jdbcTemplate.queryForObject(
            """
                INSERT INTO interest_tags (name, slug, display_order, is_active, created_at)
                VALUES (?, ?, ?, true, now())
                RETURNING id
                """,
            Long.class,
            name,
            slug,
            displayOrder
        );
    }

    public void update(Long tagId, String name, String slug, int displayOrder, boolean active) {
        jdbcTemplate.update(
            """
                UPDATE interest_tags
                SET name = ?, slug = ?, display_order = ?, is_active = ?
                WHERE id = ?
                """,
            name,
            slug,
            displayOrder,
            active,
            tagId
        );
    }

    public void deactivate(Long tagId) {
        jdbcTemplate.update("UPDATE interest_tags SET is_active = false WHERE id = ?", tagId);
    }

    public record InterestTagRow(Long id, String name, String slug, int displayOrder, boolean active) {
    }
}
