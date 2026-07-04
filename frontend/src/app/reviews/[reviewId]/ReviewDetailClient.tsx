"use client";

import Image from "next/image";
import { FormEvent, useEffect, useRef, useState } from "react";
import { Button, Card, ConfirmDialog, EmptyState, Section, SkeletonBlock, Tag } from "@/components/ui/primitives";
import { apiDelete, apiGet, apiPut, type MeetingReviewDetail } from "@/lib/api";

export function ReviewDetailClient({ reviewId }: { reviewId: string }) {
  const [review, setReview] = useState<MeetingReviewDetail | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ title: "", content: "" });
  const [imageIndex, setImageIndex] = useState(0);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const dragStartX = useRef<number | null>(null);

  useEffect(() => {
    async function load() {
      const result = await apiGet<MeetingReviewDetail>(`/reviews/${reviewId}`);
      if (!result.success) {
        setMessage(result.error?.message ?? "후기를 불러오지 못했습니다.");
        return;
      }
      setReview(result.data);
      setForm({ title: result.data.title, content: result.data.content });
      setImageIndex(0);
    }
    load();
  }, [reviewId]);

  function moveImage(direction: -1 | 1) {
    if (!review?.images.length) {
      return;
    }
    setImageIndex((current) => (current + direction + review.images.length) % review.images.length);
  }

  function finishImageDrag(clientX: number) {
    if (dragStartX.current === null) {
      return;
    }
    const delta = clientX - dragStartX.current;
    dragStartX.current = null;
    if (Math.abs(delta) < 36) {
      return;
    }
    moveImage(delta < 0 ? 1 : -1);
  }

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
          {message ? <EmptyState title="확인 중입니다" description={message} /> : <ReviewDetailSkeleton />}
        </Section>
      </main>
    );
  }

  return (
    <main>
      <Section>
        <div className="grid gap-5 sm:gap-8">
          <div className="max-w-3xl">
            <p className="font-latin text-2xl leading-none text-[var(--color-bronze)] sm:text-4xl">Meeting Review</p>
            <h1 className="mt-2 font-display text-xl font-normal leading-[1.28] sm:mt-4 sm:text-3xl">{review.title}</h1>
            <p className="mt-3 max-w-2xl text-sm leading-7 text-[var(--color-muted)] sm:mt-4 sm:text-base sm:leading-8">{review.meetingTitle} · {review.memberDisplayName}</p>
          </div>
          {message && <p className="text-sm leading-6 text-[var(--color-muted)]">{message}</p>}
          <Card>
            <div className="flex flex-wrap gap-2">
              <Tag>{review.status}</Tag>
              <Tag>{formatDate(review.createdAt)}</Tag>
            </div>
            {!editing && <p className="mt-4 whitespace-pre-wrap text-sm leading-7 text-[var(--color-charcoal)] sm:mt-6">{review.content}</p>}
            {!editing && review.images.length > 0 && (
              <div className="mt-5 sm:mt-7">
                <div
                  className="relative overflow-hidden rounded-[var(--radius-card)]"
                  onMouseDown={(event) => {
                    dragStartX.current = event.clientX;
                  }}
                  onMouseLeave={() => {
                    dragStartX.current = null;
                  }}
                  onMouseUp={(event) => finishImageDrag(event.clientX)}
                  onTouchEnd={(event) => {
                    const touch = event.changedTouches[0];
                    if (touch) {
                      finishImageDrag(touch.clientX);
                    }
                  }}
                  onTouchStart={(event) => {
                    dragStartX.current = event.touches[0]?.clientX ?? null;
                  }}
                >
                  {review.images.length > 1 && (
                    <>
                      <button
                        aria-label="이전 이미지 보기"
                        className="absolute inset-y-0 left-0 z-10 flex w-12 items-center justify-start bg-gradient-to-r from-[rgba(31,77,58,0.16)] to-transparent pl-2 opacity-80 transition hover:opacity-100 sm:w-16"
                        onClick={() => moveImage(-1)}
                        type="button"
                      >
                        <span className="grid size-7 place-items-center rounded-full border border-[rgba(31,77,58,0.16)] bg-[rgba(255,253,248,0.84)] shadow-[var(--shadow-soft)]">
                          <span className="block size-2.5 rotate-45 border-b border-l border-[var(--color-deep-green)]" />
                        </span>
                      </button>
                      <button
                        aria-label="다음 이미지 보기"
                        className="absolute inset-y-0 right-0 z-10 flex w-12 items-center justify-end bg-gradient-to-l from-[rgba(31,77,58,0.16)] to-transparent pr-2 opacity-80 transition hover:opacity-100 sm:w-16"
                        onClick={() => moveImage(1)}
                        type="button"
                      >
                        <span className="grid size-7 place-items-center rounded-full border border-[rgba(31,77,58,0.16)] bg-[rgba(255,253,248,0.84)] shadow-[var(--shadow-soft)]">
                          <span className="block size-2.5 -rotate-45 border-b border-r border-[var(--color-deep-green)]" />
                        </span>
                      </button>
                    </>
                  )}
                  {review.images[imageIndex]?.imageUrl ? (
                    <Image
                      alt=""
                      className="aspect-[4/3] w-full object-cover photo-muted"
                      height={900}
                      src={review.images[imageIndex].imageUrl}
                      unoptimized
                      width={1200}
                    />
                  ) : (
                    <div className="grid aspect-[4/3] place-items-center border border-[var(--color-line)] bg-[var(--color-ivory)] text-sm text-[var(--color-charcoal)]">이미지 저장소 설정 필요</div>
                  )}
                </div>
                {review.images.length > 1 && (
                  <div className="mt-3 flex justify-center gap-2" aria-label={`${review.images.length}개 이미지`}>
                    {review.images.map((image, index) => (
                      <button
                        aria-current={index === imageIndex}
                        aria-label={`${index + 1}번 이미지 보기`}
                        className={[
                          "size-2 rounded-full transition",
                          index === imageIndex ? "bg-[var(--color-deep-green)]" : "bg-[var(--color-line)] hover:bg-[var(--color-bronze)]",
                        ].join(" ")}
                        key={image.imageId}
                        onClick={() => setImageIndex(index)}
                        type="button"
                      />
                    ))}
                  </div>
                )}
              </div>
            )}
            {editing && (
              <form className="mt-4 grid gap-3 sm:mt-6" onSubmit={save}>
                <input className="min-h-10 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} value={form.title} />
                <textarea className="min-h-40 border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3 sm:min-h-56" onChange={(event) => setForm((current) => ({ ...current, content: event.target.value }))} value={form.content} />
                <div className="flex flex-wrap gap-3">
                  <Button type="submit">수정 저장</Button>
                  <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-normal" onClick={() => setEditing(false)} type="button">취소</button>
                </div>
              </form>
            )}
            {review.canEdit && !editing && (
              <div className="mt-4 flex flex-wrap gap-3 sm:mt-6">
                <button className="archive-record-action inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2" onClick={() => setEditing(true)} type="button">수정</button>
                <button className="archive-record-action archive-record-action--danger inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2" onClick={() => setDeleteOpen(true)} type="button">삭제</button>
              </div>
            )}
          </Card>
        </div>
      </Section>
      <ConfirmDialog
        description="삭제한 모임 후기는 후기 목록에서 더 이상 보이지 않습니다."
        onCancel={() => setDeleteOpen(false)}
        onConfirm={() => {
          void remove();
        }}
        open={deleteOpen}
        title="이 모임 후기를 삭제할까요?"
      />
    </main>
  );
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", { dateStyle: "medium", timeZone: "Asia/Seoul" }).format(new Date(value));
}

