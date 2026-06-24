#!/usr/bin/env python3
"""CSV import helper for Growth Archive OPS-001 migration."""

from __future__ import annotations

import argparse
import csv
import os
import subprocess
import sys
import tempfile
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path


DISALLOWED_COLUMNS = {"source_url", "somoim_url", "notion_url", "original_url", "original_link"}
CONTENT_STATUSES = {"ACTIVE", "HIDDEN", "DELETED"}
MEETING_STATUSES = {"SCHEDULED", "HELD", "CANCELED", "HIDDEN", "DELETED"}
MEETING_TYPES = {"REGULAR_READING", "REGULAR_ACTION", "SMALL"}


@dataclass
class ReportRow:
    row_number: int
    migration_key: str
    import_type: str
    import_status: str
    content_status: str
    entity_type: str
    message: str


def main() -> int:
    parser = argparse.ArgumentParser(description="Import Growth Archive CSV migration files.")
    parser.add_argument("--type", required=True, choices=[
        "member-mapping",
        "join-intros",
        "reading-records",
        "meetings",
        "meeting-reviews",
    ])
    parser.add_argument("--csv", required=True, type=Path, help="UTF-8 CSV file to import.")
    parser.add_argument("--report", type=Path, help="Report CSV path. Defaults to scripts/import/reports/.")
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument("--dry-run", action="store_true")
    mode.add_argument("--commit", action="store_true")
    parser.add_argument("--database-url", default=os.environ.get("DATABASE_URL") or os.environ.get("GROWTH_ARCHIVE_DATABASE_URL"))
    parser.add_argument(
        "--psql-command",
        default=os.environ.get("PSQL_COMMAND"),
        help="Optional psql command prefix, for example: docker compose exec -T postgres psql -U growth_archive -d growth_archive",
    )
    args = parser.parse_args()

    rows = read_csv(args.csv)
    report: list[ReportRow] = []
    sql: list[str] = ["BEGIN;"]

    for index, row in enumerate(rows, start=2):
        row_report, row_sql = process_row(args.type, index, row, commit=args.commit)
        report.append(row_report)
        sql.extend(row_sql)

    sql.append("COMMIT;")
    report_path = write_report(args.report, args.type, report)

    failed = [row for row in report if row.import_status == "FAILED"]
    if failed:
        print(f"Validation failed for {len(failed)} row(s). Report: {report_path}", file=sys.stderr)
        return 1

    if args.dry_run:
        print(f"Dry run completed. Report: {report_path}")
        return 0

    if not args.database_url and not args.psql_command:
        print("Commit mode requires --database-url, DATABASE_URL, or --psql-command.", file=sys.stderr)
        return 1

    run_sql(args.database_url, args.psql_command, "\n".join(sql))
    print(f"Import committed. Report: {report_path}")
    return 0


def read_csv(path: Path) -> list[dict[str, str]]:
    if not path.exists():
        raise SystemExit(f"CSV not found: {path}")
    with path.open("r", encoding="utf-8-sig", newline="") as file:
        reader = csv.DictReader(file)
        if reader.fieldnames is None:
            raise SystemExit("CSV header is required.")
        disallowed = DISALLOWED_COLUMNS.intersection({name.strip() for name in reader.fieldnames})
        if disallowed:
            raise SystemExit(f"Original source link columns are not allowed: {', '.join(sorted(disallowed))}")
        return [{key: (value or "").strip() for key, value in row.items()} for row in reader]


def process_row(import_type: str, row_number: int, row: dict[str, str], commit: bool) -> tuple[ReportRow, list[str]]:
    migration_key = value(row, "migration_key") or f"row-{row_number}"
    try:
        if import_type == "member-mapping":
            return process_member_mapping(row_number, migration_key, row, commit)
        if import_type == "join-intros":
            return process_join_intro(row_number, migration_key, row, commit)
        if import_type == "reading-records":
            return process_reading_record(row_number, migration_key, row, commit)
        if import_type == "meetings":
            return process_meeting(row_number, migration_key, row, commit)
        if import_type == "meeting-reviews":
            return process_meeting_review(row_number, migration_key, row, commit)
    except ValueError as error:
        return ReportRow(row_number, migration_key, import_type, "FAILED", "HIDDEN", "", str(error)), []
    raise ValueError(f"Unsupported import type: {import_type}")


