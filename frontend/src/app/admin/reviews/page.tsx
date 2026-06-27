"use client";

import { useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiDelete, apiGet, apiPost, type MeetingReviewSummary } from "@/lib/api";

export default function AdminReviewsPage() {
  return (
    <AuthGate required="ADMIN">
      {() => <AdminReviewsContent />}
    </AuthGate>
  );
}

function AdminReviewsContent() {
  const [reviews, setReviews] = useState<MeetingReviewSummary[]>([]);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    const result = await apiGet<MeetingReviewSummary[]>("/admin/reviews?page=0&size=100");
    if (!result.success) {
      setMessage(result.error?.message ?? "후기 목록을 불러오지 못했습니다.");
      return;
    }
    setReviews(result.data);
  }

  async function hide(reviewId: number) {
    const result = await apiPost<void>(`/admin/reviews/${reviewId}/hide`);
    setMessage(result.success ? "후기를 숨김 처리했습니다." : result.error?.message ?? "숨김 처리에 실패했습니다.");
    load();
  }

  async function restore(reviewId: number) {
    const result = await apiPost<void>(`/admin/reviews/${reviewId}/restore`);
    setMessage(result.success ? "후기를 복구했습니다." : result.error?.message ?? "복구에 실패했습니다.");
    load();
  }

  async function remove(reviewId: number) {
    const result = await apiDelete<void>(`/admin/reviews/${reviewId}`);
    setMessage(result.success ? "후기를 삭제했습니다." : result.error?.message ?? "삭제에 실패했습니다.");
    load();
  }

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <PageHeader eyebrow="Admin" title="모임 후기 관리" description="후기 본문은 직접 수정하지 않고 숨김/복구/삭제만 수행합니다." />
          {message && <EmptyState title="상태" description={message} />}
          <div className="grid gap-3">
            {reviews.map((review) => (
              <Card key={review.id}>
                <div className="flex flex-wrap gap-2">
                  <Tag>{review.status}</Tag>
                  <Tag>{review.meetingTitle}</Tag>
                </div>
                <h2 className="mt-4 text-lg font-normal">{review.title}</h2>
                <p className="mt-2 text-sm text-[var(--color-charcoal)]">{review.memberDisplayName} · {formatDate(review.createdAt)}</p>
                <div className="mt-5 flex flex-wrap gap-3">
                  <Button href={`/reviews/${review.id}`} variant="secondary">상세 보기</Button>
                  {review.status === "HIDDEN" ? (
                    <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-normal" onClick={() => restore(review.id)} type="button">복구</button>
                  ) : (
                    <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-normal" onClick={() => hide(review.id)} type="button">숨김</button>
                  )}
                  <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-normal" onClick={() => remove(review.id)} type="button">삭제</button>
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
