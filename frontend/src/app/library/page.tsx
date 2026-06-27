"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { Avatar, Button, EmptyState, PageHeader, Section } from "@/components/ui/primitives";
import { apiGet, type LibraryResponse } from "@/lib/api";

export default function LibraryPage() {
  const [library, setLibrary] = useState<LibraryResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [recordQuery, setRecordQuery] = useState("");
  const [visibleRecordCount, setVisibleRecordCount] = useState(10);
  const [recommendedIndex, setRecommendedIndex] = useState(0);
  const dragStartX = useRef<number | null>(null);

  const filteredRecords = useMemo(() => {
    if (!library) {
      return [];
    }
    const query = recordQuery.trim().toLowerCase();
    if (!query) {
      return library.recentReadingRecords;
    }
    return library.recentReadingRecords.filter((record) => {
      return [
        record.bookTitle,
        record.authorsText,
        record.memberDisplayName,
        record.oneLineReview,
      ].some((value) => value.toLowerCase().includes(query));
    });
  }, [library, recordQuery]);

  const visibleRecords = filteredRecords.slice(0, visibleRecordCount);
  const recommendedBooks = library?.recommendedBooks ?? [];
  const currentRecommendedBook = recommendedBooks[recommendedIndex] ?? recommendedBooks[0];

  useEffect(() => {
    apiGet<LibraryResponse>("/library")
      .then((result) => {
        if (!result.success) {
          setError(result.error?.message ?? "라이브러리를 불러오지 못했습니다.");
          return;
        }
        setLibrary(result.data);
        setRecommendedIndex(0);
      })
      .catch(() => setError("라이브러리를 불러오지 못했습니다."));
  }, []);

  useEffect(() => {
    if (recommendedBooks.length <= 1) {
      return;
    }
    const timer = window.setInterval(() => {
      setRecommendedIndex((index) => (index + 1) % recommendedBooks.length);
    }, 5000);
    return () => window.clearInterval(timer);
  }, [recommendedBooks.length]);

  function finishRecommendedDrag(clientX: number) {
    if (dragStartX.current === null || recommendedBooks.length <= 1) {
      dragStartX.current = null;
      return;
    }
    const delta = clientX - dragStartX.current;
    dragStartX.current = null;
    if (Math.abs(delta) < 40) {
      return;
    }
    setRecommendedIndex((index) => (
      delta < 0
        ? (index + 1) % recommendedBooks.length
        : (index - 1 + recommendedBooks.length) % recommendedBooks.length
    ));
  }

  function moveRecommendedBook(direction: -1 | 1) {
    if (recommendedBooks.length <= 1) {
      return;
    }
    setRecommendedIndex((index) => (index + direction + recommendedBooks.length) % recommendedBooks.length);
  }

  return (
    <main>
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 bg-cover bg-center" style={{ backgroundImage: "url(/images/book-shelf.jpg)" }} />
        <div className="absolute inset-0 bg-[rgba(248,246,238,0.88)] backdrop-blur-[1px]" />
        <div className="relative mx-auto flex min-h-[420px] max-w-6xl items-end px-5 py-16 sm:px-8 lg:px-10">
          <div className="flex w-full flex-col gap-6 sm:flex-row sm:items-end sm:justify-between">
            <PageHeader
              eyebrow="Reading Library"
              title="독서기록 라이브러리"
              description="성장하는 사람들이 남긴 공개 독서기록과 책별 성장 기록을 여유 있게 탐색합니다."
            />
            <Button href="/reading-records/new">독서기록 작성</Button>
          </div>
        </div>
      </section>
      <Section>
        <div className="grid gap-8">
          {error && <EmptyState title="불러오기 실패" description={error} />}
          {!library && !error && <EmptyState title="불러오는 중입니다." description="공개 독서기록 데이터를 확인하고 있습니다." />}

          {library && (
            <div className="grid gap-8">
              <section className="grid gap-4">
                <h2 className="text-xl font-normal">이달의 추천책</h2>
                {library.recommendedBooks.length === 0 ? (
                  <EmptyState title="추천책 준비 중" description="운영진 추천책이 등록되면 이곳에 표시됩니다." />
                ) : currentRecommendedBook ? (
                  <div className="grid gap-4">
                    <article
                      className="relative grid cursor-grab select-none gap-5 overflow-hidden rounded-[var(--radius-card)] bg-[rgba(255,254,250,0.92)] p-4 shadow-[var(--shadow-soft)] active:cursor-grabbing sm:grid-cols-[180px_1fr] sm:p-5"
                      onMouseDown={(event) => {
                        dragStartX.current = event.clientX;
                      }}
                      onMouseLeave={() => {
                        dragStartX.current = null;
                      }}
                      onMouseUp={(event) => finishRecommendedDrag(event.clientX)}
                      onTouchEnd={(event) => {
                        const touch = event.changedTouches[0];
                        if (touch) {
                          finishRecommendedDrag(touch.clientX);
                        }
                      }}
                      onTouchStart={(event) => {
                        dragStartX.current = event.touches[0]?.clientX ?? null;
                      }}
                    >
                      {recommendedBooks.length > 1 && (
                        <>
                          <button
                            aria-label="이전 추천책 보기"
                            className="absolute inset-y-0 left-0 z-10 flex w-12 items-center justify-start bg-gradient-to-r from-[rgba(31,77,58,0.10)] to-transparent pl-2 opacity-75 transition hover:opacity-100 sm:w-16"
                            onClick={(event) => {
                              event.stopPropagation();
                              moveRecommendedBook(-1);
                            }}
                            type="button"
                          >
                            <span className="grid size-7 place-items-center rounded-full border border-[rgba(31,77,58,0.16)] bg-[rgba(255,253,248,0.82)] shadow-[var(--shadow-soft)]">
                              <span className="block size-2.5 rotate-45 border-b border-l border-[var(--color-deep-green)]" />
                            </span>
                          </button>
                          <button
                            aria-label="다음 추천책 보기"
                            className="absolute inset-y-0 right-0 z-10 flex w-12 items-center justify-end bg-gradient-to-l from-[rgba(31,77,58,0.10)] to-transparent pr-2 opacity-75 transition hover:opacity-100 sm:w-16"
                            onClick={(event) => {
                              event.stopPropagation();
                              moveRecommendedBook(1);
                            }}
                            type="button"
                          >
                            <span className="grid size-7 place-items-center rounded-full border border-[rgba(31,77,58,0.16)] bg-[rgba(255,253,248,0.82)] shadow-[var(--shadow-soft)]">
                              <span className="block size-2.5 -rotate-45 border-b border-r border-[var(--color-deep-green)]" />
                            </span>
                          </button>
                        </>
                      )}
                      <div className="aspect-[3/4] w-full max-w-[190px] justify-self-center rounded-[var(--radius-card)] bg-[var(--color-line)] bg-cover bg-center photo-muted sm:max-w-none" style={{ backgroundImage: `url(${currentRecommendedBook.thumbnailUrl ?? "/images/reading-books.jpg"})` }} />
                      <div className="flex min-w-0 flex-col justify-center">
                        <p className="text-xs text-[var(--color-bronze)]">이달의 추천 {String(currentRecommendedBook.displayOrder).padStart(2, "0")}</p>
                        <h3 className="mt-2 font-display text-2xl font-normal leading-snug">{currentRecommendedBook.title}</h3>
                        <p className="mt-2 text-sm text-[var(--color-muted)]">{currentRecommendedBook.authorsText}</p>
                        <p className="mt-5 max-w-2xl text-sm leading-7">{currentRecommendedBook.reason}</p>
                      </div>
                    </article>
                    <div className="flex justify-center">
                      <div className="flex items-center gap-2" aria-label="이달의 추천책 슬라이드 위치">
                        {recommendedBooks.map((book, index) => (
                          <button
                            aria-label={`추천책 ${index + 1}번 보기`}
                            aria-current={index === recommendedIndex}
                            className={[
                              "size-2.5 rounded-full transition",
                              index === recommendedIndex ? "bg-[var(--color-deep-green)]" : "bg-[var(--color-line)] hover:bg-[var(--color-bronze)]",
                            ].join(" ")}
                            key={book.id}
                            onClick={() => setRecommendedIndex(index)}
                            type="button"
                          />
                        ))}
                      </div>
                    </div>
                  </div>
                ) : (
                  <EmptyState title="추천책 준비 중" description="운영진 추천책이 등록되면 이곳에 표시됩니다." />
                )}
              </section>

              <section className="grid gap-4">
                <h2 className="text-xl font-normal">성장하는 사람들이 많이 읽은 책</h2>
                {library.popularBooks.length === 0 ? (
                  <EmptyState title="인기 도서가 아직 없습니다." description="공개 독서기록이 쌓이면 자동으로 계산됩니다." />
                ) : (
                  <div className="grid items-stretch gap-4 md:grid-cols-5">
                    {library.popularBooks.map((book, index) => (
                      <article className="group h-full" key={book.id}>
                        <a className="grid h-full grid-rows-[auto_1fr] overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-warm-white)] shadow-[var(--shadow-soft)]" href={`/books/${book.id}`}>
                          <div className="aspect-[3/4] bg-[var(--color-line)] bg-cover bg-center photo-muted transition group-hover:scale-[1.02]" style={{ backgroundImage: `url(${book.thumbnailUrl ?? "/images/reading-books.jpg"})` }} />
                          <div className="flex min-h-40 flex-col p-4">
                            <p className="text-xs text-[var(--color-bronze)]">많이 읽힌 책 {String(index + 1).padStart(2, "0")}</p>
                            <h3 className="mt-2 line-clamp-2 min-h-12 font-display text-lg font-normal leading-snug">{book.title}</h3>
                            <p className="mt-2 line-clamp-1 text-sm text-[var(--color-muted)]">{book.authorsText}</p>
                            <p className="mt-auto pt-3 text-sm text-[var(--color-muted)]">독서기록 {book.readingRecordCount}</p>
                          </div>
                        </a>
                      </article>
                    ))}
                  </div>
                )}
              </section>

              <section className="grid gap-4">
                <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
                  <div>
                    <h2 className="text-xl font-normal">최근 독서기록</h2>
                    <p className="mt-2 text-sm leading-6 text-[var(--color-muted)]">최신 기록을 10개씩 확인하고 책, 저자, 사람, 한줄평으로 검색합니다.</p>
                  </div>
                  <label className="w-full max-w-sm">
                    <span className="sr-only">최근 독서기록 검색</span>
                    <input
                      className="min-h-11 w-full rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 text-sm outline-none transition focus:border-[var(--color-deep-green)]"
                      onChange={(event) => {
                        setRecordQuery(event.target.value);
                        setVisibleRecordCount(10);
                      }}
                      placeholder="책, 저자, 사람, 한줄평 검색"
                      value={recordQuery}
                    />
                  </label>
                </div>
                {filteredRecords.length === 0 ? (
                  <EmptyState title="독서기록이 아직 없습니다." description="첫 독서기록이 작성되면 공개 피드에 표시됩니다." />
                ) : (
                  <div className="grid gap-5">
                    {visibleRecords.map((record) => (
                      <article className="grid gap-4 border-b border-[var(--color-line)] pb-6 md:grid-cols-[160px_1fr]" key={record.id}>
                        <div className="aspect-[4/3] rounded-[var(--radius-card)] bg-cover bg-center" style={{ backgroundImage: `url(${record.recordImageUrl ?? record.bookThumbnailUrl ?? "/images/reading-books.jpg"})` }} />
                        <div className="grid content-start gap-3">
                          <div className="flex flex-wrap items-start justify-between gap-3">
                            <div className="flex min-w-0 gap-3">
                              {record.memberProfileImageUrl ? (
                                <div
                                  aria-label={`${record.memberDisplayName} 프로필 이미지`}
                                  className="size-11 shrink-0 rounded-full bg-cover bg-center ring-1 ring-[var(--color-line)]"
                                  role="img"
                                  style={{ backgroundImage: `url(${record.memberProfileImageUrl})` }}
                                />
                              ) : (
                                <Avatar name={record.memberDisplayName} />
                              )}
                              <div className="min-w-0">
                                <p className="flex flex-wrap items-center gap-2 text-sm text-[var(--color-muted)]">
                                  <span>{record.memberDisplayName}</span>
                                  <span aria-hidden="true">·</span>
                                  <time dateTime={record.createdAt}>{formatKoreanDate(record.createdAt)}</time>
                                  {isToday(record.createdAt) && (
                                    <span className="rounded-full bg-[rgba(47,90,67,0.12)] px-2 py-0.5 text-xs text-[var(--color-deep-green)]">New</span>
                                  )}
                                </p>
                              </div>
                            </div>
                          </div>
                          <div>
                            <h3 className="font-display text-xl font-normal leading-snug">
                              <a className="font-normal" href={`/books/${record.bookId}`}>
                                {record.bookTitle}
                              </a>
                            </h3>
                            <p className="mt-1 text-sm text-[var(--color-muted)]">{record.authorsText}</p>
                          </div>
                          <p className="text-sm leading-7">{record.oneLineReview}</p>
                          <a className="inline-flex w-fit border-b border-[rgba(138,99,61,0.44)] pb-0.5 text-sm font-normal text-[var(--color-bronze)] transition hover:border-[var(--color-deep-green)] hover:text-[var(--color-deep-green)]" href={record.blogUrl}>
                            블로그 원문
                          </a>
                        </div>
                      </article>
                    ))}
                    {visibleRecordCount < filteredRecords.length && (
                      <div className="flex justify-center pt-2">
                        <button
                          aria-label="최근 독서기록 더 보기"
                          className="archive-more-button"
                          onClick={() => setVisibleRecordCount((count) => count + 10)}
                          type="button"
                        >
                          More
                        </button>
                      </div>
                    )}
                  </div>
                )}
              </section>
            </div>
          )}
        </div>
      </Section>
    </main>
  );
}

function formatKoreanDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeZone: "Asia/Seoul",
  }).format(new Date(value));
}

function isToday(value: string) {
  const formatter = new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Seoul",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
  return formatter.format(new Date(value)) === formatter.format(new Date());
}
