package com.growtharchive.repository;

import com.growtharchive.service.monthly.MonthlyActionPlanView;
import com.growtharchive.service.monthly.MonthlyReflectionView;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MonthlyRepository {

    private final JdbcTemplate jdbcTemplate;

    public MonthlyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<MonthlyActionPlanView> findActionPlan(Long memberId, LocalDate targetMonth) {
        return jdbcTemplate.query(
            """
                SELECT id, member_id, target_month, title, content, status, created_at, updated_at
                FROM monthly_action_plans
                WHERE member_id = ? AND target_month = ? AND status <> 'DELETED'
                """,
            rs -> rs.next() ? Optional.of(mapActionPlan(rs)) : Optional.empty(),
            memberId,
            Date.valueOf(targetMonth)
        );
    }

    public Optional<MonthlyActionPlanView> findActionPlanById(Long actionPlanId) {
        return jdbcTemplate.query(
            """
                SELECT id, member_id, target_month, title, content, status, created_at, updated_at
                FROM monthly_action_plans
                WHERE id = ? AND status <> 'DELETED'
                """,
            rs -> rs.next() ? Optional.of(mapActionPlan(rs)) : Optional.empty(),
            actionPlanId
        );
    }

    public Long upsertActionPlan(Long memberId, LocalDate targetMonth, String title, String content) {
        Optional<MonthlyActionPlanView> existing = findActionPlan(memberId, targetMonth);
        if (existing.isPresent()) {
            jdbcTemplate.update(
                """
                    UPDATE monthly_action_plans
                    SET title = ?, content = ?, status = 'ACTIVE', deleted_at = null, updated_at = now()
                    WHERE id = ?
                    """,
                title,
                content,
                existing.get().id()
            );
            return existing.get().id();
        }
        return jdbcTemplate.queryForObject(
            """
                INSERT INTO monthly_action_plans (member_id, target_month, title, content, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, 'ACTIVE', now(), now())
                RETURNING id
                """,
            Long.class,
            memberId,
            Date.valueOf(targetMonth),
            title,
            content
        );
    }

    public void updateActionPlan(Long actionPlanId, String title, String content) {
        jdbcTemplate.update(
            """
                UPDATE monthly_action_plans
                SET title = ?, content = ?, updated_at = now()
                WHERE id = ? AND status <> 'DELETED'
                """,
            title,
            content,
            actionPlanId
        );
    }

    public void deleteActionPlan(Long actionPlanId) {
        jdbcTemplate.update(
            """
                UPDATE monthly_action_plans
                SET status = 'DELETED', deleted_at = coalesce(deleted_at, now()), updated_at = now()
                WHERE id = ? AND status <> 'DELETED'
                """,
            actionPlanId
        );
    }

    public Optional<MonthlyReflectionView> findReflection(Long memberId, LocalDate targetMonth) {
        return jdbcTemplate.query(
            """
                SELECT id, member_id, target_month, well_done, regret, next_focus, status, created_at, updated_at
                FROM monthly_reflections
                WHERE member_id = ? AND target_month = ? AND status <> 'DELETED'
                """,
            rs -> rs.next() ? Optional.of(mapReflection(rs)) : Optional.empty(),
            memberId,
            Date.valueOf(targetMonth)
        );
    }

    public Long upsertReflection(Long memberId, LocalDate targetMonth, String wellDone, String regret, String nextFocus) {
        Optional<MonthlyReflectionView> existing = findReflection(memberId, targetMonth);
        if (existing.isPresent()) {
            jdbcTemplate.update(
                """
                    UPDATE monthly_reflections
                    SET well_done = ?, regret = ?, next_focus = ?, status = 'ACTIVE',
                        deleted_at = null, updated_at = now()
                    WHERE id = ?
                    """,
                wellDone,
                regret,
                nextFocus,
                existing.get().id()
            );
            return existing.get().id();
        }
        return jdbcTemplate.queryForObject(
            """
                INSERT INTO monthly_reflections (
                    member_id, target_month, well_done, regret, next_focus, status, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, 'ACTIVE', now(), now())
                RETURNING id
                """,
            Long.class,
            memberId,
            Date.valueOf(targetMonth),
            wellDone,
            regret,
            nextFocus
        );
    }

    public Optional<MonthlyReflectionView> findReflectionById(Long reflectionId) {
        return jdbcTemplate.query(
            """
                SELECT id, member_id, target_month, well_done, regret, next_focus, status, created_at, updated_at
                FROM monthly_reflections
                WHERE id = ? AND status <> 'DELETED'
                """,
            rs -> rs.next() ? Optional.of(mapReflection(rs)) : Optional.empty(),
            reflectionId
        );
    }

    private MonthlyActionPlanView mapActionPlan(ResultSet rs) throws SQLException {
        return new MonthlyActionPlanView(
            rs.getLong("id"),
            rs.getLong("member_id"),
            rs.getDate("target_month").toLocalDate(),
            rs.getString("title"),
            rs.getString("content"),
            rs.getString("status"),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getObject("updated_at", OffsetDateTime.class)
        );
    }

    private MonthlyReflectionView mapReflection(ResultSet rs) throws SQLException {
        return new MonthlyReflectionView(
            rs.getLong("id"),
            rs.getLong("member_id"),
            rs.getDate("target_month").toLocalDate(),
            rs.getString("well_done"),
            rs.getString("regret"),
            rs.getString("next_focus"),
            rs.getString("status"),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getObject("updated_at", OffsetDateTime.class)
        );
    }
}
