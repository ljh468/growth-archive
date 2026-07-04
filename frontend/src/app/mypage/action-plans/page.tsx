"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AuthGate } from "@/components/AuthGate";
import { Card, ConfirmDialog, PageHeader, Section } from "@/components/ui/primitives";
import { apiDelete, apiGet, apiPost, apiPut, type MonthlyActionPlan } from "@/lib/api";

export default function MyActionPlansPage() {
  return (
    <AuthGate required="MEMBER">
      {() => <ActionPlanContent />}
    </AuthGate>
  );
}

function ActionPlanContent() {
  const router = useRouter();
  const currentMonth = new Date().toISOString().slice(0, 7);
  const [month, setMonth] = useState(currentMonth);
  const [plan, setPlan] = useState<MonthlyActionPlan | null>(null);
  const [form, setForm] = useState({ title: "", content: "" });
  const [message, setMessage] = useState<string | null>(null);
  const [deleteOpen, setDeleteOpen] = useState(false);

  useEffect(() => {
    apiGet<MonthlyActionPlan | null>(`/me/action-plans/${month}`).then((result) => {
      if (!result.success) {
        setMessage(result.error?.message ?? "실행계획을 불러오지 못했습니다.");
        return;
      }
      setPlan(result.data);
      setForm({ title: result.data?.title ?? "", content: result.data?.content ?? "" });
      setMessage(result.data ? null : "이번 달 실행계획을 아직 작성하지 않았습니다.");
    });
  }, [month]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const result = plan
      ? await apiPut<MonthlyActionPlan>(`/me/action-plans/${plan.id}`, form)
      : await apiPost<MonthlyActionPlan>("/me/action-plans", { month, ...form });
    if (!result.success) {
      setMessage(result.error?.message ?? "실행계획을 저장하지 못했습니다.");
      return;
    }
    setPlan(result.data);
    router.push("/mypage");
  }

  async function remove() {
    if (!plan) {
      return;
    }
    const result = await apiDelete<void>(`/me/action-plans/${plan.id}`);
    if (!result.success) {
      setMessage(result.error?.message ?? "실행계획을 삭제하지 못했습니다.");
      return;
    }
    setPlan(null);
    setForm({ title: "", content: "" });
    setDeleteOpen(false);
    setMessage("실행계획이 삭제되었습니다.");
  }

  return (
    <main>
      <Section>
        <div className="grid gap-5 sm:gap-8">
          <PageHeader eyebrow="Monthly Action Plan" title="월간 액션플랜" description="이번 달의 선언을 자유롭게 남기고 참여 상태에 반영합니다." />
          <Card>
            <form className="grid gap-3 sm:gap-4" onSubmit={submit}>
              <label className="grid gap-2 text-sm">
                대상 월
                <input className="min-h-10 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setMonth(event.target.value)} type="month" value={month} />
              </label>
              <label className="grid gap-2 text-sm">
                제목
                <input className="min-h-10 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} value={form.title} />
              </label>
              <label className="grid gap-2 text-sm">
                내용
                <textarea className="min-h-36 border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3 sm:min-h-48" onChange={(event) => setForm((current) => ({ ...current, content: event.target.value }))} value={form.content} />
              </label>
              {message && <p className="text-sm leading-6 text-[var(--color-muted)]">{message}</p>}
              <div className="flex flex-wrap gap-3">
                <button className="archive-record-button archive-record-button--primary" type="submit">
                  {plan ? "수정" : "저장"}
                </button>
                {plan && (
                  <button className="archive-record-button archive-record-button--danger" onClick={() => setDeleteOpen(true)} type="button">
                    삭제
                  </button>
                )}
              </div>
            </form>
          </Card>
        </div>
      </Section>
      <ConfirmDialog
        description="삭제하면 이번 달 실행계획 기록에서 사라집니다."
        onCancel={() => setDeleteOpen(false)}
        onConfirm={() => {
          void remove();
        }}
        open={deleteOpen}
        title="이 실행계획을 삭제할까요?"
      />
    </main>
  );
}
