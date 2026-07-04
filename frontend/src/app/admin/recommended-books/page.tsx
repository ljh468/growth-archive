"use client";

import { type Dispatch, type FormEvent, type SetStateAction, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiGet, apiPost, apiPut, type BookSearchResult, type BookSummary, type RecommendedBook } from "@/lib/api";

const defaultTargetMonth = `${new Date().getFullYear()}-${String(new Date().getMonth() + 1).padStart(2, "0")}-01`;

export default function AdminRecommendedBooksPage() {
  return (
    <AuthGate required="ADMIN">
      {() => <AdminRecommendedBooksContent />}
    </AuthGate>
  );
}

function AdminRecommendedBooksContent() {
  const [books, setBooks] = useState<RecommendedBook[]>([]);
  const [selected, setSelected] = useState<RecommendedBook | null>(null);
  const [form, setForm] = useState({ targetMonth: defaultTargetMonth, bookId: "", reason: "", displayOrder: "1" });
  const [bookQuery, setBookQuery] = useState("");
  const [bookResults, setBookResults] = useState<BookSearchResult[]>([]);
  const [selectedBook, setSelectedBook] = useState<BookSummary | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [bookSearchLoading, setBookSearchLoading] = useState(false);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    setLoading(true);
    const result = await apiGet<RecommendedBook[]>("/admin/recommended-books");
    if (!result.success) {
      setMessage(result.error?.message ?? "추천책을 불러오지 못했습니다.");
      setLoading(false);
      return;
    }
    setBooks(result.data);
    setLoading(false);
  }

  function choose(book: RecommendedBook) {
    if (selected?.id === book.id) {
      reset();
      return;
    }
    setSelected(book);
    setForm({ targetMonth: book.targetMonth, bookId: String(book.bookId), reason: book.reason, displayOrder: String(book.displayOrder) });
    setSelectedBook({
      id: book.bookId,
      title: book.title,
      authorsText: book.authorsText,
      publisher: book.publisher,
      publishedDate: null,
      thumbnailUrl: book.thumbnailUrl,
      status: "VERIFIED",
    });
  }

  function reset() {
    setSelected(null);
    setForm({ targetMonth: defaultTargetMonth, bookId: "", reason: "", displayOrder: "1" });
    setSelectedBook(null);
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!form.bookId) {
      setMessage("추천책으로 사용할 책을 먼저 선택해 주세요.");
      return;
    }
    const payload = {
      targetMonth: form.targetMonth,
      bookId: Number(form.bookId),
      reason: form.reason,
      displayOrder: Number(form.displayOrder),
    };
    const result = selected
      ? await apiPut<RecommendedBook>(`/admin/recommended-books/${selected.id}`, payload)
      : await apiPost<RecommendedBook>("/admin/recommended-books", payload);
    setMessage(result.success ? "추천책이 저장되었습니다." : result.error?.message ?? "추천책을 저장하지 못했습니다.");
    if (result.success) {
      reset();
      await load();
    }
  }

  async function hide(id: number) {
    const result = await apiPost<void>(`/admin/recommended-books/${id}/hide`);
    setMessage(result.success ? "추천책을 비활성화했습니다." : result.error?.message ?? "추천책을 비활성화하지 못했습니다.");
    if (result.success) {
      reset();
      await load();
    }
  }

  async function restore(id: number) {
    const result = await apiPost<void>(`/admin/recommended-books/${id}/restore`);
    setMessage(result.success ? "추천책을 복구했습니다." : result.error?.message ?? "추천책을 복구하지 못했습니다.");
    if (result.success) {
      reset();
      await load();
    }
  }

  async function searchBooks(event: FormEvent) {
    event.preventDefault();
    const query = bookQuery.trim();
    if (!query) {
      setMessage("검색할 책 제목을 입력해 주세요.");
      return;
    }
    setBookSearchLoading(true);
    const result = await apiGet<BookSearchResult[]>(`/books/search?query=${encodeURIComponent(query)}&page=0&size=5`);
    if (!result.success) {
      setMessage(result.error?.message ?? "책 검색에 실패했습니다.");
      setBookSearchLoading(false);
      return;
    }
    setBookResults(result.data);
    setBookSearchLoading(false);
  }

  async function selectSearchResult(result: BookSearchResult) {
    const imported = await apiPost<BookSummary>("/books/import", result);
    if (!imported.success) {
      setMessage(imported.error?.message ?? "책을 선택하지 못했습니다.");
      return;
    }
    setSelectedBook(imported.data);
    setForm((current) => ({ ...current, bookId: String(imported.data.id) }));
    setBookResults([]);
    setMessage("추천책으로 사용할 책을 선택했습니다.");
  }

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <PageHeader eyebrow="Admin" title="추천책 관리" description="라이브러리에 노출할 추천책을 등록하고 순서, 문구, 노출 상태를 관리합니다." />
          {message && <EmptyState title="처리 결과" description={message} />}
          <div className="grid gap-5">
            <div className="grid gap-3">
              {loading && <EmptyState title="불러오는 중입니다." description="추천책 목록을 확인하고 있습니다." />}
              {books.map((book) => (
                <div
                  className={[
                    "grid gap-3",
                    selected?.id === book.id ? "lg:grid-cols-[minmax(0,1fr)_360px] lg:items-start" : "",
                  ].join(" ")}
                  key={book.id}
                >
                  <div
                    className={[
                      "flex flex-wrap items-start justify-between gap-3 border bg-[var(--color-warm-white)] p-4 transition",
                      selected?.id === book.id ? "border-[var(--color-deep-green)] shadow-[var(--shadow-soft)]" : "border-[var(--color-line)]",
                    ].join(" ")}
                  >
                    <button className="min-w-0 flex-1 text-left" onClick={() => choose(book)} type="button">
                      <div className="flex flex-wrap gap-2">
                        <Tag>#{book.displayOrder}</Tag>
                        <Tag>Book {book.bookId}</Tag>
                        <Tag>{book.status === "ACTIVE" ? "노출 중" : "비활성"}</Tag>
                      </div>
                      <p className="mt-3 font-normal">{book.title}</p>
                      <p className="mt-1 text-sm text-[var(--color-charcoal)]">{book.authorsText}</p>
                      <p className="mt-3 text-sm leading-6">{book.reason}</p>
                    </button>
                    {book.status === "HIDDEN" ? (
                      <button className="archive-record-button" onClick={() => restore(book.id)} type="button">
                        복구
                      </button>
                    ) : (
                      <button className="archive-record-button archive-record-button--danger" onClick={() => hide(book.id)} type="button">
                        비활성화
                      </button>
                    )}
                  </div>
                  {selected?.id === book.id && (
                    <RecommendedBookEditor
                      bookQuery={bookQuery}
                      bookResults={bookResults}
                      bookSearchLoading={bookSearchLoading}
                      form={form}
                      reset={reset}
                      searchBooks={searchBooks}
                      selectSearchResult={selectSearchResult}
                      selected={selected}
                      selectedBook={selectedBook}
                      setBookQuery={setBookQuery}
                      setForm={setForm}
                      submit={submit}
                    />
                  )}
                </div>
              ))}
              {!loading && books.length < 3 && <EmptyState title="추천책 권장 수량" description="추천책은 3~5권 정도로 유지하면 라이브러리 첫 화면이 가장 안정적입니다." />}
            </div>
          </div>
        </div>
      </Section>
    </main>
  );
}

