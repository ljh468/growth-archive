"use client";

import { useEffect, useState } from "react";
import { Avatar, Button, EmptyState, Section, Tag } from "@/components/ui/primitives";
import { apiGet, type BookDetailResponse } from "@/lib/api";

export function BookDetailClient({ bookId }: { bookId: string }) {
  const [detail, setDetail] = useState<BookDetailResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    apiGet<BookDetailResponse>(`/books/${bookId}`)
      .then((result) => {
        if (!result.success) {
          setError(result.error?.message ?? "책 상세를 불러오지 못했습니다.");
          return;
        }
        setDetail(result.data);
      })
      .catch(() => setError("책 상세를 불러오지 못했습니다."));
  }, [bookId]);

  return (
    <main>
      {error && (
        <Section>
          <EmptyState title="불러오기 실패" description={error} />
        </Section>
      )}
      {!detail && !error && (
        <Section>
          <EmptyState title="불러오는 중입니다." description="책 상세와 독서기록을 확인하고 있습니다." />
        </Section>
      )}
      {detail && (
        <>
          <section className="border-b border-[var(--color-line)] bg-[linear-gradient(180deg,var(--color-warm-white),var(--color-ivory))]">
            <Section>
              <div className="grid gap-8 lg:grid-cols-[260px_1fr] lg:items-end">
                <div className="w-full max-w-[220px] justify-self-center lg:max-w-[260px]">
                  <div className="overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-line)] shadow-[0_22px_52px_rgba(63,47,34,0.16)]">
                    <img
                      alt={`${detail.book.title} 표지`}
                      className="aspect-[3/4] w-full object-cover photo-muted"
                      onError={(event) => {
                        event.currentTarget.src = "/images/reading-books.jpg";
                      }}
                      src={detail.book.thumbnailUrl ?? "/images/reading-books.jpg"}
                    />
                  </div>
                </div>

                <div className="grid gap-6">
                  <div>
                    <p className="font-latin text-4xl leading-none text-[var(--color-bronze)]">Book Detail</p>
                    <h1 className="mt-4 max-w-3xl font-display text-3xl font-normal leading-[1.2] sm:text-4xl lg:text-5xl">{detail.book.title}</h1>
                    <p className="mt-4 max-w-2xl text-base leading-8 text-[var(--color-muted)]">
                      {detail.book.authorsText}
                      {detail.book.publisher ? ` · ${detail.book.publisher}` : ""}
                      {detail.book.publishedDate ? ` · ${formatYear(detail.book.publishedDate)}` : ""}
                    </p>
                  </div>

                  <div className="grid gap-3 sm:grid-cols-3">
                    <MetricCard label="평균 평점" value={detail.book.averageRating ? detail.book.averageRating.toFixed(1) : "-"} helper={detail.book.averageRating ? "5점 만점" : "아직 없음"} />
                    <MetricCard label="독서기록" value={detail.book.readingRecordCount.toLocaleString("ko-KR")} helper="공개 기록" />
                    <MetricCard label="읽은 사람" value={detail.book.readerCount.toLocaleString("ko-KR")} helper="기록 작성 기준" />
                  </div>

                  <div className="flex flex-wrap items-center gap-3">
                    <Button href="/reading-records/new">이 책으로 기록하기</Button>
                    <Tag>{detail.book.status === "VERIFIED" ? "확인된 책" : "직접 등록된 책"}</Tag>
                  </div>
                </div>
              </div>
            </Section>
          </section>

          <Section>
            <section className="grid gap-5">
              <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
                <div>
                  <p className="font-latin text-4xl leading-none text-[var(--color-bronze)]">Reading Notes</p>
                  <h2 className="mt-2 font-display text-2xl font-normal leading-[1.25] sm:text-3xl">작성자별 독서기록</h2>
                </div>
                <p className="text-sm leading-6 text-[var(--color-muted)]">{detail.readingRecords.length.toLocaleString("ko-KR")}개의 공개 기록</p>
              </div>

              {detail.readingRecords.length === 0 ? (
                <EmptyState title="공개 독서기록이 없습니다." description="성장하는 사람들이 독서기록을 작성하면 여기에 표시됩니다." />
              ) : (
                <div className="grid gap-4">
                  {detail.readingRecords.map((record) => (
                    <article className="grid gap-4 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4 shadow-[var(--shadow-soft)] sm:grid-cols-[112px_1fr] sm:p-5" key={record.id}>
                      <div
                        className="aspect-[4/3] rounded-[var(--radius-card)] bg-[var(--color-line)] bg-cover bg-center photo-muted sm:aspect-square"
                        style={{ backgroundImage: `url(${record.recordImageUrl ?? record.bookThumbnailUrl ?? detail.book.thumbnailUrl ?? "/images/reading-books.jpg"})` }}
                      />
                      <div className="grid min-w-0 gap-3">
                        <div className="flex flex-wrap items-start justify-between gap-3">
                          <div className="flex min-w-0 items-center gap-3">
                            {record.memberProfileImageUrl ? (
                              <img alt="" className="size-10 shrink-0 rounded-full object-cover ring-1 ring-[var(--color-line)]" src={record.memberProfileImageUrl} />
                            ) : (
                              <Avatar name={record.memberDisplayName} />
                            )}
                            <div className="min-w-0">
                              <p className="font-normal text-[var(--color-ink)]">{record.memberDisplayName}</p>
                              <time className="text-sm text-[var(--color-muted)]" dateTime={record.createdAt}>{formatDate(record.createdAt)}</time>
                            </div>
                          </div>
                          <Tag>{ratingLabel(record.rating)}</Tag>
                        </div>

                        <p className="text-sm leading-7 text-[var(--color-charcoal)]">{record.oneLineReview}</p>

                        <a
                          className="inline-flex w-fit items-center gap-2 border-b border-[rgba(138,106,69,0.44)] pb-0.5 text-sm font-normal text-[var(--color-bronze)] transition hover:border-[var(--color-deep-green)] hover:text-[var(--color-deep-green)]"
                          href={record.blogUrl}
                          rel="noreferrer"
                          target="_blank"
                        >
                          블로그 원문
                          <span aria-hidden="true">↗</span>
                        </a>
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </section>
          </Section>
        </>
      )}
    </main>
  );
}

function MetricCard({ label, value, helper }: { label: string; value: string; helper: string }) {
  return (
    <div className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.78)] p-4">
      <p className="text-xs text-[var(--color-muted)]">{label}</p>
      <p className="mt-2 font-display text-2xl font-normal leading-none text-[var(--color-ink)]">{value}</p>
      <p className="mt-2 text-xs text-[var(--color-muted)]">{helper}</p>
    </div>
  );
}

function ratingLabel(value: number | null) {
  return value ? `평점 ${value}/5` : "평점 없음";
}

function formatYear(value: string) {
  return new Intl.DateTimeFormat("ko-KR", { year: "numeric", timeZone: "Asia/Seoul" }).format(new Date(value));
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", { dateStyle: "medium", timeZone: "Asia/Seoul" }).format(new Date(value));
}
