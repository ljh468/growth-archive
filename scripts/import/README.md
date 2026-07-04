# Growth Archive Import Tools

CSV import support for OPS-001 data migration.

## Templates

Templates live in `scripts/import/templates/`.

- `member_mapping.csv`
- `join_intro.csv`
- `reading_record.csv`
- `meeting.csv`
- `meeting_review.csv`

All CSV files must be UTF-8. Keep Somoim/Notion original URLs out of CSV files. The import script rejects common original-link columns such as `source_url`, `somoim_url`, `notion_url`, and `original_url`.

## Dry Run

```bash
python3 scripts/import/import_growth_archive.py --type reading-records --csv scripts/import/templates/reading_record.csv --dry-run
```

## Commit

Commit mode requires `psql` and a database URL.

```bash
DATABASE_URL=postgresql://growth_archive:growth_archive@localhost:5432/growth_archive \
python3 scripts/import/import_growth_archive.py --type reading-records --csv data/reading_record.csv --commit
```

The script writes a report CSV for every run. Rows without a mapped active/onboarded author are not published. They are reported as `NEEDS_REVIEW` with `content_status=HIDDEN`.

