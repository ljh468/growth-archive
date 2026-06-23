"use client";

import { FormEvent, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiGet, apiPost, type BookSearchResult, type BookSummary, type ReadingRecord } from "@/lib/api";

export default function NewReadingRecordPage() {
  const [query, setQuery] = useState("");
  const [searchResults, setSearchResults] = useState<BookSearchResult[]>([]);
  const [selectedBook, setSelectedBook] = useState<BookSummary | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [manualBook, setManualBook] = useState({ title: "", author: "", publisher: "" });
  const [record, setRecord] = useState({ rating: "", oneLineReview: "", blogUrl: "" });

  async function searchBooks(event: FormEvent) {
    event.preventDefault();
    setMessage(null);
    const result = await apiGetBookSearch(query);
    if (!result.success) {
      setMessage(result.error?.message ?? "책을 검색하지 못했습니다.");
      return;
    }
    setSearchResults(result.data);
    if (result.data.length === 0) {
      setMessage("검색 결과가 없습니다. 직접 책 정보를 입력해서 계속할 수 있습니다.");
    }
  }

  async function importBook(result: BookSearchResult) {
    const response = await apiPost<BookSummary>("/books/import", result);
    if (!response.success) {
      setMessage(response.error?.message ?? "책을 저장하지 못했습니다.");
      return;
    }
    setSelectedBook(response.data);
    setMessage("책이 선택되었습니다.");
  }

  async function createManualBook(event: FormEvent) {
    event.preventDefault();
    const response = await apiPost<BookSummary>("/books/manual", manualBook);
    if (!response.success) {
      setMessage(response.error?.message ?? "직접 등록 책을 저장하지 못했습니다.");
      return;
    }
    setSelectedBook(response.data);
    setMessage("UNVERIFIED 책이 생성되어 선택되었습니다.");
  }

  async function createRecord(event: FormEvent) {
    event.preventDefault();
    if (!selectedBook) {
      setMessage("책을 먼저 선택해 주세요.");
      return;
    }
    const response = await apiPost<ReadingRecord>("/reading-records", {
      bookId: selectedBook.id,
      rating: record.rating ? Number(record.rating) : null,
      oneLineReview: record.oneLineReview,
      blogUrl: record.blogUrl,
    });
    if (!response.success) {
      setMessage(response.error?.message ?? "독서기록을 저장하지 못했습니다.");
      return;
    }
    window.location.href = `/books/${response.data.bookId}`;
  }

  return (
    <AuthGate required="MEMBER">
      {() => (
        <main>
          <Section>
            <div className="grid gap-8">
              <PageHeader
                eyebrow="Reading Record"
                title="독서기록 작성"
                description="책 검색이 실패해도 직접 등록으로 기록을 계속 남길 수 있습니다."
              />
              {message && <EmptyState title="상태" description={message} />}

              <Card>
                <h2 className="text-xl font-semibold">1. 책 검색</h2>
                <form className="mt-4 flex flex-col gap-3 sm:flex-row" onSubmit={searchBooks}>
                  <input
                    className="min-h-11 flex-1 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
                    onChange={(event) => setQuery(event.target.value)}
                    placeholder="책 제목"
                    value={query}
                  />
                  <Button type="submit">카카오 검색</Button>
                </form>
                <div className="mt-5 grid gap-3">
                  {searchResults.map((result) => (
                    <button
                      className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4 text-left"
                      key={`${result.title}-${result.isbn13 ?? result.isbn10 ?? result.publisher}`}
                      onClick={() => importBook(result)}
                      type="button"
                    >
                      <span className="font-semibold">{result.title}</span>
                      <span className="mt-1 block text-sm text-[var(--color-charcoal)]">{result.authorsText}</span>
                    </button>
                  ))}
                </div>
              </Card>

              <Card>
                <h2 className="text-xl font-semibold">2. 직접 책 등록</h2>
                <form className="mt-4 grid gap-3" onSubmit={createManualBook}>
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
                    placeholder="출판사 선택"
                    value={manualBook.publisher}
                  />
                  <Button type="submit" variant="secondary">
                    UNVERIFIED 책 만들기
                  </Button>
                </form>
              </Card>

              <Card>
                <div className="flex flex-wrap items-center gap-3">
                  <h2 className="text-xl font-semibold">3. 기록 저장</h2>
                  {selectedBook && <Tag>{selectedBook.title}</Tag>}
                </div>
                <form className="mt-4 grid gap-3" onSubmit={createRecord}>
                  <select
                    className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
                    onChange={(event) => setRecord((current) => ({ ...current, rating: event.target.value }))}
                    value={record.rating}
                  >
                    <option value="">평점 없음</option>
                    <option value="1">1</option>
                    <option value="2">2</option>
                    <option value="3">3</option>
                    <option value="4">4</option>
                    <option value="5">5</option>
                  </select>
                  <input
                    className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
                    onChange={(event) => setRecord((current) => ({ ...current, oneLineReview: event.target.value }))}
                    placeholder="한줄평"
                    value={record.oneLineReview}
                  />
                  <input
                    className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
                    onChange={(event) => setRecord((current) => ({ ...current, blogUrl: event.target.value }))}
                    placeholder="https://blog.example.com/post"
                    value={record.blogUrl}
                  />
                  <Button type="submit">독서기록 저장</Button>
                </form>
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