def process_member_mapping(row_number: int, migration_key: str, row: dict[str, str], commit: bool) -> tuple[ReportRow, list[str]]:
    member_ref = member_lookup(row)
    participation_start_month = value(row, "participation_start_month")
    sql: list[str] = []
    if commit and member_ref and participation_start_month:
        sql.append(
            "UPDATE members SET participation_start_month = {month}::date, updated_at = now() "
            "WHERE id = ({member_select});".format(
                month=sql_literal(normalize_month(participation_start_month)),
                member_select=member_ref,
            )
        )
    status = "IMPORTED" if commit and member_ref else "MAPPED" if member_ref else "NEEDS_REVIEW"
    message = "member mapped" if member_ref else "member_id or resolvable nickname/real_name is required"
    return ReportRow(row_number, migration_key, "member-mapping", status, "HIDDEN", "MEMBER", message), sql


def process_join_intro(row_number: int, migration_key: str, row: dict[str, str], commit: bool) -> tuple[ReportRow, list[str]]:
    require(row, "raw_intro_text")
    member_ref = member_lookup(row)
    import_status = "MAPPED" if member_ref else "PENDING"
    sql: list[str] = []
    if commit:
        member_select = f"({member_ref})" if member_ref else "null"
        sql.append(
            """
            INSERT INTO member_join_intro_sources (
                member_id, migration_key, member_alias, raw_intro_text,
                parsed_join_reason, parsed_current_concern, parsed_interests_text,
                parsed_three_year_goal, source_created_at, import_status, created_at, updated_at
            )
            VALUES (
                {member_id}, {migration_key}, {member_alias}, {raw_intro_text},
                {parsed_join_reason}, {parsed_current_concern}, {parsed_interests_text},
                {parsed_three_year_goal}, {source_created_at}, {import_status}, now(), now()
            )
            ON CONFLICT (migration_key)
            DO UPDATE SET member_id = excluded.member_id,
                          member_alias = excluded.member_alias,
                          raw_intro_text = excluded.raw_intro_text,
                          parsed_join_reason = excluded.parsed_join_reason,
                          parsed_current_concern = excluded.parsed_current_concern,
                          parsed_interests_text = excluded.parsed_interests_text,
                          parsed_three_year_goal = excluded.parsed_three_year_goal,
                          source_created_at = excluded.source_created_at,
                          import_status = excluded.import_status,
                          updated_at = now();
            """.format(
                member_id=member_select,
                migration_key=sql_literal(migration_key),
                member_alias=sql_literal(value(row, "member_alias")),
                raw_intro_text=sql_literal(value(row, "raw_intro_text")),
                parsed_join_reason=sql_literal(value(row, "parsed_join_reason")),
                parsed_current_concern=sql_literal(value(row, "parsed_current_concern")),
                parsed_interests_text=sql_literal(value(row, "parsed_interests_text")),
                parsed_three_year_goal=sql_literal(value(row, "parsed_three_year_goal")),
                source_created_at=sql_timestamptz(value(row, "source_created_at")),
                import_status=sql_literal(import_status),
            )
        )
    report_status = "IMPORTED" if commit else import_status
    return ReportRow(row_number, migration_key, "join-intros", report_status, "HIDDEN", "MEMBER_JOIN_INTRO", "Guest-hidden intro source"), sql


