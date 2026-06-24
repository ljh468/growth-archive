import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiGet, type MeetingSummary } from "@/lib/api";

export default async function MeetingsPage() {
  const result = await apiGet<MeetingSummary[]>("/meetings?page=0&size=30");
  const meetings = result.success ? result.data : [];

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <div className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
            <PageHeader
              eyebrow="Meetings"
              title="모임"
              description="정기 모임과 멤버가 만든 소소모임을 확인합니다. 공개 화면에서는 지역 수준 장소와 참석자 수만 보여줍니다."
            />
            <Button href="/meetings/new">소소모임 만들기</Button>
          </div>

          {!result.success && <EmptyState title="모임을 불러오지 못했습니다" description={result.error?.message ?? "잠시 후 다시 시도해 주세요."} />}
          {result.success && meetings.length === 0 && <EmptyState title="아직 예정된 모임이 없어요" description="멤버라면 직접 소소모임을 만들 수 있습니다." />}

          <div className="grid gap-4 md:grid-cols-2">
            {meetings.map((meeting) => (
              <Card key={meeting.id}>
                <div className="flex flex-wrap gap-2">
                  <Tag>{meetingTypeLabel(meeting.meetingType)}</Tag>
                  <Tag>{meeting.status}</Tag>
                </div>
                <h2 className="mt-4 text-xl font-semibold">{meeting.title}</h2>
                {meeting.description && <p className="mt-2 line-clamp-2 text-sm leading-6 text-[var(--color-charcoal)]">{meeting.description}</p>}
                <dl className="mt-5 grid gap-2 text-sm text-[var(--color-charcoal)]">
                  <div className="flex justify-between gap-4">
                    <dt>일시</dt>
                    <dd>{formatDateTime(meeting.meetingAt)}</dd>
                  </div>
                  <div className="flex justify-between gap-4">
                    <dt>지역</dt>
                    <dd>{meeting.locationRegion}</dd>
                  </div>
                  <div className="flex justify-between gap-4">
                    <dt>참석</dt>
                    <dd>
                      {meeting.attendeeCount}
                      {meeting.capacity ? ` / ${meeting.capacity}` : ""}
                    </dd>
                  </div>
                </dl>
                <div className="mt-5 flex items-center justify-between gap-4">
                  <AttendeePreview images={meeting.attendeePreviewImageUrls} count={meeting.attendeeCount} />
                  <Button href={`/meetings/${meeting.id}`} variant="secondary">
                    상세 보기
                  </Button>
                </div>
              </Card>
            ))}
          </div>
        </div>
      </Section>
    </main>
  );
}

function meetingTypeLabel(type: MeetingSummary["meetingType"]) {
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

function AttendeePreview({ images, count }: { images: string[]; count: number }) {
  return (
    <div className="flex items-center gap-2">
      <div className="flex -space-x-2">
        {images.slice(0, 4).map((image, index) => (
          <img alt="" className="size-7 rounded-full border border-[var(--color-warm-white)] object-cover" key={`${image}-${index}`} src={image} />
        ))}
        {images.length === 0 && <div className="size-7 rounded-full border border-[var(--color-line)] bg-[var(--color-ivory)]" />}
      </div>
      <span className="text-xs text-[var(--color-charcoal)]">{count}명</span>
    </div>
  );
}
