"use client";

import { FormEvent, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { API_BASE_URL, apiGet, apiPut, type AdminParticipationSummary } from "@/lib/api";

export default function AdminParticipationPage() {
  return (
    <AuthGate required="ADMIN">
      {() => <AdminParticipationContent />}
    </AuthGate>
  );
}

function AdminParticipationContent() {
  const [month, setMonth] = useState(new Date().toISOString().slice(0, 7));
  const [summary, setSummary] = useState<AdminParticipationSummary | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [month]);

  async function load() {
    const result = await apiGet<AdminParticipationSummary>(`/admin/participation?month=${month}`);
    if (!result.success) {
      setMessage(result.error?.message ?? "참여 현황을 불러오지 못했습니다.");
      return;
    }
    setSummary(result.data);
    setMessage(null);
  }

  async function saveNote(memberId: number, note: string) {
    const result = await apiPut<void>(`/admin/participation/${month}/members/${memberId}/note`, { note });
    setMessage(result.success ? "운영 메모가 저장되었습니다." : result.error?.message ?? "운영 메모를 저장하지 못했습니다.");
    if (result.success) {
      await load();
    }
  }

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <div className="flex flex-wrap items-end justify-between gap-4">
            <PageHeader eyebrow="Admin" title="참여 현황 관리" description="월별 참여 완료와 커피 후원 대상자를 운영용으로 확인합니다." />
            <a className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-normal" href={`${API_BASE_URL}/admin/participation.csv?month=${month}`}>
              CSV 다운로드
            </a>
          </div>
          <label className="grid max-w-xs gap-2 text-sm">
            대상 월
            <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setMonth(event.target.value)} type="month" value={month} />
          </label>
          {message && <EmptyState title="상태" description={message} />}
          {summary && (
            <div className="grid gap-6">
              <div className="grid gap-4 md:grid-cols-3">
                <Metric label="대상 회원" value={summary.totalTargetMemberCount} />
                <Metric label="참여 완료" value={summary.completedCount} />
                <Metric label="참여 필요" value={summary.incompleteCount} />
              </div>
              <div className="grid gap-4">
                {summary.members.map((member) => (
                  <Card key={member.memberId}>
                    <div className="flex flex-wrap items-start justify-between gap-4">
                      <div>
                        <h2 className="font-normal">{member.displayName}</h2>
                        <p className="mt-1 text-sm text-[var(--color-charcoal)]">@{member.nickname}</p>
                        <p className="mt-3 text-sm">독서기록 {member.readingRecordCount}개 · 실행계획 {member.hasActionPlan ? "있음" : "없음"}</p>
                      </div>
                      <Tag>{member.completed ? "참여 완료" : member.calculationTarget ? "참여 필요" : "대상 제외"}</Tag>
                    </div>
                    {member.coffeeSupportTarget && <p className="mt-3 text-sm text-[var(--color-charcoal)]">{member.coffeeSupportItem}</p>}
                    <NoteForm initialNote={member.adminMemo ?? ""} memberId={member.memberId} onSave={saveNote} />
                  </Card>
                ))}
              </div>
            </div>
          )}
        </div>
      </Section>
    </main>
  );
}

function Metric({ label, value }: { label: string; value: number }) {
  return (
    <Card>
      <p className="text-sm text-[var(--color-charcoal)]">{label}</p>
      <p className="mt-3 text-2xl font-normal">{value}</p>
    </Card>
  );
}

function NoteForm({ memberId, initialNote, onSave }: { memberId: number; initialNote: string; onSave: (memberId: number, note: string) => Promise<void> }) {
  const [note, setNote] = useState(initialNote);

  async function submit(event: FormEvent) {
    event.preventDefault();
    await onSave(memberId, note);
  }

  return (
    <form className="mt-4 flex flex-col gap-3 sm:flex-row" onSubmit={submit}>
      <input className="min-h-11 flex-1 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setNote(event.target.value)} placeholder="운영 메모" value={note} />
      <Button type="submit" variant="secondary">
        저장
      </Button>
    </form>
  );
}