def process_reading_record(row_number: int, migration_key: str, row: dict[str, str], commit: bool) -> tuple[ReportRow, list[str]]:
    require(row, "book_title")
    require(row, "author")
    require(row, "one_line_review")
    require(row, "recorded_at")
    content_status = normalize_content_status(value(row, "content_status"))
    member_ref = member_lookup(row)
    if not member_ref:
        return ReportRow(row_number, migration_key, "reading-records", "NEEDS_REVIEW", "HIDDEN", "READING_RECORD", "unmapped author; not inserted"), []

    sql: list[str] = []
    if commit:
        sql.append(
            """
            WITH target_member AS (
                SELECT id, onboarding_completed_at, deactivated_at FROM members WHERE id = ({member_select}) LIMIT 1
            ),
            existing_book AS (
                SELECT id FROM books
                WHERE ({isbn13} IS NOT NULL AND isbn13 = {isbn13})
                   OR ({isbn10} IS NOT NULL AND isbn10 = {isbn10})
                   OR (lower(title) = lower({book_title}) AND lower(authors_text) = lower({author}))
                ORDER BY id ASC LIMIT 1
            ),
            inserted_book AS (
                INSERT INTO books (
                    source, isbn13, isbn10, title, authors_text, publisher, status,
                    created_by_member_id, created_at, updated_at
                )
                SELECT 'MANUAL', {isbn13}, {isbn10}, {book_title}, {author}, null, 'UNVERIFIED',
                       (SELECT id FROM target_member), now(), now()
                WHERE NOT EXISTS (SELECT 1 FROM existing_book)
                RETURNING id
            ),
            final_book AS (
                SELECT id FROM existing_book
                UNION ALL
                SELECT id FROM inserted_book
                LIMIT 1
            )
            INSERT INTO reading_records (
                member_id, book_id, rating, one_line_review, blog_url,
                representative_image_id, status, recorded_at, created_at, updated_at, hidden_at
            )
            SELECT tm.id, fb.id, {rating}, {one_line_review}, {blog_url},
                   null,
                   CASE WHEN {requested_status} = 'ACTIVE'
                             AND tm.onboarding_completed_at IS NOT NULL
                             AND tm.deactivated_at IS NULL
                        THEN 'ACTIVE' ELSE 'HIDDEN' END,
                   {recorded_at}, now(), now(),
                   CASE WHEN {requested_status} = 'ACTIVE'
                             AND tm.onboarding_completed_at IS NOT NULL
                             AND tm.deactivated_at IS NULL
                        THEN null ELSE now() END
            FROM target_member tm
            CROSS JOIN final_book fb
            WHERE NOT EXISTS (
                SELECT 1 FROM reading_records rr
                WHERE rr.member_id = tm.id AND rr.book_id = fb.id AND rr.blog_url = {blog_url}
            );
            """.format(
                member_select=member_ref,
                isbn13=sql_literal(value(row, "isbn13")),
                isbn10=sql_literal(value(row, "isbn10")),
                book_title=sql_literal(value(row, "book_title")),
                author=sql_literal(value(row, "author")),
                rating=sql_int(value(row, "rating")),
                one_line_review=sql_literal(value(row, "one_line_review")),
                blog_url=sql_literal(value(row, "blog_url")),
                requested_status=sql_literal(content_status),
                recorded_at=sql_timestamptz(value(row, "recorded_at")),
            )
        )
    return ReportRow(row_number, migration_key, "reading-records", "IMPORTED" if commit else "MAPPED", content_status, "READING_RECORD", "blog_url preserved; source links not stored"), sql


def process_meeting(row_number: int, migration_key: str, row: dict[str, str], commit: bool) -> tuple[ReportRow, list[str]]:
    require(row, "meeting_type")
    require(row, "title")
    require(row, "scheduled_at")
    require(row, "location_region")
    meeting_type = value(row, "meeting_type").upper()
    status = value(row, "status").upper() or "HELD"
    if meeting_type not in MEETING_TYPES:
        raise ValueError(f"meeting_type must be one of {sorted(MEETING_TYPES)}")
    if status not in MEETING_STATUSES:
        raise ValueError(f"status must be one of {sorted(MEETING_STATUSES)}")
    sql: list[str] = []
    if commit:
        sql.append(
            """
            INSERT INTO meetings (
                meeting_type, title, description, meeting_at, region_text, detail_address,
                capacity, cost_amount, status, target_month, is_auto_generated, created_at, updated_at
            )
            SELECT {meeting_type}, {title}, {description}, {scheduled_at}, {location_region},
                   {exact_location}, {capacity}, {fee_amount}, {status},
                   CASE WHEN {meeting_type} IN ('REGULAR_READING', 'REGULAR_ACTION')
                        THEN date_trunc('month', {scheduled_at}::timestamptz)::date ELSE null END,
                   false, now(), now()
            WHERE NOT EXISTS (
                SELECT 1 FROM meetings
                WHERE title = {title} AND meeting_at = {scheduled_at}::timestamptz
                  AND status <> 'DELETED'
            );
            """.format(
                meeting_type=sql_literal(meeting_type),
                title=sql_literal(value(row, "title")),
                description=sql_literal(value(row, "description")),
                scheduled_at=sql_timestamptz(value(row, "scheduled_at")),
                location_region=sql_literal(value(row, "location_region")),
                exact_location=sql_literal(value(row, "exact_location")),
                capacity=sql_int(value(row, "capacity")),
                fee_amount=sql_int(value(row, "fee_amount"), default=0),
                status=sql_literal(status),
            )
        )
    return ReportRow(row_number, migration_key, "meetings", "IMPORTED" if commit else "MAPPED", status, "MEETING", "meeting import is idempotent by title + scheduled_at"), sql


