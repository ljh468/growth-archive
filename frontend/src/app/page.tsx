"use client";

import type { PointerEvent } from "react";
import { useEffect, useRef, useState } from "react";
import { Button, EmptyState, Section } from "@/components/ui/primitives";
import { apiGet, type MeetingReviewSummary } from "@/lib/api";

const heroSlides = [
  "/images/korean-bookclub-discussion.jpg",
  "/images/korean-reading-table.jpg",
  "/images/meeting-table.jpg",
  "/images/hero-bookclub-study.png",
  "/images/hero-bookclub-discussion.png",
];

export default function HomePage() {
  const [activeSlide, setActiveSlide] = useState(0);
  const [dragOffset, setDragOffset] = useState(0);
  const [isDragging, setIsDragging] = useState(false);
  const [reviews, setReviews] = useState<MeetingReviewSummary[]>([]);
  const dragStartX = useRef<number | null>(null);
  const dragCurrentX = useRef(0);
  const heroRef = useRef<HTMLElement | null>(null);

  useEffect(() => {
    apiGet<MeetingReviewSummary[]>("/reviews?page=0&size=3").then((result) => result.success && setReviews(result.data));
  }, []);

  useEffect(() => {
    const timer = window.setInterval(() => {
      if (dragStartX.current === null) {
        setActiveSlide((current) => (current + 1) % heroSlides.length);
      }
    }, 5200);
    return () => window.clearInterval(timer);
  }, []);

  function goToSlide(index: number) {
    setActiveSlide((index + heroSlides.length) % heroSlides.length);
    setDragOffset(0);
  }

  function handlePointerDown(event: PointerEvent<HTMLElement>) {
    if (event.pointerType === "mouse" && event.button !== 0) {
      return;
    }
    dragStartX.current = event.clientX;
    dragCurrentX.current = event.clientX;
    setIsDragging(true);
    event.currentTarget.setPointerCapture(event.pointerId);
  }

  function handlePointerMove(event: PointerEvent<HTMLElement>) {
    if (dragStartX.current === null || !heroRef.current) {
      return;
    }
    dragCurrentX.current = event.clientX;
    const width = heroRef.current.getBoundingClientRect().width || 1;
    const delta = event.clientX - dragStartX.current;
    setDragOffset(Math.max(-18, Math.min(18, (delta / width) * 100)));
  }

  function handlePointerEnd(event: PointerEvent<HTMLElement>) {
    if (dragStartX.current === null || !heroRef.current) {
      return;
    }
    const width = heroRef.current.getBoundingClientRect().width || 1;
    const delta = dragCurrentX.current - dragStartX.current;
    const threshold = Math.max(60, width * 0.12);

    if (delta <= -threshold) {
      goToSlide(activeSlide + 1);
    } else if (delta >= threshold) {
      goToSlide(activeSlide - 1);
    } else {
      setDragOffset(0);
    }

    dragStartX.current = null;
    dragCurrentX.current = 0;
    setIsDragging(false);
    if (event.currentTarget.hasPointerCapture(event.pointerId)) {
      event.currentTarget.releasePointerCapture(event.pointerId);
    }
  }

  return (
    <main>
      <section
        className="relative min-h-[calc(100vh-5rem)] touch-pan-y overflow-hidden bg-[var(--color-ink)]"
        onPointerCancel={handlePointerEnd}
        onPointerDown={handlePointerDown}
        onPointerMove={handlePointerMove}
        onPointerUp={handlePointerEnd}
        ref={heroRef}
      >
        <div
          className={`absolute inset-0 flex ${isDragging ? "" : "transition-transform duration-700"}`}
          style={{ transform: `translateX(calc(${-activeSlide * 100}% + ${dragOffset}%))` }}
        >
          {heroSlides.map((image) => (
            <div
              className="min-w-full bg-cover bg-center photo-muted"
              key={image}
              style={{ backgroundImage: `url(${image})` }}
            />
          ))}
        </div>
        <div className="absolute inset-0 bg-[linear-gradient(90deg,rgba(31,47,36,0.78),rgba(45,63,49,0.36),rgba(248,246,238,0.06))]" />
        <div className="absolute inset-0 bg-[rgba(31,77,58,0.12)]" />

        <div className="pointer-events-none relative mx-auto grid min-h-[calc(100vh-5rem)] max-w-6xl content-end px-5 pb-16 pt-24 sm:px-8 sm:pb-20 lg:px-10">
          <div className="max-w-3xl text-[var(--color-warm-white)]">
            <p className="font-hand text-3xl text-[#efe7d8] sm:text-4xl">부자습관 만들기 모임</p>
            <h1 className="mt-5 max-w-3xl font-display text-3xl font-normal leading-[1.22] sm:text-4xl lg:text-5xl">
              읽고, 실행하고, 성장한 기록을 남기는 사람들
            </h1>
            <div className="pointer-events-auto mt-8 flex flex-wrap gap-3" onPointerDown={(event) => event.stopPropagation()}>
              <Button href="/library">독서기록 라이브러리</Button>
              <Button href="/meetings" variant="secondary">모임 일정</Button>
            </div>
          </div>

          <div className="pointer-events-auto mt-12 flex gap-2">
            {heroSlides.map((image, index) => (
              <button
                aria-label={`슬라이드 ${index + 1}`}
                className={`h-1.5 rounded-full transition-all ${index === activeSlide ? "w-10 bg-[var(--color-warm-white)]" : "w-5 bg-[rgba(255,253,248,0.42)]"}`}
                key={image}
                onClick={() => goToSlide(index)}
                type="button"
              />
            ))}
          </div>
        </div>
      </section>

      <Section>
        <div className="mx-auto max-w-5xl text-center">
          <p className="font-latin text-4xl text-[var(--color-bronze)] sm:text-5xl">Book, Action, Reflection</p>
          <h2 className="mt-4 font-display text-2xl font-normal leading-[1.35] sm:whitespace-nowrap sm:text-3xl lg:text-4xl">
            책을 읽는 데서 멈추지 않고, 실행한 흔적을 함께 보관합니다.
          </h2>
          <p className="keep-words mt-5 text-base leading-8 text-[var(--color-muted)]">
            모임의 대화, 한 달의 실행계획, 읽은 뒤 남긴 문장을 차분하게 쌓아가는 프라이빗 아카이브입니다.
          </p>
        </div>
      </Section>

      <Section>
        <div className="grid gap-8">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <p className="font-latin text-4xl text-[var(--color-bronze)]">Reviews</p>
              <h2 className="mt-2 font-display text-3xl font-normal leading-[1.25] sm:text-4xl">모임 후기는 사진과 문장으로 남깁니다.</h2>
            </div>
            <Button href="/reviews" variant="secondary">후기 전체 보기</Button>
          </div>

          {reviews.length ? (
            <div className="grid gap-5 md:grid-cols-3">
              {reviews.map((review) => (
                <a className="group overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-warm-white)] shadow-[var(--shadow-soft)] transition hover:-translate-y-0.5" href={`/reviews/${review.id}`} key={review.id}>
                  <div className="aspect-[4/3] bg-cover bg-center photo-muted" style={{ backgroundImage: `url(${review.representativeImageUrl ?? "/images/korean-reading-table.jpg"})` }} />
                  <div className="p-6">
                    <p className="text-sm text-[var(--color-bronze)]">{review.meetingTitle}</p>
                    <h3 className="mt-2 font-display text-xl font-normal">{review.title}</h3>
                    <p className="mt-3 line-clamp-3 text-sm leading-7 text-[var(--color-muted)]">{review.contentSummary}</p>
                  </div>
                </a>
              ))}
            </div>
          ) : (
            <EmptyState title="후기를 기다리는 중" description="모임 후기가 작성되면 최근 후기에 표시됩니다." />
          )}
        </div>
      </Section>
    </main>
  );
}