function ReviewDetailSkeleton() {
  return (
    <div className="grid gap-5 sm:gap-8" aria-label="모임 후기 상세 로딩 중">
      <div className="grid max-w-3xl gap-3">
        <SkeletonBlock className="h-8 w-36" />
        <SkeletonBlock className="h-7 w-full max-w-xl sm:h-9" />
        <SkeletonBlock className="h-4 w-64 max-w-full" />
      </div>
      <div className="rounded-[var(--radius-card)] border border-[rgba(233,225,214,0.8)] bg-[rgba(255,254,250,0.94)] p-4 shadow-[var(--shadow-soft)] sm:p-6">
        <div className="flex gap-2">
          <SkeletonBlock className="h-7 w-16 rounded-full" />
          <SkeletonBlock className="h-7 w-24 rounded-full" />
        </div>
        <div className="mt-5 grid gap-2">
          <SkeletonBlock className="h-4 w-full" />
          <SkeletonBlock className="h-4 w-11/12" />
          <SkeletonBlock className="h-4 w-4/5" />
        </div>
        <SkeletonBlock className="mt-6 aspect-[4/3] w-full" />
        <div className="mt-3 flex justify-center gap-2">
          <SkeletonBlock className="size-2 rounded-full" />
          <SkeletonBlock className="size-2 rounded-full" />
          <SkeletonBlock className="size-2 rounded-full" />
        </div>
      </div>
    </div>
  );
}