def process_meeting_review(row_number: int, migration_key: str, row: dict[str, str], commit: bool) -> tuple[ReportRow, list[str]]:
    require(row, "review_title")
    require(row, "review_content")
    require(row, "created_at")
    content_status = normalize_content_status(value(row, "content_status"))
    member_ref = member_lookup(row)
    images = split_images(value(row, "selected_image_paths"))
    if len(images) > 10:
        raise ValueError("selected_image_paths can contain at most 10 images")
    if not member_ref:
        return ReportRow(row_number, migration_key, "meeting-reviews", "NEEDS_REVIEW", "HIDDEN", "MEETING_REVIEW", "unmapped author; not inserted"), []
    meeting_ref = meeting_lookup(row)
    if not meeting_ref:
        return ReportRow(row_number, migration_key, "meeting-reviews", "NEEDS_REVIEW", "HIDDEN", "MEETING_REVIEW", "meeting_id or meeting_title is required"), []

    sql: list[str] = []
    if commit:
        sql.append(
            """
            WITH target_member AS (
                SELECT id, onboarding_completed_at, deactivated_at FROM members WHERE id = ({member_select}) LIMIT 1
            ),
            target_meeting AS (
                SELECT id FROM meetings WHERE id = ({meeting_select}) LIMIT 1
            )
            INSERT INTO meeting_reviews (
                meeting_id, member_id, title, content, representative_image_id,
                status, created_at, updated_at, hidden_at
            )
            SELECT tmtn.id, tm.id, {review_title}, {review_content}, null,
                   CASE WHEN {requested_status} = 'ACTIVE'
                             AND tm.onboarding_completed_at IS NOT NULL
                             AND tm.deactivated_at IS NULL
                        THEN 'ACTIVE' ELSE 'HIDDEN' END,
                   {created_at}, now(),
                   CASE WHEN {requested_status} = 'ACTIVE'
                             AND tm.onboarding_completed_at IS NOT NULL
                             AND tm.deactivated_at IS NULL
                        THEN null ELSE now() END
            FROM target_member tm
            CROSS JOIN target_meeting tmtn
            WHERE NOT EXISTS (
                SELECT 1 FROM meeting_reviews mr
                WHERE mr.member_id = tm.id AND mr.title = {review_title} AND mr.created_at = {created_at}::timestamptz
            );
            """.format(
                member_select=member_ref,
                meeting_select=meeting_ref,
                review_title=sql_literal(value(row, "review_title")),
                review_content=sql_literal(value(row, "review_content")),
                requested_status=sql_literal(content_status),
                created_at=sql_timestamptz(value(row, "created_at")),
            )
        )
        for order, image_path in enumerate(images, start=1):
            sql.append(review_image_sql(row, member_ref, image_path, order))
    message = "selected photos accepted" if images else "review without selected photos"
    return ReportRow(row_number, migration_key, "meeting-reviews", "IMPORTED" if commit else "MAPPED", content_status, "MEETING_REVIEW", message), sql


def review_image_sql(row: dict[str, str], member_ref: str, image_path: str, display_order: int) -> str:
    public_url = image_path if image_path.startswith("http://") or image_path.startswith("https://") else ""
    return """
        WITH target_member AS (
            SELECT id FROM members WHERE id = ({member_select}) LIMIT 1
        ),
        target_review AS (
            SELECT mr.id
            FROM meeting_reviews mr
            JOIN target_member tm ON tm.id = mr.member_id
            WHERE mr.title = {review_title} AND mr.created_at = {created_at}::timestamptz
            LIMIT 1
        ),
        existing_asset AS (
            SELECT id FROM image_assets WHERE image_type = 'MEETING_REVIEW' AND object_key = {object_key} LIMIT 1
        ),
        inserted_asset AS (
            INSERT INTO image_assets (
                owner_member_id, bucket, object_key, public_url, image_type, mime_type, created_at
            )
            SELECT (SELECT id FROM target_member), 'migration', {object_key}, {public_url},
                   'MEETING_REVIEW', 'image/jpeg', now()
            WHERE EXISTS (SELECT 1 FROM target_review)
              AND NOT EXISTS (SELECT 1 FROM existing_asset)
            RETURNING id
        ),
        final_asset AS (
            SELECT id FROM existing_asset
            UNION ALL
            SELECT id FROM inserted_asset
            LIMIT 1
        )
        INSERT INTO meeting_review_images (meeting_review_id, image_asset_id, display_order, created_at)
        SELECT tr.id, fa.id, {display_order}, now()
        FROM target_review tr
        CROSS JOIN final_asset fa
        WHERE NOT EXISTS (
            SELECT 1 FROM meeting_review_images
            WHERE meeting_review_id = tr.id AND display_order = {display_order}
        );
        """.format(
        member_select=member_ref,
        review_title=sql_literal(value(row, "review_title")),
        created_at=sql_timestamptz(value(row, "created_at")),
        object_key=sql_literal(image_path),
        public_url=sql_literal(public_url),
        display_order=display_order,
    )


