INSERT INTO meeting_attendances (meeting_id, member_id, status, created_at, updated_at)
SELECT id,
       host_member_id,
       'JOINED',
       created_at,
       now()
FROM meetings
WHERE meeting_type = 'SMALL'
  AND host_member_id IS NOT NULL
  AND status <> 'DELETED'
ON CONFLICT (meeting_id, member_id)
DO UPDATE SET status = 'JOINED',
              updated_at = excluded.updated_at;
