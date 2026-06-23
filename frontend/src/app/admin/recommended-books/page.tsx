"use client";

import { FormEvent, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section } from "@/components/ui/primitives";
import { apiPost, type RecommendedBook } from "@/lib/api";

export default function AdminRecommendedBooksPage() {
  const [form, setForm] = useState({ targetMonth: new Date().toISOString().slice(0, 7) + "-01", bookId: "", reason: "", displayOrder: "1" });
  const [message, setMessage] = useState<string | null>(null);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const result = await apiPost<RecommendedBook>("/admin/recommended-books", {
      targetMonth: form.targetMonth,
      bookId: Number(form.bookId),
      reason: form.reason,
      displayOrder: Number(form.displayOrder),
    });
    setMessage(result.success ? "추천책이 등록되었습니다." : result.error?.message ?? "추천책을 등록하지 못했습니다.");
  }

  return (
    <AuthGate required="ADMIN">
      {() => (
        <main>
          <Section>
            <div className="grid gap-8">
              <PageHeader eyebrow="Admin" title="이달의 추천책 관리" description="기존 Book ID를 기준으로 월별 추천책을 등록합니다." />
              {message && <EmptyState title="처리 결과" description={message} />}
              <Card>
                <form className="grid gap-3" onSubmit={submit}>
                  <input
                    className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
                    onChange={(event) => setForm((current) => ({ ...current, targetMonth: event.target.value }))}
                    type="date"
                    value={form.targetMonth}
                  />
                  <input
                    className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
                    onChange={(event) => setForm((current) => ({ ...current, bookId: event.target.value }))}
                    placeholder="Book ID"
                    value={form.bookId}
                  />
                  <input
                    className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
                    onChange={(event) => setForm((current) => ({ ...current, displayOrder: event.target.value }))}
                    placeholder="노출 순서 1~5"
                    value={form.displayOrder}
                  />
                  <textarea
                    className="min-h-28 border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3"
                    onChange={(event) => setForm((current) => ({ ...current, reason: event.target.value }))}
                    placeholder="추천 이유"
                    value={form.reason}
                  />
                  <Button type="submit">추천책 등록</Button>
                </form>
              </Card>
            </div>
          </Section>
        </main>
      )}
    </AuthGate>
  );
}
