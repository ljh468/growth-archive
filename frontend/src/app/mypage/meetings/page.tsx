"use client";

import Image from "next/image";
import { useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { MobileBackButton } from "@/components/MobileBackButton";
import { Button, EmptyState, MoreButton, PageHeader, Section, SkeletonBlock, Tag } from "@/components/ui/primitives";
import { apiGet, type MeetingSummary } from "@/lib/api";

export default function MyMeetingsPage() {
  return (
    <AuthGate required="MEMBER">
      {() => <MyMeetingsContent />}
    </AuthGate>
  );
}

function MyMeetingsContent() {
  const [meetings, setMeetings] = useState<MeetingSummary[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    loadMeetings(0)
      .then(({ hasMore, items }) => {
        setMeetings(items);
        setHasMore(hasMore);
        setPage(0);
      })
      .catch(() => setMessage("내가 만든 소소모임을 불러오지 못했습니다."))
      .finally(() => setLoading(false));
  }, []);

  async function more() {
    if (loadingMore) {
      return;
    }
    setLoadingMore(true);
    const nextPage = page + 1;
    try {
      const result = await loadMeetings(nextPage);
      setMeetings((current) => [...current, ...result.items]);
      setHasMore(result.hasMore);
      setPage(nextPage);
    } catch {
      setMessage("추가 소소모임을 불러오지 못했습니다.");
    } finally {
      setLoadingMore(false);
    }
  }

  return (
    <main>
      <Section>
        <div className="grid gap-5 sm:gap-8">
          <MobileBackButton fallbackHref="/mypage" />
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <PageHeader eyebrow="My Meetings" title="내가 만든 소소모임" description="직접 만든 소소모임을 최신 일정순으로 확인합니다." />
            <Button href="/meetings/new">소소모임 만들기</Button>
          </div>
          {message && <p className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.78)] px-4 py-3 text-sm leading-6 text-[var(--color-charcoal)]">{message}</p>}
          {loading ? (
            <MeetingSkeleton />
          ) : meetings.length === 0 ? (
            <EmptyState title="아직 만든 소소모임이 없습니다" description="작게 모이고 싶은 주제가 생기면 소소모임을 만들어 보세요." />
          ) : (
            <div className="grid gap-4">
              {meetings.map((meeting) => (
                <a className="grid grid-cols-[84px_1fr] gap-4 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3 shadow-[var(--shadow-soft)] transition hover:border-[rgba(31,77,58,0.28)] sm:grid-cols-[120px_1fr] sm:p-4" href={`/meetings/${meeting.id}`} key={meeting.id}>
                  <div className="aspect-[4/3] overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-line)]">
                    <Image alt="" className="h-full w-full object-cover photo-muted" height={90} src={meeting.coverImageUrl || "/images/meeting-table.jpg"} unoptimized width={120} />
                  </div>
                  <div className="min-w-0">
                    <div className="flex flex-wrap gap-2">
                      <Tag>{meeting.status === "SCHEDULED" ? "예정" : meeting.status === "HELD" ? "완료" : meeting.status === "HIDDEN" ? "숨김" : "취소"}</Tag>
                      <Tag>{meeting.attendeeCount}명</Tag>
                    </div>
                    <p className="mt-3 line-clamp-2 font-display text-base font-normal leading-snug text-[var(--color-ink)] sm:text-lg">{meeting.title}</p>
                    <p className="mt-2 line-clamp-2 text-xs leading-5 text-[var(--color-charcoal)] sm:text-sm sm:leading-6">{meeting.description}</p>
                    <p className="mt-3 text-xs text-[var(--color-muted)]">{formatDateTime(meeting.meetingAt)} · {meeting.locationRegion}</p>
                  </div>
                </a>
              ))}
              {hasMore && (
                <MoreButton className="justify-self-center" disabled={loadingMore} onClick={more}>
                  {loadingMore ? "Loading" : "More"}
                </MoreButton>
              )}
            </div>
          )}
        </div>
      </Section>
    </main>
  );
}

async function loadMeetings(page: number) {
  const size = 10;
  const result = await apiGet<MeetingSummary[]>(`/meetings/mine/created?page=${page}&size=${size}`);
  if (!result.success) {
    throw new Error(result.error?.message ?? "소소모임을 불러오지 못했습니다.");
  }
  return { items: result.data, hasMore: result.data.length === size };
}

function MeetingSkeleton() {
  return (
    <div className="grid gap-4">
      {Array.from({ length: 3 }).map((_, index) => (
        <div className="grid grid-cols-[84px_1fr] gap-4 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3" key={index}>
          <SkeletonBlock className="aspect-[4/3]" />
          <div className="grid gap-3">
            <SkeletonBlock className="h-7 w-24 rounded-full" />
            <SkeletonBlock className="h-5 w-3/4" />
            <SkeletonBlock className="h-4 w-full" />
          </div>
        </div>
      ))}
    </div>
  );
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeStyle: "short",
    timeZone: "Asia/Seoul",
  }).format(new Date(value));
}
