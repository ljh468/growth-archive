"use client";

import { FormEvent, useEffect, useState } from "react";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiDelete, apiGet, apiPut, type MeetingReviewDetail } from "@/lib/api";

export function ReviewDetailClient({ reviewId }: { reviewId: string }) {
  const [review, setReview] = useState<MeetingReviewDetail | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ title: "", content: "" });

  useEffect(() => {
    async function load() {
      const result = await apiGet<MeetingReviewDetail>(`/reviews/${reviewId}`);
      if (!result.success) {
        setMessage(result.error?.message ?? "후기를 불러오지 못했습니다.");
        return;
      }
      setReview(result.data);
      setForm({ title: result.data.title, content: result.data.content });
    }
    load();
  }, [reviewId]);

  async function save(event: FormEvent) {
    event.preventDefault();
    if (!review) {
      return;
    }
    const result = await apiPut<MeetingReviewDetail>(`/reviews/${review.id}`, {
      meetingId: review.meetingId,
      title: form.title,
      content: form.content,
      imageIds: review.images.map((image) => image.imageId),
    });
    if (!result.success) {
      setMessage(result.error?.message ?? "후기를 수정하지 못했습니다.");
      return;
    }
    setReview(result.data);
    setEditing(false);
    setMessage("후기가 수정되었습니다.");
  }

  async function remove() {
    if (!review) {
      return;
    }
    const result = await apiDelete<void>(`/reviews/${review.id}`);
    if (!result.success) {
      setMessage(result.error?.message ?? "후기를 삭제하지 못했습니다.");
      return;
    }
    window.location.href = "/reviews";
  }

  if (!review) {
    return (
      <main>
        <Section>
          <EmptyState title="확인 중입니다" description={message ?? "후기를 불러오고 있습니다."} />
        </Section>
      </main>
    );
  }

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <PageHeader eyebrow="Meeting Review" title={review.title} description={`${review.meetingTitle} · ${review.memberDisplayName}`} />
          {message && <EmptyState title="상태" description={message} />}
          <Card>
            <div className="flex flex-wrap gap-2">
              <Tag>{review.status}</Tag>
              <Tag>{formatDate(review.createdAt)}</Tag>
            </div>
            {!editing && <p className="mt-6 whitespace-pre-wrap text-sm leading-7 text-[var(--color-charcoal)]">{review.content}</p>}
            {editing && (
              <form className="mt-6 grid gap-3" onSubmit={save}>
                <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} value={form.title} />
                <textarea className="min-h-56 border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3" onChange={(event) => setForm((current) => ({ ...current, content: event.target.value }))} value={form.content} />
                <div className="flex flex-wrap gap-3">
                  <Button type="submit">수정 저장</Button>
                  <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-semibold" onClick={() => setEditing(false)} type="button">취소</button>
                </div>
              </form>
            )}
            {review.canEdit && !editing && (
              <div className="mt-6 flex flex-wrap gap-3">
                <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-semibold" onClick={() => setEditing(true)} type="button">수정</button>
                <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-semibold" onClick={remove} type="button">삭제</button>
              </div>
            )}
          </Card>
          <Card>
            <h2 className="text-lg font-semibold">사진</h2>
            {review.images.length === 0 && <p className="mt-3 text-sm text-[var(--color-charcoal)]">등록된 사진이 없습니다.</p>}
            <div className="mt-4 grid gap-3 sm:grid-cols-2">
              {review.images.map((image) => (
                image.imageUrl ? (
                  <img alt="" className="aspect-[4/3] w-full rounded-[var(--radius-card)] object-cover" key={image.imageId} src={image.imageUrl} />
                ) : (
                  <div className="grid aspect-[4/3] place-items-center rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-ivory)] text-sm text-[var(--color-charcoal)]" key={image.imageId}>이미지 저장소 설정 필요</div>
                )
              ))}
            </div>
          </Card>
        </div>
      </Section>
    </main>
  );
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", { dateStyle: "medium", timeZone: "Asia/Seoul" }).format(new Date(value));
}
