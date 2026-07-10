"use client";

import Image from "next/image";
import { useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { MobileBackButton } from "@/components/MobileBackButton";
import { Button, EmptyState, MoreButton, PageHeader, Section, SkeletonBlock, Tag } from "@/components/ui/primitives";
import { apiGet, type MeetingReviewSummary } from "@/lib/api";

export default function MyReviewsPage() {
  return (
    <AuthGate required="MEMBER">
      {() => <MyReviewsContent />}
    </AuthGate>
  );
}

function MyReviewsContent() {
  const [reviews, setReviews] = useState<MeetingReviewSummary[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    loadReviews(0)
      .then(({ hasMore, items }) => {
        setReviews(items);
        setHasMore(hasMore);
        setPage(0);
      })
      .catch(() => setMessage("내 모임 후기를 불러오지 못했습니다."))
      .finally(() => setLoading(false));
  }, []);

  async function more() {
    if (loadingMore) {
      return;
    }
    setLoadingMore(true);
    const nextPage = page + 1;
    try {
      const result = await loadReviews(nextPage);
      setReviews((current) => [...current, ...result.items]);
      setHasMore(result.hasMore);
      setPage(nextPage);
    } catch {
      setMessage("추가 후기를 불러오지 못했습니다.");
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
            <PageHeader eyebrow="My Reviews" title="내 모임 후기" description="내가 남긴 모임 후기를 최신순으로 확인합니다." />
            <Button href="/reviews/new">후기 작성</Button>
          </div>
          {message && <p className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.78)] px-4 py-3 text-sm leading-6 text-[var(--color-charcoal)]">{message}</p>}
          {loading ? (
            <ReviewSkeleton />
          ) : reviews.length === 0 ? (
            <EmptyState title="아직 남긴 후기가 없습니다" description="모임 후기를 남기면 이곳에 차곡차곡 쌓입니다." />
          ) : (
            <div className="grid gap-4">
              {reviews.map((review) => (
                <a className="grid grid-cols-[84px_1fr] gap-4 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3 shadow-[var(--shadow-soft)] transition hover:border-[rgba(31,77,58,0.28)] sm:grid-cols-[120px_1fr] sm:p-4" href={`/reviews/${review.id}`} key={review.id}>
                  <div className="aspect-[4/3] overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-line)]">
                    <Image alt="" className="h-full w-full object-cover photo-muted" height={90} src={review.representativeImageUrl || "/images/meeting-table.jpg"} unoptimized width={120} />
                  </div>
                  <div className="min-w-0">
                    <div className="flex flex-wrap gap-2">
                      <Tag>{review.meetingTitle}</Tag>
                      {review.status !== "ACTIVE" && <Tag>{review.status === "HIDDEN" ? "숨김" : "삭제됨"}</Tag>}
                    </div>
                    <p className="mt-3 line-clamp-2 font-display text-base font-normal leading-snug text-[var(--color-ink)] sm:text-lg">{review.title}</p>
                    <p className="mt-2 line-clamp-2 text-xs leading-5 text-[var(--color-charcoal)] sm:text-sm sm:leading-6">{review.contentSummary}</p>
                    <p className="mt-3 text-xs text-[var(--color-muted)]">{formatDate(review.createdAt)}</p>
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

async function loadReviews(page: number) {
  const size = 10;
  const result = await apiGet<MeetingReviewSummary[]>(`/reviews/mine?page=${page}&size=${size}`);
  if (!result.success) {
    throw new Error(result.error?.message ?? "후기를 불러오지 못했습니다.");
  }
  return { items: result.data, hasMore: result.data.length === size };
}

function ReviewSkeleton() {
  return (
    <div className="grid gap-4">
      {Array.from({ length: 3 }).map((_, index) => (
        <div className="grid grid-cols-[84px_1fr] gap-4 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3" key={index}>
          <SkeletonBlock className="aspect-[4/3]" />
          <div className="grid gap-3">
            <SkeletonBlock className="h-7 w-28 rounded-full" />
            <SkeletonBlock className="h-5 w-3/4" />
            <SkeletonBlock className="h-4 w-full" />
          </div>
        </div>
      ))}
    </div>
  );
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", { dateStyle: "medium", timeZone: "Asia/Seoul" }).format(new Date(value));
}
