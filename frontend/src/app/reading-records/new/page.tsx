"use client";

import Image from "next/image";
import { FormEvent, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { MobileBackButton } from "@/components/MobileBackButton";
import { Button, Card, PageHeader, Section } from "@/components/ui/primitives";
import { apiGet, apiPost, type BookDetailResponse, type BookSearchResult, type BookSummary, type ReadingRecord, uploadImage } from "@/lib/api";

export default function NewReadingRecordPage() {
  const [query, setQuery] = useState("");
  const [searchResults, setSearchResults] = useState<BookSearchResult[]>([]);
  const [searchResultPage, setSearchResultPage] = useState(0);
  const [selectedSearchKey, setSelectedSearchKey] = useState<string | null>(null);
  const [selectedBook, setSelectedBook] = useState<BookSummary | null>(null);
  const [savedRecord, setSavedRecord] = useState<ReadingRecord | null>(null);
  const [searchMessage, setSearchMessage] = useState<string | null>(null);
  const [manualError, setManualError] = useState<string | null>(null);
  const [recordError, setRecordError] = useState<string | null>(null);
  const [manualOpen, setManualOpen] = useState(false);
  const [manualSubmitting, setManualSubmitting] = useState(false);
  const [recordSubmitting, setRecordSubmitting] = useState(false);
  const [manualBook, setManualBook] = useState({ title: "", author: "", publisher: "" });
  const [manualBookCoverFile, setManualBookCoverFile] = useState<File | null>(null);
  const [record, setRecord] = useState({ rating: "", oneLineReview: "", blogUrl: "" });
  const [recordImageFile, setRecordImageFile] = useState<File | null>(null);
  const searchPageSize = 3;
  const searchPageCount = Math.max(1, Math.ceil(searchResults.length / searchPageSize));
  const currentSearchPage = Math.min(searchResultPage, searchPageCount - 1);
  const visibleSearchResults = searchResults.slice(
    currentSearchPage * searchPageSize,
    currentSearchPage * searchPageSize + searchPageSize
  );

  useEffect(() => {
    const bookId = new URLSearchParams(window.location.search).get("bookId");
    if (!bookId) {
      return;
    }
    apiGet<BookDetailResponse>(`/books/${bookId}`)
      .then((result) => {
        if (!result.success) {
          setSearchMessage(result.error?.message ?? "선택한 책 정보를 불러오지 못했습니다.");
          return;
        }
        setSelectedBook(result.data.book);
        setQuery(result.data.book.title);
        setSavedRecord(null);
        setRecordError(null);
      })
      .catch(() => setSearchMessage("선택한 책 정보를 불러오지 못했습니다."));
  }, []);

  async function searchBooks(event: FormEvent) {
    event.preventDefault();
    setSearchMessage(null);
    const result = await apiGetBookSearch(query);
    if (!result.success) {
      setSearchMessage(result.error?.message ?? "책을 검색하지 못했습니다.");
      return;
    }
    setSearchResults(result.data);
    setSearchResultPage(0);
    setSelectedSearchKey(null);
    if (result.data.length === 0) {
      setSearchMessage("검색 결과가 없습니다. 직접 책 정보를 입력해서 계속할 수 있습니다.");
      setManualOpen(true);
    }
  }

  async function importBook(result: BookSearchResult) {
    const response = await apiPost<BookSummary>("/books/import", result);
    if (!response.success) {
      setSearchMessage(response.error?.message ?? "책을 저장하지 못했습니다.");
      return;
    }
    setSelectedBook(response.data);
    setSelectedSearchKey(searchResultKey(result));
    setSavedRecord(null);
    setSearchMessage(null);
    setRecordError(null);
  }

  async function createManualBook(event: FormEvent) {
    event.preventDefault();
    if (manualSubmitting) {
      return;
    }
    setManualSubmitting(true);
    setManualError(null);
    const thumbnailUrl = await uploadManualBookCover();
    if (thumbnailUrl === undefined) {
      setManualSubmitting(false);
      return;
    }
    const response = await apiPost<BookSummary>("/books/manual", { ...manualBook, thumbnailUrl });
    if (!response.success) {
      setManualError(response.error?.message ?? "직접 등록 책을 저장하지 못했습니다.");
      setManualSubmitting(false);
      return;
    }
    setSelectedBook(response.data);
    setSavedRecord(null);
    setSearchMessage(null);
    setRecordError(null);
    setManualOpen(false);
    setManualSubmitting(false);
  }

  async function uploadManualBookCover() {
    if (!manualBookCoverFile) {
      return null;
    }
    const result = await uploadImage(manualBookCoverFile, "BOOK");
    if (!result.success) {
      setManualError(result.error?.message ?? "책 표지를 업로드하지 못했습니다.");
      return undefined;
    }
    return result.data.url;
  }

  async function createRecord(event: FormEvent) {
    event.preventDefault();
    if (recordSubmitting) {
      return;
    }
    setRecordSubmitting(true);
    setRecordError(null);
    if (!selectedBook) {
      setRecordError("책을 먼저 선택해 주세요.");
      setRecordSubmitting(false);
      return;
    }
    if (!record.rating) {
      setRecordError("평점을 선택해 주세요.");
      setRecordSubmitting(false);
      return;
    }
    const imageId = await uploadRecordImage();
    if (imageId === undefined) {
      setRecordSubmitting(false);
      return;
    }
    const response = await apiPost<ReadingRecord>("/reading-records", {
      bookId: selectedBook.id,
      rating: Number(record.rating),
      oneLineReview: record.oneLineReview,
      blogUrl: record.blogUrl,
      imageId,
    });
    if (!response.success) {
      setRecordError(response.error?.message ?? "독서기록을 저장하지 못했습니다.");
      setRecordSubmitting(false);
      return;
    }
    setSavedRecord(response.data);
    setRecordSubmitting(false);
  }

  async function uploadRecordImage() {
    if (!recordImageFile) {
      return null;
    }
    const result = await uploadImage(recordImageFile, "READING_RECORD");
    if (!result.success) {
      setRecordError(result.error?.message ?? "독서기록 이미지를 업로드하지 못했습니다.");
      return undefined;
    }
    return result.data.imageId;
  }

  return (
    <AuthGate required="MEMBER">
      {() => (
        <main>
          <Section>
            <div className="grid gap-5 sm:gap-8">
              <MobileBackButton fallbackHref="/library" />
              <PageHeader
                eyebrow="Reading Record"
                title="독서기록 작성"
                description="읽은 책을 찾고, 한 문장과 필요한 링크만 가볍게 남깁니다."
              />

              <Card>
                <div className="grid gap-5 sm:gap-8">
                  <section>
                    <h2 className="font-display text-xl font-normal sm:text-2xl">오늘 남길 책</h2>
                    <p className="mt-2 hidden text-sm leading-6 text-[var(--color-muted)] sm:block">책을 먼저 선택하면 아래에 짧은 기록지가 열립니다.</p>
                    <form className="mt-4 flex flex-col gap-2 sm:mt-5 sm:flex-row sm:gap-3" onSubmit={searchBooks}>
                      <input
                        className="min-h-10 flex-1 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 sm:min-h-11"
                        onChange={(event) => setQuery(event.target.value)}
                        placeholder="책 제목으로 검색"
                        value={query}
                      />
                      <Button type="submit">책 찾기</Button>
                    </form>
                    {searchMessage && <FieldMessage>{searchMessage}</FieldMessage>}

                    {searchResults.length > 0 && (
                      <div className="mt-5 grid gap-3">
                        {visibleSearchResults.map((result) => {
                          const selected = selectedSearchKey === searchResultKey(result);
                          return (
                            <button
                              aria-pressed={selected}
                              className={[
                                "grid grid-cols-[56px_1fr] gap-3 rounded-[var(--radius-card)] border bg-[var(--color-warm-white)] p-3 text-left transition sm:grid-cols-[72px_1fr] sm:gap-4 sm:p-4",
                                selected
                                  ? "border-[var(--color-deep-green)] shadow-[0_14px_30px_rgba(31,77,58,0.12)]"
                                  : "border-[var(--color-line)] hover:border-[var(--color-deep-green)]",
                              ].join(" ")}
                              key={searchResultKey(result)}
                              onClick={() => importBook(result)}
                              type="button"
                            >
                              <BookCover alt={`${result.title} 표지`} src={result.thumbnailUrl} />
                              <span className="min-w-0">
                                <span className="flex items-start justify-between gap-2">
                                  <span className="block min-w-0 font-normal text-[var(--color-ink)]">{result.title}</span>
                                  {selected && <span className="shrink-0 rounded-full bg-[rgba(31,77,58,0.08)] px-2 py-0.5 text-[10px] text-[var(--color-deep-green)]">선택됨</span>}
                                </span>
                                <span className="mt-1 block text-sm leading-6 text-[var(--color-charcoal)]">{result.authorsText}</span>
                                <span className="mt-1 block text-xs text-[var(--color-muted)]">{[result.publisher, result.publishedDate].filter(Boolean).join(" · ")}</span>
                              </span>
                            </button>
                          );
                        })}
                        {searchResults.length > searchPageSize && (
                          <div className="flex items-center justify-between border-t border-[var(--color-line)] pt-3 text-xs text-[var(--color-muted)]">
                            <button
                              className="inline-flex min-h-9 items-center rounded-full border border-[rgba(31,77,58,0.26)] bg-[var(--color-warm-white)] px-3 text-[var(--color-deep-green)] transition hover:border-[var(--color-deep-green)] hover:bg-[rgba(31,77,58,0.06)] disabled:border-[var(--color-line)] disabled:bg-transparent disabled:text-[var(--color-muted)]"
                              disabled={currentSearchPage === 0}
                              onClick={() => setSearchResultPage((page) => Math.max(0, page - 1))}
                              type="button"
                            >
                              이전
                            </button>
                            <span className="rounded-full bg-[rgba(31,77,58,0.06)] px-3 py-1 text-[var(--color-deep-green)]">{currentSearchPage + 1} / {searchPageCount}</span>
                            <button
                              className="inline-flex min-h-9 items-center rounded-full border border-[rgba(31,77,58,0.26)] bg-[var(--color-warm-white)] px-3 text-[var(--color-deep-green)] transition hover:border-[var(--color-deep-green)] hover:bg-[rgba(31,77,58,0.06)] disabled:border-[var(--color-line)] disabled:bg-transparent disabled:text-[var(--color-muted)]"
                              disabled={currentSearchPage >= searchPageCount - 1}
                              onClick={() => setSearchResultPage((page) => Math.min(searchPageCount - 1, page + 1))}
                              type="button"
                            >
                              다음
                            </button>
                          </div>
                        )}
                      </div>
                    )}
                  </section>

                  {selectedBook && !savedRecord && (
                    <section className="rounded-[var(--radius-card)] border border-[rgba(47,90,67,0.18)] bg-[linear-gradient(135deg,rgba(47,90,67,0.07),rgba(255,254,250,0.86))] p-4 sm:p-5">
                      <div className="grid grid-cols-[72px_1fr] gap-3 sm:grid-cols-[96px_1fr] sm:items-center sm:gap-4">
                        <BookCover alt={`${selectedBook.title} 표지`} size="large" src={selectedBook.thumbnailUrl} />
                        <div className="min-w-0">
                          <div className="flex flex-wrap items-center gap-2">
                            <span className="rounded-full border border-[rgba(47,90,67,0.18)] bg-[rgba(255,254,250,0.72)] px-3 py-1 text-xs text-[var(--color-deep-green)]">선택한 책</span>
                            <span className="text-xs text-[var(--color-muted)]">{selectedBook.status === "VERIFIED" ? "확인된 책" : "직접 등록된 책"}</span>
                          </div>
                          <h3 className="mt-2 line-clamp-2 font-display text-lg font-normal leading-snug text-[var(--color-ink)] sm:mt-3 sm:text-2xl">{selectedBook.title}</h3>
                          <p className="mt-1 line-clamp-1 text-sm leading-6 text-[var(--color-charcoal)] sm:mt-2 sm:line-clamp-none">
                            {selectedBook.authorsText}
                            {selectedBook.publisher ? ` · ${selectedBook.publisher}` : ""}
                          </p>
                          {selectedBook.publishedDate && <p className="mt-1 text-xs text-[var(--color-muted)]">{selectedBook.publishedDate}</p>}
                          <p className="mt-4 hidden text-sm leading-6 text-[var(--color-muted)] sm:block">이 책에 남길 한 문장과 평점을 아래에 기록합니다.</p>
                        </div>
                      </div>
                    </section>
                  )}

                  {!selectedBook && (
                    <div className="rounded-[var(--radius-card)] border border-dashed border-[var(--color-line)] bg-[rgba(255,254,250,0.66)] p-4 text-sm leading-6 text-[var(--color-muted)] sm:p-5">
                      책을 선택하면 한줄평, 평점, 블로그 원문을 남길 수 있습니다.
                    </div>
                  )}

                  {savedRecord && (
                    <section className="rounded-[var(--radius-card)] border border-[rgba(47,90,67,0.2)] bg-[rgba(255,254,250,0.9)] p-5 shadow-[var(--shadow-soft)]">
                      <p className="font-latin text-2xl leading-none text-[var(--color-bronze)] sm:text-3xl">Archived</p>
                      <h2 className="mt-2 font-display text-xl font-normal leading-snug sm:mt-3 sm:text-2xl">기록이 보관되었습니다.</h2>
                      <p className="mt-3 text-sm leading-7 text-[var(--color-charcoal)]">{savedRecord.oneLineReview}</p>
                      <div className="mt-5 flex flex-wrap gap-3">
                        <Button href={`/books/${savedRecord.bookId}`}>책 상세 보기</Button>
                        <Button href="/library" variant="secondary">라이브러리 보기</Button>
                        <Button href="/reading-records/new" variant="secondary">하나 더 남기기</Button>
                      </div>
                    </section>
                  )}

                  {selectedBook && !savedRecord && (
                    <section>
                      <div className="mb-4">
                        <h2 className="font-display text-xl font-normal sm:text-2xl">오늘의 짧은 기록</h2>
                        <p className="mt-2 hidden text-sm leading-6 text-[var(--color-muted)] sm:block">한 문장만 남겨도 충분합니다. 평점과 원문 링크는 필요한 만큼만 더해 주세요.</p>
                      </div>
                      <form className="grid gap-4" onSubmit={createRecord}>
                        <label className="grid gap-2 text-sm text-[var(--color-charcoal)]">
                          이 책을 읽고 남기고 싶은 한 문장
                          <input
                            className="min-h-10 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 sm:min-h-12"
                            maxLength={255}
                            onChange={(event) => setRecord((current) => ({ ...current, oneLineReview: event.target.value }))}
                            placeholder="예: 다시 읽고 싶은 문장이 많은 책이었어요."
                            required
                            value={record.oneLineReview}
                          />
                        </label>
                        <div className="grid items-start gap-4 sm:grid-cols-[160px_1fr]">
                          <label className="grid gap-2 text-sm text-[var(--color-charcoal)]">
                            평점
                            <select
                              className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
                              onChange={(event) => setRecord((current) => ({ ...current, rating: event.target.value }))}
                              required
                              value={record.rating}
                            >
                              <option disabled value="">평점 선택</option>
                              <option value="1">1</option>
                              <option value="2">2</option>
                              <option value="3">3</option>
                              <option value="4">4</option>
                              <option value="5">5</option>
                            </select>
                          </label>
                          <label className="grid gap-2 text-sm text-[var(--color-charcoal)]">
                            블로그 원문
                            <input
                              className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
                              onChange={(event) => {
                                setRecord((current) => ({ ...current, blogUrl: event.target.value }));
                                if (recordError?.includes("블로그 URL")) {
                                  setRecordError(null);
                                }
                              }}
                              placeholder="https://blog.example.com/post"
                              value={record.blogUrl}
                            />
                            {recordError?.includes("블로그 URL") && <FieldMessage>{recordError}</FieldMessage>}
                          </label>
                        </div>
                        <div className="grid gap-2 text-sm text-[var(--color-charcoal)]">
                          <span>
                            대표 이미지 <span className="text-xs text-[var(--color-muted)]">선택</span>
                          </span>
                          <div className="flex flex-wrap items-center gap-3">
                            <label className={imagePickerButtonClass(recordImageFile)}>
                              {recordImageFile ? "이미지 변경" : "이미지 선택"}
                              <input
                                accept="image/jpeg,image/png,image/webp,image/heic,image/heif,.heic,.heif"
                                className="sr-only"
                                onChange={(event) => setRecordImageFile(event.target.files?.[0] ?? null)}
                                type="file"
                              />
                            </label>
                            <SelectedImageState file={recordImageFile} emptyText="선택된 이미지 없음" />
                          </div>
                          <span className="hidden text-xs text-[var(--color-muted)] sm:inline">책 표지, 밑줄, 노트 사진을 선택으로 남길 수 있습니다.</span>
                        </div>
                        {recordError && !recordError.includes("블로그 URL") && <FieldMessage>{recordError}</FieldMessage>}
                        <Button type="submit">{recordSubmitting ? "이미지 압축 및 저장 중" : "기록 남기기"}</Button>
                      </form>
                    </section>
                  )}

                  <section className="border-t border-[var(--color-line)] pt-5">
                    <button
                      className="text-xs font-normal text-[var(--color-deep-green)] underline-offset-4 hover:underline"
                      onClick={() => setManualOpen((value) => !value)}
                      type="button"
                    >
                      찾는 책이 없나요? 직접 등록
                    </button>
                    {manualOpen && (
                      <form className="mt-4 grid gap-3" onSubmit={createManualBook}>
                        <div className="grid gap-3 sm:grid-cols-3">
                          <input
                            className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
                            onChange={(event) => setManualBook((current) => ({ ...current, title: event.target.value }))}
                            placeholder="책 제목"
                            value={manualBook.title}
                          />
                          <input
                            className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
                            onChange={(event) => setManualBook((current) => ({ ...current, author: event.target.value }))}
                            placeholder="저자"
                            value={manualBook.author}
                          />
                          <input
                            className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
                            onChange={(event) => setManualBook((current) => ({ ...current, publisher: event.target.value }))}
                            placeholder="출판사"
                            value={manualBook.publisher}
                          />
                        </div>
                        <div className="flex flex-wrap items-center gap-3">
                          <label className={imagePickerButtonClass(manualBookCoverFile)}>
                            {manualBookCoverFile ? "책 표지 변경" : "책 표지 선택"}
                            <input
                              accept="image/jpeg,image/png,image/webp,image/heic,image/heif,.heic,.heif"
                              className="sr-only"
                              onChange={(event) => setManualBookCoverFile(event.target.files?.[0] ?? null)}
                              type="file"
                            />
                          </label>
                          <SelectedImageState file={manualBookCoverFile} emptyText="표지 이미지 없음" />
                        </div>
                        <div>
                          <Button type="submit" variant="secondary">
                            {manualSubmitting ? "표지 압축 및 저장 중" : "직접 등록하고 선택"}
                          </Button>
                        </div>
                        {manualError && <FieldMessage>{manualError}</FieldMessage>}
                      </form>
                    )}
                  </section>
                </div>
              </Card>
            </div>
          </Section>
        </main>
      )}
    </AuthGate>
  );
}

function apiGetBookSearch(query: string) {
  return apiGet<BookSearchResult[]>(`/books/search?query=${encodeURIComponent(query)}&page=0&size=10`);
}

function searchResultKey(result: BookSearchResult) {
  return result.isbn13 || result.isbn10 || `${result.title}-${result.publisher ?? ""}-${result.publishedDate ?? ""}`;
}

function FieldMessage({ children }: { children: string }) {
  return <p className="mt-2 text-xs leading-5 text-[#9f3f2f]">{children}</p>;
}

function imagePickerButtonClass(file: File | null) {
  return [
    "inline-flex min-h-10 cursor-pointer items-center justify-center rounded-[var(--radius-card)] border px-4 py-2 text-sm font-normal transition",
    file
      ? "border-[var(--color-deep-green)] bg-[rgba(31,77,58,0.08)] text-[var(--color-deep-green)]"
      : "border-[var(--color-line)] bg-[var(--color-warm-white)] text-[var(--color-charcoal)] hover:border-[var(--color-deep-green)] hover:text-[var(--color-deep-green)]",
  ].join(" ");
}

function SelectedImageState({ emptyText, file }: { emptyText: string; file: File | null }) {
  if (!file) {
    return <span className="text-xs text-[var(--color-muted)]">{emptyText}</span>;
  }
  return (
    <span className="inline-flex min-h-9 max-w-full items-center gap-2 rounded-full border border-[rgba(31,77,58,0.22)] bg-[rgba(31,77,58,0.06)] px-3 text-xs text-[var(--color-deep-green)]">
      <span className="shrink-0 rounded-full bg-[var(--color-deep-green)] px-2 py-0.5 text-[10px] text-[var(--color-warm-white)]">선택됨</span>
      <span className="truncate">{file.name}</span>
    </span>
  );
}

function BookCover({ alt, src, size = "default" }: { alt: string; src: string | null; size?: "default" | "large" }) {
  return (
    <div className={`${size === "large" ? "w-[72px] sm:w-[96px]" : "w-[56px] sm:w-[72px]"} overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-line)] shadow-[0_12px_28px_rgba(63,47,34,0.09)]`}>
      <Image
        alt={alt}
        className="aspect-[3/4] w-full object-cover photo-muted"
        height={size === "large" ? 128 : 96}
        onError={(event) => {
          event.currentTarget.src = "/images/reading-books.jpg";
        }}
        src={src || "/images/reading-books.jpg"}
        unoptimized
        width={size === "large" ? 96 : 72}
      />
    </div>
  );
}
