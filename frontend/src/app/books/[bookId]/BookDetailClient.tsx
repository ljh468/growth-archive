"use client";

import Image from "next/image";
import { FormEvent, useEffect, useState } from "react";
import { Avatar, Button, EmptyState, Section, SkeletonBlock, Tag } from "@/components/ui/primitives";
import { apiGet, apiPut, type BookDetailResponse, type CurrentUser, type ReadingRecord } from "@/lib/api";

type ReadingRecordEditForm = {
  rating: string;
  oneLineReview: string;
  blogUrl: string;
};

export function BookDetailClient({ bookId }: { bookId: string }) {
  const [detail, setDetail] = useState<BookDetailResponse | null>(null);
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [recordMessage, setRecordMessage] = useState<string | null>(null);
  const [editingRecordId, setEditingRecordId] = useState<number | null>(null);
  const [editForm, setEditForm] = useState<ReadingRecordEditForm>({ rating: "", oneLineReview: "", blogUrl: "" });
  const [savingRecord, setSavingRecord] = useState(false);
  const [recordPagination, setRecordPagination] = useState({ bookId, count: 5 });

  useEffect(() => {
    async function load() {
      const [bookResult, userResult] = await Promise.all([
        apiGet<BookDetailResponse>(`/books/${bookId}`),
        apiGet<CurrentUser>("/auth/me"),
      ]);
      if (!bookResult.success) {
        setError(bookResult.error?.message ?? "책 상세를 불러오지 못했습니다.");
        return;
      }
      setDetail(bookResult.data);
      if (userResult.success) {
        setUser(userResult.data);
      }
    }
    load().catch(() => setError("책 상세를 불러오지 못했습니다."));
  }, [bookId]);

  function startEditRecord(record: ReadingRecord) {
    setEditingRecordId(record.id);
    setEditForm({
      rating: String(record.rating ?? ""),
      oneLineReview: record.oneLineReview,
      blogUrl: record.blogUrl,
    });
    setRecordMessage(null);
  }

  async function saveRecord(event: FormEvent, record: ReadingRecord) {
    event.preventDefault();
    if (savingRecord) {
      return;
    }
    setSavingRecord(true);
    setRecordMessage(null);
    const result = await apiPut<ReadingRecord>(`/reading-records/${record.id}`, {
      bookId: record.bookId,
      rating: Number(editForm.rating),
      oneLineReview: editForm.oneLineReview,
      blogUrl: editForm.blogUrl,
      imageId: record.imageId,
    });
    if (!result.success) {
      setRecordMessage(result.error?.message ?? "독서기록을 수정하지 못했습니다.");
      setSavingRecord(false);
      return;
    }
    setDetail((current) => {
      if (!current) {
        return current;
      }
      return {
        ...current,
        readingRecords: current.readingRecords.map((item) => (item.id === record.id ? result.data : item)),
      };
    });
    setEditingRecordId(null);
    setSavingRecord(false);
    setRecordMessage("독서기록이 수정되었습니다.");
  }

  function cancelEditRecord() {
    setEditingRecordId(null);
    setRecordMessage(null);
  }

  function canEditRecord(record: ReadingRecord) {
    return Boolean(user?.memberId && user.memberId === record.memberId);
  }

  const visibleRecordCount = recordPagination.bookId === bookId ? recordPagination.count : 5;
  const visibleReadingRecords = detail?.readingRecords.slice(0, visibleRecordCount) ?? [];

  return (
    <main>
      {error && (
        <Section>
          <EmptyState title="불러오기 실패" description={error} />
        </Section>
      )}
      {!detail && !error && (
        <BookDetailSkeleton />
      )}
      {detail && (
        <>
          <section className="border-b border-[var(--color-line)] bg-[linear-gradient(180deg,var(--color-warm-white),var(--color-ivory))]">
            <Section>
              <div className="grid gap-5 sm:gap-8 lg:grid-cols-[300px_1fr] lg:items-end">
                <div className="w-full max-w-[148px] justify-self-center sm:max-w-[240px] lg:max-w-[300px]">
                  <div className="overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-line)] shadow-[0_22px_52px_rgba(63,47,34,0.16)]">
                    <Image
                      alt={`${detail.book.title} 표지`}
                      className="aspect-[2/3] w-full object-contain photo-muted"
                      height={450}
                      onError={(event) => {
                        event.currentTarget.src = "/images/reading-books.jpg";
                      }}
                      src={detail.book.thumbnailUrl || "/images/reading-books.jpg"}
                      unoptimized
                      width={300}
                    />
                  </div>
                </div>

                <div className="grid gap-4 sm:gap-6">
                  <div>
                    <p className="hidden font-latin text-4xl leading-none text-[var(--color-bronze)] sm:block">Book Detail</p>
                    <h1 className="max-w-3xl font-display text-2xl font-normal leading-[1.25] sm:mt-4 sm:text-4xl lg:text-5xl">{detail.book.title}</h1>
                    <p className="mt-2 max-w-2xl text-sm leading-6 text-[var(--color-muted)] sm:mt-4 sm:text-base sm:leading-8">
                      {detail.book.authorsText}
                      {detail.book.publisher ? ` · ${detail.book.publisher}` : ""}
                      {detail.book.publishedDate ? ` · ${formatYear(detail.book.publishedDate)}` : ""}
                    </p>
                  </div>

                  <div className="grid gap-1.5 sm:grid-cols-3 sm:gap-3">
                    <MetricCard label="평균 평점" value={detail.book.averageRating ? detail.book.averageRating.toFixed(1) : "-"} helper={detail.book.averageRating ? "5점 만점" : "아직 없음"} />
                    <MetricCard label="독서기록" value={detail.book.readingRecordCount.toLocaleString("ko-KR")} helper="공개 기록" />
                    <MetricCard label="읽은 사람" value={detail.book.readerCount.toLocaleString("ko-KR")} helper="기록 작성 기준" />
                  </div>

                  <div className="flex flex-wrap items-center gap-3">
                    <Button href={`/reading-records/new?bookId=${bookId}`}>이 책으로 기록하기</Button>
                    <Tag>{detail.book.status === "VERIFIED" ? "확인된 책" : "직접 등록된 책"}</Tag>
                  </div>
                </div>
              </div>
            </Section>
          </section>

          <Section>
            <section className="grid gap-5">
              <div className="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
                <div>
                  <p className="hidden font-latin text-4xl leading-none text-[var(--color-bronze)] sm:block">Reading Notes</p>
                  <h2 className="font-display text-xl font-normal leading-[1.25] sm:mt-2 sm:text-3xl">작성자별 독서기록</h2>
                </div>
                <p className="text-xs leading-5 text-[var(--color-muted)] sm:text-sm sm:leading-6">{detail.readingRecords.length.toLocaleString("ko-KR")}개의 공개 기록</p>
              </div>

              {detail.readingRecords.length === 0 ? (
                <EmptyState title="공개 독서기록이 없습니다." description="성장하는 사람들이 독서기록을 작성하면 여기에 표시됩니다." />
              ) : (
                <div className="grid gap-4">
                  {recordMessage && <p className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.78)] px-4 py-3 text-sm leading-6 text-[var(--color-charcoal)]">{recordMessage}</p>}
                  {visibleReadingRecords.map((record) => (
                    <article className="grid gap-3 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3 shadow-[var(--shadow-soft)] sm:grid-cols-[112px_1fr] sm:gap-4 sm:p-5" key={record.id}>
                      <div
                        className="hidden aspect-[4/3] rounded-[var(--radius-card)] bg-[var(--color-line)] bg-cover bg-center photo-muted sm:block sm:aspect-square"
                        style={{ backgroundImage: `url(${record.recordImageUrl ?? record.bookThumbnailUrl ?? detail.book.thumbnailUrl ?? "/images/reading-books.jpg"})` }}
                      />
                      <div className="grid min-w-0 gap-2.5 sm:gap-3">
                        <div className="flex flex-wrap items-start justify-between gap-3">
                          <div className="flex min-w-0 items-center gap-3">
                            {record.memberProfileImageUrl ? (
                              <Image alt="" className="size-10 shrink-0 rounded-full object-cover ring-1 ring-[var(--color-line)]" height={40} src={record.memberProfileImageUrl} unoptimized width={40} />
                            ) : (
                              <Avatar name={record.memberDisplayName} />
                            )}
                            <div className="min-w-0">
                              <p className="font-normal text-[var(--color-ink)]">{record.memberDisplayName}</p>
                              <time className="text-sm text-[var(--color-muted)]" dateTime={record.createdAt}>{formatDate(record.createdAt)}</time>
                            </div>
                          </div>
                          <div className="flex flex-wrap items-center justify-end gap-2">
                            <Tag>{ratingLabel(record.rating)}</Tag>
                          </div>
                        </div>

                        {editingRecordId === record.id ? (
                          <form className="grid gap-3" onSubmit={(event) => saveRecord(event, record)}>
                            <div className="grid gap-3 sm:grid-cols-[120px_1fr]">
                              <label className="grid gap-1.5 text-xs text-[var(--color-charcoal)]">
                                평점
                                <select className="min-h-10 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 text-sm" onChange={(event) => setEditForm((current) => ({ ...current, rating: event.target.value }))} required value={editForm.rating}>
                                  <option disabled value="">평점 선택</option>
                                  <option value="1">1</option>
                                  <option value="2">2</option>
                                  <option value="3">3</option>
                                  <option value="4">4</option>
                                  <option value="5">5</option>
                                </select>
                              </label>
                              <label className="grid gap-1.5 text-xs text-[var(--color-charcoal)]">
                                블로그 원문
                                <input className="min-h-10 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 text-sm" onChange={(event) => setEditForm((current) => ({ ...current, blogUrl: event.target.value }))} required value={editForm.blogUrl} />
                              </label>
                            </div>
                            <label className="grid gap-1.5 text-xs text-[var(--color-charcoal)]">
                              한줄평
                              <input className="min-h-10 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 text-sm" maxLength={300} onChange={(event) => setEditForm((current) => ({ ...current, oneLineReview: event.target.value }))} required value={editForm.oneLineReview} />
                            </label>
                            <div className="flex flex-wrap gap-2">
                              <button className="archive-record-button archive-record-button--primary" type="submit">
                                {savingRecord ? "저장 중" : "저장"}
                              </button>
                              <button className="archive-record-button" onClick={cancelEditRecord} type="button">
                                취소
                              </button>
                            </div>
                          </form>
                        ) : (
                          <>
                            <p className="text-sm leading-6 text-[var(--color-charcoal)] sm:leading-7">{record.oneLineReview}</p>

                            <a
                              className="inline-flex w-fit items-center border-b border-[#0b3d2e] text-xs font-normal text-[#0b3d2e] transition hover:border-[var(--color-deep-green)] hover:text-[var(--color-deep-green)]"
                              href={record.blogUrl}
                              rel="noreferrer"
                              target="_blank"
                            >
                              블로그 원문
                            </a>
                            {canEditRecord(record) && (
                              <div className="flex justify-end">
                                <button className="archive-record-button" onClick={() => startEditRecord(record)} type="button">
                                  수정
                                </button>
                              </div>
                            )}
                          </>
                        )}
                      </div>
                    </article>
                  ))}
                  {visibleRecordCount < detail.readingRecords.length && (
                    <div className="flex justify-center pt-2">
                      <button
                        aria-label="작성자별 독서기록 더 보기"
                        className="archive-more-button"
                        onClick={() => setRecordPagination({ bookId, count: visibleRecordCount + 5 })}
                        type="button"
                      >
                        More
                      </button>
                    </div>
                  )}
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
    <div className="flex items-center justify-between rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.78)] px-3 py-2 sm:block sm:p-4">
      <p className="text-xs text-[var(--color-muted)]">{label}</p>
      <p className="font-display text-lg font-normal leading-none text-[var(--color-ink)] sm:mt-2 sm:text-2xl">{value}</p>
      <p className="hidden text-xs text-[var(--color-muted)] sm:mt-2 sm:block">{helper}</p>
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

function BookDetailSkeleton() {
  return (
    <>
      <section className="border-b border-[var(--color-line)] bg-[linear-gradient(180deg,var(--color-warm-white),var(--color-ivory))]">
        <Section>
          <div className="grid gap-5 sm:gap-8 lg:grid-cols-[300px_1fr] lg:items-end">
            <SkeletonBlock className="aspect-[2/3] w-full max-w-[148px] justify-self-center shadow-[0_22px_52px_rgba(63,47,34,0.10)] sm:max-w-[240px] lg:max-w-[300px]" />
            <div className="grid gap-4 sm:gap-6">
              <div className="grid gap-3">
                <SkeletonBlock className="hidden h-8 w-36 sm:block" />
                <SkeletonBlock className="h-8 w-full max-w-2xl sm:h-11" />
                <SkeletonBlock className="h-4 w-3/4 max-w-xl" />
              </div>
              <div className="grid gap-1.5 sm:grid-cols-3 sm:gap-3">
                {Array.from({ length: 3 }).map((_, index) => (
                  <SkeletonBlock className="h-12 sm:h-28" key={index} />
                ))}
              </div>
              <div className="flex gap-3">
                <SkeletonBlock className="h-11 w-36" />
                <SkeletonBlock className="h-7 w-24 rounded-full" />
              </div>
            </div>
          </div>
        </Section>
      </section>
      <Section>
        <div className="grid gap-5">
          <div className="grid gap-2">
            <SkeletonBlock className="hidden h-8 w-36 sm:block" />
            <SkeletonBlock className="h-7 w-48" />
          </div>
          {Array.from({ length: 3 }).map((_, index) => (
            <article className="grid gap-3 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3 shadow-[var(--shadow-soft)] sm:grid-cols-[112px_1fr] sm:gap-4 sm:p-5" key={index}>
              <SkeletonBlock className="hidden aspect-square sm:block" />
              <div className="grid gap-3">
                <div className="flex items-center justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <SkeletonBlock className="size-10 rounded-full" />
                    <div className="grid gap-2">
                      <SkeletonBlock className="h-4 w-24" />
                      <SkeletonBlock className="h-4 w-20" />
                    </div>
                  </div>
                  <SkeletonBlock className="h-7 w-20 rounded-full" />
                </div>
                <SkeletonBlock className="h-4 w-full" />
                <SkeletonBlock className="h-4 w-5/6" />
                <SkeletonBlock className="h-5 w-28" />
              </div>
            </article>
          ))}
        </div>
      </Section>
    </>
  );
}
