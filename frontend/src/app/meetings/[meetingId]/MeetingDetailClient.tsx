"use client";

import Image from "next/image";
import { useEffect, useState } from "react";
import { MobileBackButton } from "@/components/MobileBackButton";
import { Button, Card, EmptyState, PageHeader, RecordButton, Section, SkeletonBlock, Tag } from "@/components/ui/primitives";
import { apiDelete, apiGet, apiGetCurrentUser, apiPost, type CurrentUser, type MeetingDetail } from "@/lib/api";

export function MeetingDetailClient({ meetingId }: { meetingId: string }) {
  const [meeting, setMeeting] = useState<MeetingDetail | null>(null);
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    async function load() {
      const [meetingResult, userResult] = await Promise.all([
        apiGet<MeetingDetail>(`/meetings/${meetingId}`),
        apiGetCurrentUser(),
      ]);
      if (!meetingResult.success) {
        setMessage(meetingResult.error?.message ?? "모임을 불러오지 못했습니다.");
        return;
      }
      setMeeting(meetingResult.data);
      if (userResult.success) {
        setUser(userResult.data);
      }
    }
    load();
  }, [meetingId]);

  async function join() {
    const result = await apiPost<MeetingDetail>(`/meetings/${meetingId}/join`);
    if (!result.success) {
      setMessage(result.error?.message ?? "참석 처리에 실패했습니다.");
      return;
    }
    setMeeting(result.data);
    setMessage("참석 신청이 완료되었습니다.");
  }

  async function cancel() {
    const result = await apiDelete<MeetingDetail>(`/meetings/${meetingId}/join`);
    if (!result.success) {
      setMessage(result.error?.message ?? "참석 취소에 실패했습니다.");
      return;
    }
    setMeeting(result.data);
    setMessage("참석이 취소되었습니다.");
  }

  if (!meeting) {
    return (
      <main>
        <Section>
          {message ? <EmptyState title="확인 중입니다" description={message} /> : <MeetingDetailSkeleton />}
        </Section>
      </main>
    );
  }

  const memberView = user?.accessLevel === "MEMBER" || user?.accessLevel === "ADMIN";
  const canManageAttendance = memberView && meeting.status === "SCHEDULED" && isFutureMeeting(meeting.meetingAt);

  return (
    <main>
      <Section>
        <div className="grid gap-5 sm:gap-8">
          <MobileBackButton fallbackHref="/meetings" />
          <div className="grid gap-3">
            <PageHeader eyebrow="Meeting Detail" title={meeting.title} />
            {meeting.description && (
              <p className="max-w-2xl whitespace-pre-wrap break-words text-sm leading-7 text-[var(--color-muted)] sm:text-base sm:leading-8">
                {meeting.description}
              </p>
            )}
          </div>

          <Card>
            <div className="flex flex-wrap gap-2">
              <Tag>{meetingTypeLabel(meeting.meetingType)}</Tag>
              <Tag>{meetingStatusLabel(meeting.status)}</Tag>
              {meeting.feeAmount > 0 && <Tag>{meeting.feeAmount.toLocaleString("ko-KR")}원</Tag>}
            </div>
            <dl className="mt-4 grid gap-2 text-sm text-[var(--color-charcoal)] sm:mt-6 sm:grid-cols-2 sm:gap-3">
              <Field label="일시" value={formatDateTime(meeting.meetingAt)} />
              <Field label="지역" value={meeting.locationRegion} />
              <Field label="참석" value={`${meeting.attendeeCount}${meeting.capacity ? ` / ${meeting.capacity}` : ""}명`} />
              <Field label="정확한 장소" value={memberView ? meeting.exactLocation ?? "추후 공지" : "성장하는 사람들에게만 공개"} />
            </dl>
            {message && <p className="mt-4 text-sm leading-6 text-[var(--color-muted)]">{message}</p>}
            <div className="mt-4 flex flex-wrap gap-2 sm:mt-6 sm:gap-3">
              {canManageAttendance && (
                meeting.attendedByMe ? (
                  <RecordButton onClick={cancel} variant="primary">참석 취소</RecordButton>
                ) : (
                  <RecordButton onClick={join} variant="primary">참석하기</RecordButton>
                )
              )}
              {!memberView && <Button href="/login" variant="secondary">로그인하고 참석하기</Button>}
              {meeting.meetingType === "SMALL" && meeting.canEdit && <RecordButton href={`/meetings/${meeting.id}/edit`}>소소모임 수정</RecordButton>}
              {memberView && <RecordButton href="/reviews/new" variant="primary">후기 작성</RecordButton>}
            </div>
          </Card>

          <Card>
            <h2 className="text-base font-normal sm:text-lg">참석자</h2>
            {!memberView && (
              <div className="mt-3 grid gap-2 sm:mt-4 sm:flex sm:items-center sm:gap-3">
                <AttendeePreview count={meeting.attendeeCount} images={meeting.attendeePreviewImageUrls} />
                <p className="min-w-0 text-sm leading-6 text-[var(--color-charcoal)]">참석자 이름과 프로필은 성장하는 사람들에게만 공개됩니다.</p>
              </div>
            )}
            {memberView && (
              <div className="mt-3 grid gap-2 sm:mt-4 sm:grid-cols-2 sm:gap-3">
                {meeting.attendees.length === 0 && <p className="text-sm text-[var(--color-charcoal)]">아직 참석자가 없습니다.</p>}
                {meeting.attendees.map((attendee) => (
                  <a className="flex items-center gap-3 border border-[var(--color-line)] p-2.5 sm:p-3" href={attendee.profileHref} key={attendee.memberId}>
                    {attendee.profileImageUrl ? (
                      <Image alt="" className="size-10 rounded-full object-cover" height={40} src={attendee.profileImageUrl} unoptimized width={40} />
                    ) : (
                      <div className="flex size-10 items-center justify-center rounded-full bg-[var(--color-deep-green)] text-sm font-normal text-[var(--color-warm-white)]">
                        {attendee.displayName.slice(0, 1)}
                      </div>
                    )}
                    <span className="text-sm font-normal">{attendee.displayName}</span>
                  </a>
                ))}
              </div>
            )}
          </Card>
        </div>
      </Section>
    </main>
  );
}

