"use client";

import { FormEvent, useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, PageHeader, Section } from "@/components/ui/primitives";
import { apiGet, apiPut, type MonthlyReflectionSlot } from "@/lib/api";

export default function MyReflectionsPage() {
  return (
    <AuthGate required="MEMBER">
      {() => <ReflectionContent />}
    </AuthGate>
  );
}

function ReflectionContent() {
  const searchParams = useSearchParams();
  const currentMonth = formatMonthKst(new Date().toISOString());
  const [month, setMonth] = useState(() => normalizeMonth(searchParams.get("month")) ?? currentMonth);
  const [form, setForm] = useState({ didWell: "", couldImprove: "", nextFocus: "" });
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    apiGet<MonthlyReflectionSlot>(`/me/reflections/${month}`).then((result) => {
      if (!result.success) {
        setMessage(result.error?.message ?? "회고 슬롯을 불러오지 못했습니다.");
        return;
      }
      setForm({
        didWell: result.data.reflection?.wellDone ?? "",
        couldImprove: result.data.reflection?.regret ?? "",
        nextFocus: result.data.reflection?.nextFocus ?? "",
      });
      setMessage(result.data.reflection ? null : "이번 달 회고를 작성할 수 있습니다.");
    });
  }, [month]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const result = await apiPut(`/me/reflections/${month}`, form);
    if (!result.success) {
      setMessage(result.error?.message ?? "회고를 저장하지 못했습니다.");
      return;
    }
    setMessage("회고가 저장되었습니다.");
  }

  return (
    <main>
      <Section>
        <div className="grid gap-5 sm:gap-8">
          <PageHeader eyebrow="Monthly Reflection" title="월간 회고" description="회고는 선택 기록이며 참여 현황 계산에 포함되지 않습니다." />
          <Card>
            <form className="grid gap-3 sm:gap-4" onSubmit={submit}>
              <label className="grid gap-2 text-sm">
                대상 월
                <input className="min-h-10 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setMonth(event.target.value)} type="month" value={month} />
              </label>
              <Textarea label="이번 달 잘한 것" value={form.didWell} onChange={(value) => setForm((current) => ({ ...current, didWell: value }))} />
              <Textarea label="아쉬운 점" value={form.couldImprove} onChange={(value) => setForm((current) => ({ ...current, couldImprove: value }))} />
              <Textarea label="다음 달 집중할 것" value={form.nextFocus} onChange={(value) => setForm((current) => ({ ...current, nextFocus: value }))} />
              {message && <p className="text-sm leading-6 text-[var(--color-muted)]">{message}</p>}
              <Button type="submit">회고 저장</Button>
            </form>
          </Card>
        </div>
      </Section>
    </main>
  );
}

function normalizeMonth(value: string | null) {
  return value && /^\d{4}-\d{2}$/.test(value) ? value : null;
}

function formatMonthKst(value: string) {
  return new Intl.DateTimeFormat("en-CA", {
    year: "numeric",
    month: "2-digit",
    timeZone: "Asia/Seoul",
  }).format(new Date(value));
}

function Textarea({ label, value, onChange }: { label: string; value: string; onChange: (value: string) => void }) {
  return (
    <label className="grid gap-2 text-sm">
      {label}
      <textarea className="min-h-28 border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3 sm:min-h-32" onChange={(event) => onChange(event.target.value)} value={value} />
    </label>
  );
}