def member_lookup(row: dict[str, str]) -> str:
    member_id = value(row, "member_id")
    if member_id:
        return f"SELECT id FROM members WHERE id = {sql_int(member_id)}"
    alias = value(row, "member_alias") or value(row, "nickname") or value(row, "real_name")
    if not alias:
        return ""
    return (
        "SELECT id FROM members "
        f"WHERE lower(nickname) = lower({sql_literal(alias)}) OR lower(coalesce(real_name, '')) = lower({sql_literal(alias)}) "
        "ORDER BY onboarding_completed_at DESC NULLS LAST, id ASC LIMIT 1"
    )


def meeting_lookup(row: dict[str, str]) -> str:
    meeting_id = value(row, "meeting_id")
    if meeting_id:
        return f"SELECT id FROM meetings WHERE id = {sql_int(meeting_id)}"
    title = value(row, "meeting_title")
    if not title:
        return ""
    return f"SELECT id FROM meetings WHERE title = {sql_literal(title)} AND status <> 'DELETED' ORDER BY meeting_at DESC LIMIT 1"


def write_report(path: Path | None, import_type: str, rows: list[ReportRow]) -> Path:
    if path is None:
        reports_dir = Path("scripts/import/reports")
        reports_dir.mkdir(parents=True, exist_ok=True)
        timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
        path = reports_dir / f"{import_type}-{timestamp}.csv"
    else:
        path.parent.mkdir(parents=True, exist_ok=True)

    with path.open("w", encoding="utf-8", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=[
            "row_number",
            "migration_key",
            "import_type",
            "import_status",
            "content_status",
            "entity_type",
            "message",
        ])
        writer.writeheader()
        for row in rows:
            writer.writerow(row.__dict__)
    return path


def run_sql(database_url: str | None, psql_command: str | None, sql: str) -> None:
    if psql_command:
        command = psql_command.split() + ["-v", "ON_ERROR_STOP=1"]
        subprocess.run(command, input=sql, text=True, check=True)
        return

    with tempfile.NamedTemporaryFile("w", encoding="utf-8", suffix=".sql", delete=False) as file:
        file.write(sql)
        sql_path = file.name
    try:
        command = ["psql", database_url or "", "-v", "ON_ERROR_STOP=1", "-f", sql_path]
        subprocess.run(command, check=True)
    finally:
        Path(sql_path).unlink(missing_ok=True)


def require(row: dict[str, str], key: str) -> str:
    item = value(row, key)
    if not item:
        raise ValueError(f"{key} is required")
    return item


def value(row: dict[str, str], key: str) -> str:
    return (row.get(key) or "").strip()


def normalize_content_status(item: str) -> str:
    status = (item or "HIDDEN").upper()
    if status not in CONTENT_STATUSES:
        raise ValueError(f"content_status must be one of {sorted(CONTENT_STATUSES)}")
    return status


def normalize_month(item: str) -> str:
    if len(item) == 7:
        return item + "-01"
    return item


def split_images(item: str) -> list[str]:
    if not item:
        return []
    return [part.strip() for part in item.split(";") if part.strip()]


def sql_literal(item: str | None) -> str:
    if item is None or item == "":
        return "null"
    return "'" + item.replace("'", "''") + "'"


def sql_timestamptz(item: str) -> str:
    return sql_literal(item) + "::timestamptz"


def sql_int(item: str, default: int | None = None) -> str:
    if item == "":
        return "null" if default is None else str(default)
    try:
        return str(int(item))
    except ValueError as error:
        raise ValueError(f"expected integer, got {item}") from error


if __name__ == "__main__":
    raise SystemExit(main())
