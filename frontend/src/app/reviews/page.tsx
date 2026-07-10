"use client";

import Image from "next/image";
import { useEffect, useState } from "react";
import { Button, EmptyState, MoreButton, PageHeader, Section, SkeletonBlock, Tag } from "@/components/ui/primitives";
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
          {loading && <ReviewsSkeleton />}
          {error && <EmptyState title="후기를 불러오지 못했습니다" description={error} />}
          {!loading && !error && reviews.length === 0 && <EmptyState title="아직 공개 후기가 없습니다" description="성장하는 사람들이 모임 후기를 남기면 이곳에 공개됩니다." />}
          <div className="grid gap-4 md:grid-cols-2">
            {visibleReviews.map((review) => (
              <a
                className="group grid overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-warm-white)] shadow-[var(--shadow-soft)] transition hover:-translate-y-0.5"
                href={`/reviews/${review.id}`}
                key={review.id}
              >
                <div className="grid grid-cols-[104px_minmax(0,1fr)] gap-3 p-3 sm:grid-cols-1 sm:gap-3 sm:p-5">
                  <Image
                    alt=""
                    className="h-32 w-full rounded-[var(--radius-card)] object-cover photo-muted transition group-hover:scale-[1.01] sm:aspect-[16/9] sm:h-auto"
                    height={216}
                    src={reviewPreviewImage(review)}
                    unoptimized
                    width={384}
                  />
                  <div className="min-w-0 max-w-[calc(100vw-168px)] sm:max-w-none">
                    <div className="flex flex-wrap gap-2">
                      <Tag>{review.meetingTitle}</Tag>
                    </div>
                    <h2 className="mt-2 truncate font-display text-[15px] font-normal leading-snug sm:line-clamp-3 sm:whitespace-normal sm:text-lg">{review.title}</h2>
                    <p className="hidden font-hand text-[1.05rem] leading-6 text-[var(--color-charcoal)] sm:mt-2 sm:line-clamp-2">{review.contentSummary}</p>
                    <p className="mt-2 text-xs leading-5 text-[var(--color-charcoal)] sm:text-sm">{review.memberDisplayName} · {formatDate(review.createdAt)}</p>
                  </div>
                </div>
              </a>
            ))}
          </div>
          {visibleCount < reviews.length && (
            <MoreButton
              aria-label="모임 후기 더 보기"
              className="mx-auto mt-2"
              onClick={() => setVisibleCount((count) => count + 4)}
            >
              More
            </MoreButton>
          )}
        </div>
      </Section>
    </main>
  );
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", { dateStyle: "medium", timeZone: "Asia/Seoul" }).format(new Date(value));
}

function reviewPreviewImage(review: MeetingReviewSummary) {
  const imageUrl = review.representativeImageUrl;
  if (!imageUrl || imageUrl.includes("/book-shelf") || imageUrl.includes("/reading-books") || imageUrl.includes("/member-books")) {
    const fallbacks = ["/images/korean-bookclub-discussion.jpg", "/images/meeting-table.jpg", "/images/hero-bookclub-discussion.png"];
    return fallbacks[review.id % fallbacks.length];
  }
  return imageUrl;
}

function ReviewsSkeleton() {
  return (
    <div className="grid gap-4 md:grid-cols-2" aria-label="모임 후기 로딩 중">
      {Array.from({ length: 4 }).map((_, index) => (
        <article className="grid overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-warm-white)] shadow-[var(--shadow-soft)]" key={index}>
          <div className="grid grid-cols-[104px_minmax(0,1fr)] gap-3 p-3 sm:grid-cols-1 sm:p-5">
            <SkeletonBlock className="h-32 w-full sm:aspect-[16/9] sm:h-auto" />
            <div className="grid min-w-0 content-start gap-2">
              <SkeletonBlock className="h-7 w-32 rounded-full" />
              <SkeletonBlock className="h-5 w-4/5" />
              <SkeletonBlock className="hidden h-4 w-full sm:block" />
              <SkeletonBlock className="hidden h-4 w-2/3 sm:block" />
              <SkeletonBlock className="h-4 w-32" />
            </div>
          </div>
        </article>
      ))}
    </div>
  );
}
