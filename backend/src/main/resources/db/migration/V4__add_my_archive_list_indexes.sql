CREATE INDEX IF NOT EXISTS idx_meeting_reviews_member_recent_visible
ON meeting_reviews (member_id, created_at DESC, id DESC)
WHERE status <> 'DELETED';

CREATE INDEX IF NOT EXISTS idx_meetings_host_small_recent_visible
ON meetings (host_member_id, meeting_at DESC, id DESC)
WHERE meeting_type = 'SMALL' AND status <> 'DELETED';
