"use client";

import { type Dispatch, type FormEvent, type SetStateAction, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiGet, apiPost, apiPut, type BookSummary } from "@/lib/api";

type StatusFilter = "UNVERIFIED" | "VERIFIED" | "ALL";

export default function AdminBooksPage() {
  return (
    <AuthGate required="ADMIN">
      {() => <AdminBooksContent />}
    </AuthGate>
  );
}

function AdminBooksContent() {
  const [status, setStatus] = useState<StatusFilter>("UNVERIFIED");
  const [books, setBooks] = useState<BookSummary[]>([]);
  const [selected, setSelected] = useState<BookSummary | null>(null);
  const [form, setForm] = useState({ title: "", authorsText: "", publisher: "", publishedDate: "", thumbnailUrl: "" });
  const [message, setMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status]);

  async function load() {
    setLoading(true);
    const query = status === "ALL" ? "page=0&size=50" : `verificationStatus=${status}&page=0&size=50`;
    const result = await apiGet<BookSummary[]>(`/admin/books?${query}`);
    if (!result.success) {
      setMessage(result.error?.message ?? "책 목록을 불러오지 못했습니다.");
      setLoading(false);
      return;
    }
    setBooks(result.data);
    setLoading(false);
  }

  function choose(book: BookSummary) {
    if (selected?.id === book.id) {
      reset();
      return;
    }
    setSelected(book);
    setForm({
      title: book.title,
      authorsText: book.authorsText,
      publisher: book.publisher ?? "",
      publishedDate: book.publishedDate ?? "",
      thumbnailUrl: book.thumbnailUrl ?? "",
    });
  }

  function reset() {
    setSelected(null);
    setForm({ title: "", authorsText: "", publisher: "", publishedDate: "", thumbnailUrl: "" });
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!selected) {
      setMessage("수정할 책을 선택해 주세요.");
      return;
    }
    const result = await apiPut<BookSummary>(`/admin/books/${selected.id}`, {
      title: form.title,
      authorsText: form.authorsText,
      publisher: form.publisher || null,
      publishedDate: form.publishedDate || null,
      thumbnailUrl: form.thumbnailUrl || null,
    });
    setMessage(result.success ? "책 정보가 저장되었습니다." : result.error?.message ?? "책 정보를 저장하지 못했습니다.");
    if (result.success) {
      setSelected(result.data);
      await load();
    }
  }

  async function verify(book: BookSummary) {
    const result = await apiPost<BookSummary>(`/admin/books/${book.id}/verify`);
    setMessage(result.success ? "책이 검증 완료 처리되었습니다." : result.error?.message ?? "책을 검증 처리하지 못했습니다.");
    if (result.success) {
      reset();
      await load();
    }
  }

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <PageHeader eyebrow="Admin" title="책 검증" description="회원이 직접 등록한 책 정보를 확인하고 필요한 경우 수정한 뒤 검증 완료 처리합니다." />
          {message && <EmptyState title="처리 결과" description={message} />}
          <label className="grid max-w-xs gap-2 text-sm">
            검증 상태
            <select className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setStatus(event.target.value as StatusFilter)} value={status}>
              <option value="UNVERIFIED">검증 필요</option>
              <option value="VERIFIED">검증 완료</option>
              <option value="ALL">전체</option>
            </select>
          </label>
          <div className="grid gap-5">
            <div className="grid gap-3">
              {loading && <EmptyState title="불러오는 중입니다." description="책 목록을 확인하고 있습니다." />}
              {books.map((book) => (
                <div
                  className={[
                    "grid gap-3",
                    selected?.id === book.id ? "lg:grid-cols-[minmax(0,1fr)_380px] lg:items-start" : "",
                  ].join(" ")}
                  key={book.id}
                >
                  <div
                    className={[
                      "flex flex-wrap items-start justify-between gap-4 border bg-[var(--color-warm-white)] p-4 transition",
                      selected?.id === book.id ? "border-[var(--color-deep-green)] shadow-[var(--shadow-soft)]" : "border-[var(--color-line)]",
                    ].join(" ")}
                  >
                    <button className="min-w-0 flex-1 text-left" onClick={() => choose(book)} type="button">
                      <div className="flex flex-wrap gap-2">
                        <Tag>Book {book.id}</Tag>
                        <Tag>{bookStatusLabel(book.status)}</Tag>
                      </div>
                      <p className="mt-3 font-normal">{book.title}</p>
                      <p className="mt-1 text-sm text-[var(--color-charcoal)]">{book.authorsText}</p>
                      <p className="mt-2 text-sm text-[var(--color-charcoal)]">
                        {[book.publisher, book.publishedDate].filter(Boolean).join(" · ") || "출판 정보 없음"}
                      </p>
                    </button>
                    {book.status === "UNVERIFIED" && (
                      <button className="inline-flex min-h-10 items-center border border-[var(--color-line)] px-3 text-sm font-normal" onClick={() => verify(book)} type="button">
                        검증 완료
                      </button>
                    )}
                  </div>
                  {selected?.id === book.id && <BookEditor form={form} reset={reset} selected={selected} setForm={setForm} submit={submit} />}
                </div>
              ))}
              {!loading && books.length === 0 && <EmptyState title="대상 책 없음" description="현재 조건에 해당하는 책이 없습니다." />}
            </div>
          </div>
        </div>
      </Section>
    </main>
  );
}

function BookEditor({
  form,
  reset,
  selected,
  setForm,
  submit,
}: {
  form: { title: string; authorsText: string; publisher: string; publishedDate: string; thumbnailUrl: string };
  reset: () => void;
  selected: BookSummary | null;
  setForm: Dispatch<SetStateAction<{ title: string; authorsText: string; publisher: string; publishedDate: string; thumbnailUrl: string }>>;
  submit: (event: FormEvent) => Promise<void>;
}) {
  return (
    <Card>
      <form className="grid gap-3" onSubmit={submit}>
        <h2 className="text-lg font-normal">{selected ? `Book ${selected.id} 수정` : "책 선택"}</h2>
        <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} placeholder="책 제목" value={form.title} />
        <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setForm((current) => ({ ...current, authorsText: event.target.value }))} placeholder="저자" value={form.authorsText} />
        <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setForm((current) => ({ ...current, publisher: event.target.value }))} placeholder="출판사" value={form.publisher} />
        <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setForm((current) => ({ ...current, publishedDate: event.target.value }))} type="date" value={form.publishedDate} />
        <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setForm((current) => ({ ...current, thumbnailUrl: event.target.value }))} placeholder="표지 이미지 URL" value={form.thumbnailUrl} />
        <div className="flex flex-wrap gap-3">
          <Button type="submit">저장</Button>
          <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-normal" onClick={reset} type="button">
            선택 해제
          </button>
        </div>
      </form>
    </Card>
  );
}

function bookStatusLabel(status: BookSummary["status"]) {
  return status === "VERIFIED" ? "검증 완료" : "검증 필요";
}
