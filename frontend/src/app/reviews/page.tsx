"use client";

import { useEffect, useState } from "react";
import { Button, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiGet, type MeetingReviewSummary } from "@/lib/api";

export default function ReviewsPage() {
  const [reviews, setReviews] = useState<MeetingReviewSummary[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [visibleCount, setVisibleCount] = useState(4);

  const visibleReviews = reviews.slice(0, visibleCount);

  useEffect(() => {
    apiGet<MeetingReviewSummary[]>("/reviews?page=0&size=40")
      .then((result) => {
        if (!result.success) {
          setError(result.error?.message ?? "잠시 후 다시 시도해 주세요.");
          return;
        }
        setReviews(result.data);
      })
      .catch(() => setError("잠시 후 다시 시도해 주세요."))
      .finally(() => setLoading(false));
  }, []);

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <div className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
            <PageHeader eyebrow="Meeting Reviews" title="모임 후기" description="공개된 모임 후기와 사진을 살펴봅니다." />
            <Button href="/reviews/new">후기 작성</Button>
          </div>
          {loading && <EmptyState title="불러오는 중입니다." description="공개된 모임 후기를 확인하고 있습니다." />}
          {error && <EmptyState title="후기를 불러오지 못했습니다" description={error} />}
          {!loading && !error && reviews.length === 0 && <EmptyState title="아직 공개 후기가 없습니다" description="성장하는 사람들이 모임 후기를 남기면 이곳에 공개됩니다." />}
          <div className="grid gap-4 md:grid-cols-2">
            {visibleReviews.map((review) => (
              <a
                className="group grid overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-warm-white)] shadow-[var(--shadow-soft)] transition hover:-translate-y-0.5"
                href={`/reviews/${review.id}`}
                key={review.id}
              >
                <div className="grid gap-3 p-5">
                  <div className="flex flex-wrap gap-2">
                    <Tag>{review.meetingTitle}</Tag>
                  </div>
                  {review.representativeImageUrl && <img alt="" className="aspect-[4/3] w-full rounded-[var(--radius-card)] object-cover photo-muted transition group-hover:scale-[1.01]" src={review.representativeImageUrl} />}
                  <h2 className="font-display text-xl font-normal leading-snug">{review.title}</h2>
                  <p className="line-clamp-3 text-sm leading-7 text-[var(--color-muted)]">{review.contentSummary}</p>
                  <p className="pt-2 text-sm text-[var(--color-charcoal)]">{review.memberDisplayName} · {formatDate(review.createdAt)}</p>
                </div>
              </a>
            ))}
          </div>
          {visibleCount < reviews.length && (
            <button
              aria-label="모임 후기 더 보기"
              className="archive-more-button mx-auto mt-2"
              onClick={() => setVisibleCount((count) => count + 4)}
              type="button"
            >
              More
            </button>
          )}
        </div>
      </Section>
    </main>
  );
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", { dateStyle: "medium", timeZone: "Asia/Seoul" }).format(new Date(value));
}
