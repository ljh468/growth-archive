import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiGet, type MeetingReviewSummary } from "@/lib/api";

export default async function ReviewsPage() {
  const result = await apiGet<MeetingReviewSummary[]>("/reviews?page=0&size=30");
  const reviews = result.success ? result.data : [];

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <div className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
            <PageHeader eyebrow="Meeting Reviews" title="모임 후기" description="공개된 모임 후기와 사진을 살펴봅니다." />
            <Button href="/reviews/new">후기 작성</Button>
          </div>
          {!result.success && <EmptyState title="후기를 불러오지 못했습니다" description={result.error?.message ?? "잠시 후 다시 시도해 주세요."} />}
          {result.success && reviews.length === 0 && <EmptyState title="아직 공개 후기가 없습니다" description="멤버가 모임 후기를 남기면 이곳에 공개됩니다." />}
          <div className="grid gap-4 md:grid-cols-2">
            {reviews.map((review) => (
              <Card key={review.id}>
                <div className="flex flex-wrap gap-2">
                  <Tag>{review.meetingTitle}</Tag>
                  <Tag>{review.status}</Tag>
                </div>
                {review.representativeImageUrl && <img alt="" className="mt-4 aspect-[4/3] w-full rounded-[var(--radius-card)] object-cover" src={review.representativeImageUrl} />}
                <h2 className="mt-4 text-xl font-semibold">{review.title}</h2>
                <p className="mt-2 text-sm leading-6 text-[var(--color-charcoal)]">{review.contentSummary}</p>
                <div className="mt-5 flex items-center justify-between gap-4">
                  <p className="text-sm text-[var(--color-charcoal)]">{review.memberDisplayName} · {formatDate(review.createdAt)}</p>
                  <Button href={`/reviews/${review.id}`} variant="secondary">상세 보기</Button>
                </div>
              </Card>
            ))}
          </div>
        </div>
      </Section>
    </main>
  );
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", { dateStyle: "medium", timeZone: "Asia/Seoul" }).format(new Date(value));
}