function Field({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-4 border-b border-[var(--color-line)] pb-2">
      <dt className="text-xs text-[var(--color-muted)] sm:text-sm">{label}</dt>
      <dd className="text-right text-sm font-normal text-[var(--color-ink)]">{value}</dd>
    </div>
  );
}

function AttendeePreview({ images, count }: { images: Array<string | null>; count: number }) {
  const maxVisible = 5;
  const visibleAttendees = images.slice(0, maxVisible);

  return (
    <div className="flex shrink-0 -space-x-2">
      {visibleAttendees.map((image, index) => (
        image ? (
          <Image alt="" className="size-8 rounded-full border border-[var(--color-warm-white)] object-cover" height={32} key={`${image}-${index}`} src={image} unoptimized width={32} />
        ) : (
          <div className="size-8 rounded-full border border-[var(--color-warm-white)] bg-[var(--color-ivory)]" key={`placeholder-${index}`} />
        )
      ))}
      {count === 0 && <div className="size-8 rounded-full border border-[var(--color-line)] bg-[var(--color-ivory)]" />}
    </div>
  );
}

function meetingTypeLabel(type: MeetingDetail["meetingType"]) {
  return {
    REGULAR_READING: "독서기록 정기모임",
    REGULAR_ACTION: "실행목표 수다모임",
    SMALL: "소소모임",
  }[type];
}

function meetingStatusLabel(status: MeetingDetail["status"]) {
  return {
    SCHEDULED: "예정",
    HELD: "진행 완료",
    CANCELED: "취소",
    HIDDEN: "숨김",
    DELETED: "삭제",
  }[status];
}

function isFutureMeeting(value: string) {
  return new Date(value).getTime() > Date.now();
}

function MeetingDetailSkeleton() {
  return (
    <div className="grid gap-5 sm:gap-8" aria-label="모임 상세 로딩 중">
      <div className="grid max-w-3xl gap-3">
        <SkeletonBlock className="h-8 w-36" />
        <SkeletonBlock className="h-8 w-full max-w-xl sm:h-10" />
        <SkeletonBlock className="hidden h-4 w-full max-w-2xl sm:block" />
      </div>
      <div className="rounded-[var(--radius-card)] border border-[rgba(233,225,214,0.8)] bg-[rgba(255,254,250,0.94)] p-4 shadow-[var(--shadow-soft)] sm:p-6">
        <div className="flex gap-2">
          <SkeletonBlock className="h-7 w-28 rounded-full" />
          <SkeletonBlock className="h-7 w-16 rounded-full" />
        </div>
        <div className="mt-4 grid gap-2 sm:mt-6 sm:grid-cols-2 sm:gap-3">
          {Array.from({ length: 4 }).map((_, index) => (
            <SkeletonBlock className="h-8" key={index} />
          ))}
        </div>
        <div className="mt-4 flex gap-2 sm:mt-6">
          <SkeletonBlock className="h-11 w-28" />
          <SkeletonBlock className="h-11 w-24" />
        </div>
      </div>
      <div className="rounded-[var(--radius-card)] border border-[rgba(233,225,214,0.8)] bg-[rgba(255,254,250,0.94)] p-4 shadow-[var(--shadow-soft)] sm:p-6">
        <SkeletonBlock className="h-6 w-20" />
        <div className="mt-4 grid gap-2 sm:grid-cols-2 sm:gap-3">
          {Array.from({ length: 4 }).map((_, index) => (
            <div className="flex items-center gap-3 border border-[var(--color-line)] p-2.5 sm:p-3" key={index}>
              <SkeletonBlock className="size-10 rounded-full" />
              <SkeletonBlock className="h-4 w-24" />
            </div>
          ))}
        </div>
      </div>
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
