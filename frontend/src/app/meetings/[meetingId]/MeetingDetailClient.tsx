"use client";

import { useEffect, useState } from "react";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiDelete, apiGet, apiPost, type CurrentUser, type MeetingDetail } from "@/lib/api";

export function MeetingDetailClient({ meetingId }: { meetingId: string }) {
  const [meeting, setMeeting] = useState<MeetingDetail | null>(null);
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    async function load() {
      const [meetingResult, userResult] = await Promise.all([
        apiGet<MeetingDetail>(`/meetings/${meetingId}`),
        apiGet<CurrentUser>("/auth/me"),
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
          <EmptyState title="확인 중입니다" description={message ?? "모임 정보를 불러오고 있습니다."} />
        </Section>
      </main>
    );
  }

  const memberView = user?.accessLevel === "MEMBER" || user?.accessLevel === "ADMIN";

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <PageHeader eyebrow="Meeting Detail" title={meeting.title} description={meeting.description ?? undefined} />
          {message && <EmptyState title="상태" description={message} />}

          <Card>
            <div className="flex flex-wrap gap-2">
              <Tag>{meetingTypeLabel(meeting.meetingType)}</Tag>
              <Tag>{meeting.status}</Tag>
              {meeting.feeAmount > 0 && <Tag>{meeting.feeAmount.toLocaleString("ko-KR")}원</Tag>}
            </div>
            <dl className="mt-6 grid gap-3 text-sm text-[var(--color-charcoal)] sm:grid-cols-2">
              <Field label="일시" value={formatDateTime(meeting.meetingAt)} />
              <Field label="지역" value={meeting.locationRegion} />
              <Field label="참석" value={`${meeting.attendeeCount}${meeting.capacity ? ` / ${meeting.capacity}` : ""}명`} />
              <Field label="정확한 장소" value={memberView ? meeting.exactLocation ?? "추후 공지" : "멤버에게만 공개"} />
            </dl>
            <div className="mt-6 flex flex-wrap gap-3">
              {memberView && meeting.status === "SCHEDULED" && (
                meeting.attendedByMe ? (
                  <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-semibold" onClick={cancel} type="button">
                    참석 취소
                  </button>
                ) : (
                  <button className="inline-flex min-h-11 items-center justify-center border bg-[var(--color-ink)] px-4 py-2 text-sm font-semibold text-[var(--color-warm-white)] transition" onClick={join} type="button">
                    참석하기
                  </button>
                )
              )}
              {!memberView && <Button href="/login" variant="secondary">로그인하고 참석하기</Button>}
              {meeting.canEdit && <Button href={`/meetings/${meeting.id}/edit`} variant="secondary">소소모임 수정</Button>}
              {memberView && <Button href="/reviews/new" variant="ghost">후기 작성</Button>}
            </div>
          </Card>

          <Card>
            <h2 className="text-lg font-semibold">참석자</h2>
            {!memberView && (
              <div className="mt-4 flex items-center gap-3">
                <AttendeePreview images={meeting.attendeePreviewImageUrls} />
                <p className="text-sm text-[var(--color-charcoal)]">참석자 이름과 프로필은 멤버에게만 공개됩니다.</p>
              </div>
            )}
            {memberView && (
              <div className="mt-4 grid gap-3 sm:grid-cols-2">
                {meeting.attendees.length === 0 && <p className="text-sm text-[var(--color-charcoal)]">아직 참석자가 없습니다.</p>}
                {meeting.attendees.map((attendee) => (
                  <a className="flex items-center gap-3 border border-[var(--color-line)] p-3" href={attendee.profileHref} key={attendee.memberId}>
                    {attendee.profileImageUrl ? (
                      <img alt="" className="size-10 rounded-full object-cover" src={attendee.profileImageUrl} />
                    ) : (
                      <div className="flex size-10 items-center justify-center rounded-full bg-[var(--color-deep-green)] text-sm font-semibold text-[var(--color-warm-white)]">
                        {attendee.displayName.slice(0, 1)}
                      </div>
                    )}
                    <span className="text-sm font-semibold">{attendee.displayName}</span>
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
      <dt>{label}</dt>
      <dd className="text-right font-medium text-[var(--color-ink)]">{value}</dd>
    </div>
  );
}

function AttendeePreview({ images }: { images: string[] }) {
  return (
    <div className="flex -space-x-2">
      {images.slice(0, 5).map((image, index) => (
        <img alt="" className="size-8 rounded-full border border-[var(--color-warm-white)] object-cover" key={`${image}-${index}`} src={image} />
      ))}
      {images.length === 0 && <div className="size-8 rounded-full border border-[var(--color-line)] bg-[var(--color-ivory)]" />}
    </div>
  );
}

function meetingTypeLabel(type: MeetingDetail["meetingType"]) {
  return {
    REGULAR_READING: "독서기록 정기모임",
    REGULAR_ACTION: "실행계획 정기모임",
    SMALL: "소소모임",
  }[type];
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeStyle: "short",
    timeZone: "Asia/Seoul",
  }).format(new Date(value));
}
