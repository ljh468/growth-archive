"use client";

import Image from "next/image";
import { useEffect, useMemo, useRef, useState } from "react";
import { Button, EmptyState, MoreButton, PageHeader, Section, SkeletonBlock } from "@/components/ui/primitives";
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
          {!library && !error && <LibrarySkeleton />}

          {library && (
            <div className="grid gap-8">
              <section className="grid gap-4">
                <h2 className="text-xl font-normal">추천책</h2>
                {library.recommendedBooks.length === 0 ? (
                  <EmptyState title="추천책 준비 중" description="운영진 추천책이 등록되면 이곳에 표시됩니다." />
                ) : currentRecommendedBook ? (
                  <div className="grid gap-4">
                    <article
                      className="relative grid cursor-grab select-none grid-cols-[104px_minmax(0,1fr)] items-center gap-3 overflow-hidden rounded-[var(--radius-card)] bg-[rgba(255,254,250,0.92)] p-3 shadow-[var(--shadow-soft)] active:cursor-grabbing sm:grid-cols-[180px_1fr] sm:gap-5 sm:p-5"
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
                      <Image
                        alt={`${currentRecommendedBook.title} 표지`}
                        className="aspect-[3/4] w-full max-w-[104px] justify-self-center rounded-[var(--radius-card)] bg-[var(--color-line)] object-cover photo-muted shadow-[0_14px_30px_rgba(63,47,34,0.16),0_2px_8px_rgba(63,47,34,0.08)] sm:max-w-none sm:shadow-[0_18px_42px_rgba(63,47,34,0.18),0_2px_10px_rgba(63,47,34,0.10)]"
                        height={360}
                        priority
                        sizes="(min-width: 640px) 180px, 104px"
                        src={currentRecommendedBook.thumbnailUrl || "/images/reading-books.jpg"}
                        width={240}
                      />
                      <div className="flex min-w-0 flex-col justify-center pl-1 pr-6 sm:px-0">
                        <p className="text-xs text-[var(--color-bronze)]">추천 {String(currentRecommendedBook.displayOrder).padStart(2, "0")}</p>
                        <h3 className="mt-1 break-words font-display text-base font-normal leading-snug [word-break:keep-all] sm:mt-2 sm:text-2xl">{currentRecommendedBook.title}</h3>
                        <p className="mt-1 line-clamp-1 text-xs text-[var(--color-muted)] sm:mt-2 sm:text-sm">{currentRecommendedBook.authorsText}</p>
                        <p className="mt-2 line-clamp-2 max-w-2xl break-words text-xs leading-5 [word-break:keep-all] sm:mt-5 sm:line-clamp-none sm:text-sm sm:leading-7">{currentRecommendedBook.reason}</p>
                      </div>
                    </article>
                    <div className="flex justify-center">
                      <div className="flex items-center gap-2" aria-label="추천책 슬라이드 위치">
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
                        <a className="grid h-full grid-cols-[72px_1fr] gap-4 overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-warm-white)] p-3 shadow-[var(--shadow-soft)] md:grid-cols-1 md:grid-rows-[auto_1fr] md:gap-0 md:p-0" href={`/books/${book.id}`}>
                          <Image
                            alt={`${book.title} 표지`}
                            className="aspect-[3/4] w-[72px] self-center rounded-[var(--radius-card)] bg-[var(--color-line)] object-contain photo-muted transition group-hover:scale-[1.02] md:w-full md:self-auto md:rounded-none"
                            height={300}
                            loading="lazy"
                            sizes="(min-width: 768px) 20vw, 72px"
                            src={book.thumbnailUrl || "/images/reading-books.jpg"}
                            width={200}
                          />
                          <div className="flex min-h-0 flex-col py-1 md:min-h-40 md:p-4">
                            <p className="text-xs text-[var(--color-bronze)]">많이 읽힌 책 {String(index + 1).padStart(2, "0")}</p>
                            <h3 className="mt-1 line-clamp-2 font-display text-base font-normal leading-snug md:mt-2 md:min-h-12 md:text-lg">{book.title}</h3>
                            <p className="mt-2 line-clamp-1 text-sm text-[var(--color-muted)]">{book.authorsText}</p>
                            <p className="mt-auto pt-2 text-sm text-[var(--color-muted)] md:pt-3">독서기록 {book.readingRecordCount}</p>
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
                    <p className="mt-2 hidden text-sm leading-6 text-[var(--color-muted)] sm:block">최신 기록을 10개씩 확인하고 책, 저자, 사람, 한줄평으로 검색합니다.</p>
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
                      <article className="border-b border-[var(--color-line)] pb-6" key={record.id}>
                        <div className="grid grid-cols-[72px_1fr] gap-4 sm:grid-cols-[84px_1fr]">
                          <a
                            aria-label={`${record.bookTitle} 상세 보기`}
                            className="relative aspect-[3/4] w-[72px] overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-line)] photo-muted shadow-[0_12px_28px_rgba(63,47,34,0.08)] sm:w-[84px]"
                            href={`/books/${record.bookId}`}
                          >
                            <Image
                              alt=""
                              className="object-cover"
                              fill
                              loading="lazy"
                              sizes="(min-width: 640px) 84px, 72px"
                              src={record.recordImageUrl ?? record.bookThumbnailUrl ?? "/images/reading-books.jpg"}
                            />
                          </a>

                          <div className="grid min-w-0 content-start gap-2">
                            <div className="flex min-w-0 flex-wrap items-center gap-2 text-sm text-[var(--color-muted)]">
                              {record.memberProfileImageUrl ? (
                                <div
                                  aria-label={`${record.memberDisplayName} 프로필 이미지`}
                                  className="size-7 shrink-0 rounded-full bg-cover bg-center ring-1 ring-[var(--color-line)]"
                                  role="img"
                                  style={{ backgroundImage: `url(${record.memberProfileImageUrl})` }}
                                />
                              ) : (
                                <div className="flex size-7 shrink-0 items-center justify-center rounded-full bg-[var(--color-deep-green)] text-xs font-normal text-[var(--color-warm-white)]">
                                  {record.memberDisplayName.trim().slice(0, 1)}
                                </div>
                              )}
                              <span>{record.memberDisplayName}</span>
                              <span aria-hidden="true">·</span>
                              <time dateTime={record.createdAt}>{formatKoreanDate(record.createdAt)}</time>
                              {isToday(record.createdAt) && (
                                <span className="rounded-full bg-[rgba(47,90,67,0.12)] px-2 py-0.5 text-xs text-[var(--color-deep-green)]">New</span>
                              )}
                              <a
                                className="inline-flex items-center border-b border-[#0b3d2e] text-xs font-normal text-[#0b3d2e] transition hover:border-[var(--color-deep-green)] hover:text-[var(--color-deep-green)]"
                                href={record.blogUrl}
                              >
                                블로그 원문
                              </a>
                            </div>

                            <h3 className="font-display text-lg font-normal leading-snug sm:text-xl">
                              <a className="font-normal transition hover:text-[var(--color-deep-green)]" href={`/books/${record.bookId}`}>
                                {record.bookTitle}
                              </a>
                            </h3>
                            <p className="line-clamp-1 text-sm text-[var(--color-muted)]">{record.authorsText}</p>
                            <p className="line-clamp-2 text-sm leading-7 text-[var(--color-charcoal)]">{record.oneLineReview}</p>
                          </div>
                        </div>
                      </article>
                    ))}
                    {visibleRecordCount < filteredRecords.length && (
                      <div className="flex justify-center pt-2">
                        <MoreButton
                          aria-label="최근 독서기록 더 보기"
                          onClick={() => setVisibleRecordCount((count) => count + 10)}
                        >
                          More
                        </MoreButton>
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

function LibrarySkeleton() {
  return (
    <div className="grid gap-8" aria-label="독서기록 라이브러리 로딩 중">
      <section className="grid gap-4">
        <SkeletonBlock className="h-7 w-36" />
        <article className="grid grid-cols-[104px_minmax(0,1fr)] items-center gap-3 overflow-hidden rounded-[var(--radius-card)] bg-[rgba(255,254,250,0.92)] p-3 shadow-[var(--shadow-soft)] sm:grid-cols-[180px_1fr] sm:gap-5 sm:p-5">
          <SkeletonBlock className="aspect-[3/4] w-full max-w-[104px] justify-self-center sm:max-w-none" />
          <div className="grid content-center gap-3">
            <SkeletonBlock className="h-4 w-24" />
            <SkeletonBlock className="h-5 w-3/4 sm:h-7" />
            <SkeletonBlock className="h-4 w-40" />
            <div className="mt-2 grid gap-2">
              <SkeletonBlock className="h-4 w-full" />
              <SkeletonBlock className="h-4 w-5/6" />
            </div>
          </div>
        </article>
      </section>

      <section className="grid gap-4">
        <SkeletonBlock className="h-7 w-56" />
        <div className="grid items-stretch gap-4 md:grid-cols-5">
          {Array.from({ length: 5 }).map((_, index) => (
            <article className="grid grid-cols-[72px_1fr] gap-4 rounded-[var(--radius-card)] bg-[var(--color-warm-white)] p-3 shadow-[var(--shadow-soft)] md:grid-cols-1 md:gap-0 md:p-0" key={index}>
              <SkeletonBlock className="aspect-[3/4] w-[72px] md:w-full md:rounded-none" />
              <div className="grid content-start gap-2 py-1 md:min-h-40 md:p-4">
                <SkeletonBlock className="h-3 w-20" />
                <SkeletonBlock className="h-4 w-full" />
                <SkeletonBlock className="h-4 w-4/5" />
                <SkeletonBlock className="mt-2 h-4 w-24 md:mt-auto" />
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="grid gap-4">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div className="grid gap-2">
            <SkeletonBlock className="h-7 w-36" />
            <SkeletonBlock className="hidden h-4 w-96 sm:block" />
          </div>
          <SkeletonBlock className="h-11 w-full max-w-sm" />
        </div>
        <div className="grid gap-5">
          {Array.from({ length: 4 }).map((_, index) => (
            <article className="border-b border-[var(--color-line)] pb-6" key={index}>
              <div className="grid grid-cols-[72px_1fr] gap-4 sm:grid-cols-[84px_1fr]">
                <SkeletonBlock className="aspect-[3/4] w-[72px] sm:w-[84px]" />
                <div className="grid min-w-0 content-start gap-2">
                  <div className="flex items-center gap-2">
                    <SkeletonBlock className="size-7 rounded-full" />
                    <SkeletonBlock className="h-4 w-24" />
                    <SkeletonBlock className="h-4 w-20" />
                  </div>
                  <SkeletonBlock className="h-5 w-3/4" />
                  <SkeletonBlock className="h-4 w-36" />
                  <SkeletonBlock className="h-4 w-full" />
                  <SkeletonBlock className="h-4 w-5/6" />
                </div>
              </div>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}
