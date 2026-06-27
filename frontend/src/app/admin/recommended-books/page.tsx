"use client";

import { FormEvent, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiDelete, apiGet, apiPost, apiPut, type RecommendedBook } from "@/lib/api";

export default function AdminRecommendedBooksPage() {
  return (
    <AuthGate required="ADMIN">
      {() => <AdminRecommendedBooksContent />}
    </AuthGate>
  );
}

function AdminRecommendedBooksContent() {
  const [month, setMonth] = useState(new Date().toISOString().slice(0, 7));
  const [books, setBooks] = useState<RecommendedBook[]>([]);
  const [selected, setSelected] = useState<RecommendedBook | null>(null);
  const [form, setForm] = useState({ targetMonth: new Date().toISOString().slice(0, 7) + "-01", bookId: "", reason: "", displayOrder: "1" });
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [month]);

  async function load() {
    const result = await apiGet<RecommendedBook[]>(`/admin/recommended-books?month=${month}`);
    if (!result.success) {
      setMessage(result.error?.message ?? "추천책을 불러오지 못했습니다.");
      return;
    }
    setBooks(result.data);
  }

  function choose(book: RecommendedBook) {
    setSelected(book);
    setForm({ targetMonth: `${month}-01`, bookId: String(book.bookId), reason: book.reason, displayOrder: String(book.displayOrder) });
  }

  function reset() {
    setSelected(null);
    setForm({ targetMonth: `${month}-01`, bookId: "", reason: "", displayOrder: "1" });
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
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

  async function remove(id: number) {
    const result = await apiDelete<void>(`/admin/recommended-books/${id}`);
    setMessage(result.success ? "추천책이 삭제되었습니다." : result.error?.message ?? "추천책을 삭제하지 못했습니다.");
    if (result.success) {
      reset();
      await load();
    }
  }

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <PageHeader eyebrow="Admin" title="이달의 추천책 관리" description="기존 Book ID를 기준으로 월별 추천책 3~5권을 관리합니다." />
          {message && <EmptyState title="처리 결과" description={message} />}
          <label className="grid max-w-xs gap-2 text-sm">
            대상 월
            <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setMonth(event.target.value)} type="month" value={month} />
          </label>
          <div className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_360px]">
            <div className="grid gap-3">
              {books.map((book) => (
                <div className="flex flex-wrap items-start justify-between gap-3 border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4" key={book.id}>
                  <button className="text-left" onClick={() => choose(book)} type="button">
                    <div className="flex flex-wrap gap-2">
                      <Tag>#{book.displayOrder}</Tag>
                      <Tag>Book {book.bookId}</Tag>
                    </div>
                    <p className="mt-3 font-normal">{book.title}</p>
                    <p className="mt-1 text-sm text-[var(--color-charcoal)]">{book.authorsText}</p>
                    <p className="mt-3 text-sm leading-6">{book.reason}</p>
                  </button>
                  <button className="inline-flex min-h-10 items-center border border-[var(--color-line)] px-3 text-sm font-normal" onClick={() => remove(book.id)} type="button">
                    삭제
                  </button>
                </div>
              ))}
              {books.length < 3 && <EmptyState title="추천책 권장 수량" description="이달의 추천책은 3~5권 등록을 권장합니다." />}
            </div>
            <Card>
              <form className="grid gap-3" onSubmit={submit}>
                <h2 className="text-lg font-normal">{selected ? "추천책 수정" : "추천책 등록"}</h2>
                <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setForm((current) => ({ ...current, targetMonth: event.target.value }))} type="date" value={form.targetMonth} />
                <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setForm((current) => ({ ...current, bookId: event.target.value }))} placeholder="Book ID" value={form.bookId} />
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
          </div>
        </div>
      </Section>
    </main>
  );
}