function RecommendedBookEditor({
  bookQuery,
  bookResults,
  bookSearchLoading,
  form,
  reset,
  searchBooks,
  selectSearchResult,
  selected,
  selectedBook,
  setBookQuery,
  setForm,
  submit,
}: {
  bookQuery: string;
  bookResults: BookSearchResult[];
  bookSearchLoading: boolean;
  form: { targetMonth: string; bookId: string; reason: string; displayOrder: string };
  reset: () => void;
  searchBooks: (event: FormEvent) => Promise<void>;
  selectSearchResult: (result: BookSearchResult) => Promise<void>;
  selected: RecommendedBook | null;
  selectedBook: BookSummary | null;
  setBookQuery: (value: string) => void;
  setForm: Dispatch<SetStateAction<{ targetMonth: string; bookId: string; reason: string; displayOrder: string }>>;
  submit: (event: FormEvent) => Promise<void>;
}) {
  return (
    <Card>
      <div className="grid gap-5">
        <form className="grid gap-3" onSubmit={searchBooks}>
          <h2 className="text-lg font-normal">책 검색</h2>
          <input
            className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
            onChange={(event) => setBookQuery(event.target.value)}
            placeholder="카카오 책 검색"
            value={bookQuery}
          />
          <div>
            <Button type="submit">{bookSearchLoading ? "검색 중" : "책 찾기"}</Button>
          </div>
        </form>
        {bookResults.length > 0 && (
          <div className="grid gap-2">
            {bookResults.map((book) => (
              <button
                className="border border-[var(--color-line)] bg-[rgba(255,254,250,0.72)] p-3 text-left transition hover:border-[var(--color-deep-green)]"
                key={`${book.isbn13 ?? book.isbn10 ?? book.title}-${book.authorsText}`}
                onClick={() => selectSearchResult(book)}
                type="button"
              >
                <p className="text-sm font-normal">{book.title}</p>
                <p className="mt-1 text-xs leading-5 text-[var(--color-charcoal)]">{book.authorsText}</p>
              </button>
            ))}
          </div>
        )}
      </div>
      <form className="mt-6 grid gap-3 border-t border-[var(--color-line)] pt-5" onSubmit={submit}>
        <h2 className="text-lg font-normal">{selected ? "추천책 수정" : "추천책 등록"}</h2>
        {selectedBook ? (
          <div className="border border-[rgba(229,222,209,0.82)] bg-[rgba(255,254,250,0.58)] p-3">
            <p className="text-xs text-[var(--color-bronze)]">선택한 책 · Book {selectedBook.id}</p>
            <p className="mt-1 text-sm font-normal">{selectedBook.title}</p>
            <p className="mt-1 text-xs text-[var(--color-charcoal)]">{selectedBook.authorsText}</p>
          </div>
        ) : (
          <p className="text-sm leading-6 text-[var(--color-muted)]">위에서 책을 검색하고 선택해 주세요.</p>
        )}
        <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setForm((current) => ({ ...current, displayOrder: event.target.value }))} placeholder="노출 순서 1~5" type="number" value={form.displayOrder} />
        <textarea className="min-h-28 border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3" onChange={(event) => setForm((current) => ({ ...current, reason: event.target.value }))} placeholder="추천 이유" value={form.reason} />
        <div className="flex flex-wrap gap-3">
          <Button type="submit">{selected ? "수정" : "등록"}</Button>
          <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-normal" onClick={reset} type="button">
            새 추천책
          </button>
        </div>
      </form>
    </Card>
  );
}
