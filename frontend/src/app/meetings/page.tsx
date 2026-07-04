"use client";

import Image from "next/image";
import { useEffect, useState } from "react";
import { Button, EmptyState, Section, SkeletonBlock, Tag } from "@/components/ui/primitives";
import { apiGet, type MeetingSummary } from "@/lib/api";

type PastMeetingGroup = {
  monthOffset: number;
  label: string;
  meetings: MeetingSummary[];
};

export default function MeetingsPage() {
  const [currentMeetings, setCurrentMeetings] = useState<MeetingSummary[]>([]);
  const [pastGroups, setPastGroups] = useState<PastMeetingGroup[]>([]);
  const [nextPastOffset, setNextPastOffset] = useState(1);
  const [error, setError] = useState<string | null>(null);
  const [loadingCurrent, setLoadingCurrent] = useState(true);
  const [loadingPast, setLoadingPast] = useState(false);

  useEffect(() => {
    apiGet<MeetingSummary[]>("/meetings?scope=current&page=0&size=50")
      .then((result) => {
        if (!result.success) {
          setError(result.error?.message ?? "모임을 불러오지 못했습니다.");
          return;
        }
        setCurrentMeetings(result.data);
      })
      .catch(() => setError("모임을 불러오지 못했습니다."))
      .finally(() => setLoadingCurrent(false));
  }, []);

  async function loadPastMonth() {
    if (loadingPast) {
      return;
    }
    setLoadingPast(true);
    const monthOffset = nextPastOffset;
    const result = await apiGet<MeetingSummary[]>(`/meetings?scope=past&monthOffset=${monthOffset}&page=0&size=50`);
    if (!result.success) {
      setError(result.error?.message ?? "지난 모임을 불러오지 못했습니다.");
      setLoadingPast(false);
      return;
    }
    setPastGroups((groups) => [
      ...groups,
      {
        monthOffset,
        label: monthLabel(monthOffset),
        meetings: result.data,
      },
    ]);
    setNextPastOffset((offset) => offset + 1);
    setLoadingPast(false);
  }

  return (
    <main>
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 bg-cover bg-center photo-muted" style={{ backgroundImage: "url(/images/korean-bookclub-discussion.jpg)" }} />
        <div className="absolute inset-0 bg-[linear-gradient(90deg,rgba(31,47,36,0.72),rgba(45,63,49,0.28))]" />
        <div className="relative mx-auto flex min-h-[420px] max-w-6xl items-end px-5 py-16 text-[var(--color-warm-white)] sm:px-8 lg:px-10">
          <div className="flex w-full flex-col gap-6 sm:flex-row sm:items-end sm:justify-between">
            <div className="max-w-3xl">
              <p className="font-latin text-4xl text-[#efe7d8] sm:text-5xl">Meetings</p>
              <h1 className="mt-4 font-display text-3xl font-normal leading-[1.2] sm:text-4xl">모임</h1>
              <p className="mt-5 hidden max-w-2xl text-base leading-8 text-[rgba(255,253,248,0.84)] sm:block">
                이번 달 자동 생성 정기모임 2개와 소소모임을 확인합니다. 지난 모임은 간략한 기록으로 살펴봅니다.
              </p>
            </div>
            <Button href="/meetings/new" variant="secondary">소소모임 만들기</Button>
          </div>
        </div>
      </section>
      <Section>
        <div className="grid gap-10">
          {error && <EmptyState title="모임을 불러오지 못했습니다" description={error} />}

          <section className="grid gap-4">
            <div>
              <h2 className="text-xl font-normal">이번 달 모임</h2>
              <p className="mt-2 hidden text-sm leading-6 text-[var(--color-muted)] sm:block">
                매달 자동으로 만들어지는 정기 모임 2개와 이번 달에 열리는 소소모임만 보여줍니다.
              </p>
            </div>
            {loadingCurrent && <MeetingCardsSkeleton />}
            {!loadingCurrent && currentMeetings.length === 0 && <EmptyState title="이번 달 모임이 아직 없어요" description="정기 모임이 자동 생성되지 않았다면 잠시 후 새로고침해 주세요." />}

            <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
              {currentMeetings.map((meeting) => (
                <a className="group overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-warm-white)] shadow-[var(--shadow-soft)] transition hover:-translate-y-0.5" href={`/meetings/${meeting.id}`} key={meeting.id}>
                  <div className="h-32 bg-cover bg-center photo-muted sm:h-36" style={{ backgroundImage: `url(${meeting.coverImageUrl ?? "/images/korean-bookclub-discussion.jpg"})` }} />
                  <div className="p-4">
                    <div className="flex flex-wrap gap-2">
                      <Tag>{meetingTypeLabel(meeting.meetingType)}</Tag>
                      <Tag>{meetingStatusLabel(meeting.status)}</Tag>
                    </div>
                    <h2 className="mt-3 line-clamp-1 text-lg font-normal">{meeting.title}</h2>
                    {meeting.description && <p className="mt-1 hidden text-xs leading-5 text-[var(--color-muted)] sm:line-clamp-2">{meeting.description}</p>}
                    <dl className="mt-3 grid gap-1.5 text-xs text-[var(--color-charcoal)]">
                      <div className="grid grid-cols-[42px_minmax(0,1fr)] gap-2 sm:flex sm:justify-between sm:gap-4">
                        <dt>일시</dt>
                        <dd className="min-w-0 text-left sm:text-right">{formatDateTime(meeting.meetingAt)}</dd>
                      </div>
                      <div className="grid grid-cols-[42px_minmax(0,1fr)] gap-2 sm:flex sm:justify-between sm:gap-4">
                        <dt>지역</dt>
                        <dd className="min-w-0 text-left sm:text-right">{meeting.locationRegion}</dd>
                      </div>
                      <div className="grid grid-cols-[42px_minmax(0,1fr)] gap-2 sm:flex sm:justify-between sm:gap-4">
                        <dt>참석</dt>
                        <dd className="min-w-0 text-left sm:text-right">
                          {meeting.attendeeCount}
                          {meeting.capacity ? ` / ${meeting.capacity}` : ""}
                        </dd>
                      </div>
                    </dl>
                    <div className="mt-4">
                      <AttendeePreview images={meeting.attendeePreviewImageUrls} count={meeting.attendeeCount} />
                    </div>
                  </div>
                </a>
              ))}
            </div>
          </section>

          <section className="grid gap-4 border-t border-[var(--color-line)] pt-8">
            <div>
              <div>
                <h2 className="text-xl font-normal">지난 모임</h2>
                <p className="mt-2 hidden text-sm leading-6 text-[var(--color-muted)] sm:block">지난 모임은 제목, 일정, 지역, 참여자만 간략하게 보여줍니다.</p>
              </div>
            </div>

            {pastGroups.length === 0 && <EmptyState title="지난 모임을 아직 불러오지 않았어요" description="More를 누르면 지난달부터 한 달씩 조회합니다." />}
            <div className="grid gap-6">
              {pastGroups.map((group) => (
                <div className="grid gap-3" key={group.monthOffset}>
                  <h3 className="text-sm font-normal text-[var(--color-bronze)]">{group.label}</h3>
                  {group.meetings.length === 0 ? (
                    <p className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.72)] p-4 text-sm text-[var(--color-muted)]">이 달에는 공개된 지난 모임이 없습니다.</p>
                  ) : (
                    <div className="grid gap-2">
                      {group.meetings.map((meeting) => (
                        <a className="grid gap-3 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.78)] p-4 transition hover:-translate-y-0.5 hover:border-[rgba(47,90,67,0.28)] sm:grid-cols-[1fr_auto] sm:items-center" href={`/meetings/${meeting.id}`} key={meeting.id}>
                          <div className="min-w-0">
                            <div className="flex flex-wrap gap-2">
                              <Tag>{meetingTypeLabel(meeting.meetingType)}</Tag>
                              <Tag>{meetingStatusLabel(meeting.status)}</Tag>
                            </div>
                            <h4 className="mt-2 line-clamp-1 text-base font-normal">{meeting.title}</h4>
                            <p className="mt-1 text-sm text-[var(--color-muted)]">
                              {formatDateTime(meeting.meetingAt)} · {meeting.locationRegion}
                            </p>
                          </div>
                          <AttendeePreview images={meeting.attendeePreviewImageUrls} count={meeting.attendeeCount} />
                        </a>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
            {loadingPast && <PastMeetingSkeleton />}
            <div className="flex justify-center pt-2">
              <button
                aria-label="지난 모임 더 보기"
                className="archive-more-button"
                disabled={loadingPast}
                onClick={loadPastMonth}
                type="button"
              >
                {loadingPast ? "Wait" : "More"}
              </button>
            </div>
          </section>
        </div>
      </Section>
    </main>
  );
}

function meetingTypeLabel(type: MeetingSummary["meetingType"]) {
  return {
    REGULAR_READING: "독서기록 정기모임",
    REGULAR_ACTION: "실행목표 수다모임",
    SMALL: "소소모임",
  }[type];
}

function meetingStatusLabel(status: MeetingSummary["status"]) {
  return {
    SCHEDULED: "예정",
    HELD: "진행 완료",
    CANCELED: "취소",
    HIDDEN: "숨김",
    DELETED: "삭제",
  }[status];
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeStyle: "short",
    timeZone: "Asia/Seoul",
  }).format(new Date(value));
}

function monthLabel(monthOffset: number) {
  const date = new Date();
  date.setMonth(date.getMonth() - monthOffset, 1);
  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "long",
    timeZone: "Asia/Seoul",
  }).format(date);
}

function AttendeePreview({ images, count }: { images: Array<string | null>; count: number }) {
  const maxVisible = 4;
  const visibleAttendees = images.slice(0, maxVisible);

  return (
    <div className="flex items-center gap-2">
      <div className="flex -space-x-2">
        {visibleAttendees.map((image, index) => (
          image ? (
            <Image alt="" className="size-7 rounded-full border border-[var(--color-warm-white)] object-cover" height={28} key={`${image}-${index}`} src={image} unoptimized width={28} />
          ) : (
            <div className="size-7 rounded-full border border-[var(--color-warm-white)] bg-[var(--color-ivory)]" key={`placeholder-${index}`} />
          )
        ))}
        {count === 0 && <div className="size-7 rounded-full border border-[var(--color-line)] bg-[var(--color-ivory)]" />}
      </div>
      <span className="text-xs text-[var(--color-charcoal)]">{count}명</span>
    </div>
  );
}

function MeetingCardsSkeleton() {
  return (
    <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3" aria-label="이번 달 모임 로딩 중">
      {Array.from({ length: 3 }).map((_, index) => (
        <article className="overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-warm-white)] shadow-[var(--shadow-soft)]" key={index}>
          <SkeletonBlock className="h-32 rounded-none sm:h-36" />
          <div className="grid gap-3 p-4">
            <div className="flex gap-2">
              <SkeletonBlock className="h-7 w-24 rounded-full" />
              <SkeletonBlock className="h-7 w-16 rounded-full" />
            </div>
            <SkeletonBlock className="h-5 w-3/4" />
            <SkeletonBlock className="hidden h-4 w-full sm:block" />
            <div className="grid gap-2 pt-1">
              <SkeletonBlock className="h-4 w-full" />
              <SkeletonBlock className="h-4 w-5/6" />
              <SkeletonBlock className="h-4 w-2/3" />
            </div>
            <div className="flex items-center gap-2 pt-1">
              <div className="flex -space-x-2">
                {Array.from({ length: 3 }).map((__, avatarIndex) => (
                  <SkeletonBlock className="size-7 rounded-full" key={avatarIndex} />
                ))}
              </div>
              <SkeletonBlock className="h-4 w-10" />
            </div>
          </div>
        </article>
      ))}
    </div>
  );
}

function PastMeetingSkeleton() {
  return (
    <div className="grid gap-3" aria-label="지난 모임 로딩 중">
      <SkeletonBlock className="h-4 w-28" />
      {Array.from({ length: 2 }).map((_, index) => (
        <article className="grid gap-3 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.78)] p-4 sm:grid-cols-[1fr_auto] sm:items-center" key={index}>
          <div className="grid gap-2">
            <div className="flex gap-2">
              <SkeletonBlock className="h-7 w-24 rounded-full" />
              <SkeletonBlock className="h-7 w-16 rounded-full" />
            </div>
            <SkeletonBlock className="h-5 w-64 max-w-full" />
            <SkeletonBlock className="h-4 w-48 max-w-full" />
          </div>
          <div className="flex items-center gap-2">
            <div className="flex -space-x-2">
              {Array.from({ length: 3 }).map((__, avatarIndex) => (
                <SkeletonBlock className="size-7 rounded-full" key={avatarIndex} />
              ))}
            </div>
            <SkeletonBlock className="h-4 w-10" />
          </div>
        </article>
      ))}
    </div>
  );
}
