"use client";

import { FormEvent, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { MobileBackButton } from "@/components/MobileBackButton";
import { Button, Card, EmptyState, PageHeader, RecordButton, Section, Tag } from "@/components/ui/primitives";
import { apiDelete, apiGet, apiPost, apiPut, type AdminParticipationMember, type AdminParticipationSummary } from "@/lib/api";

export default function AdminParticipationPage() {
  return (
    <AuthGate required="ADMIN">
      {() => <AdminParticipationContent />}
    </AuthGate>
  );
}

function AdminParticipationContent() {
  const [month, setMonth] = useState(previousMonthValue());
  const [summary, setSummary] = useState<AdminParticipationSummary | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [messageBody, setMessageBody] = useState("투썸 커피 선물 부탁드려요~");
  const [memberSearch, setMemberSearch] = useState("");
  const [memberPage, setMemberPage] = useState(1);

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

  async function markManualCompletion(memberId: number) {
    const result = await apiPost<void>(`/admin/participation/${month}/members/${memberId}/manual-completion`);
    setMessage(result.success ? "운영진 참여 처리되었습니다." : result.error?.message ?? "참여 처리하지 못했습니다.");
    if (result.success) {
      await load();
    }
  }

  async function clearManualCompletion(memberId: number) {
    const result = await apiDelete<void>(`/admin/participation/${month}/members/${memberId}/manual-completion`);
    setMessage(result.success ? "참여 처리가 취소되었습니다." : result.error?.message ?? "참여 처리를 취소하지 못했습니다.");
    if (result.success) {
      await load();
    }
  }

  const incompleteMembers = summary?.members.filter((member) => member.coffeeSupportTarget) ?? [];
  const managedMembers = summary?.members.filter((member) => member.coffeeSupportTarget || member.manuallyCompleted) ?? [];
  const filteredManagedMembers = filterMembers(managedMembers, memberSearch);
  const memberPageSize = 10;
  const totalMemberPages = Math.max(1, Math.ceil(filteredManagedMembers.length / memberPageSize));
  const currentMemberPage = Math.min(memberPage, totalMemberPages);
  const pagedManagedMembers = filteredManagedMembers.slice((currentMemberPage - 1) * memberPageSize, currentMemberPage * memberPageSize);
  const mentionLine = createMentionLine(incompleteMembers);
  const kakaoMessage = createKakaoMessage(mentionLine, messageBody);

  async function copyMessage() {
    if (!mentionLine) {
      setMessage("보낼 메시지가 없습니다.");
      return;
    }
    await navigator.clipboard.writeText(kakaoMessage);
    setMessage("카카오톡 메시지를 복사했습니다.");
  }

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <MobileBackButton fallbackHref="/admin" />
          <PageHeader eyebrow="Admin" title="참여 현황 관리" description="선택한 월의 참여 인원과 미참여자를 확인하고 카카오톡 안내 메시지를 만듭니다." />
          <label className="grid max-w-xs gap-2 text-sm">
            대상 월
            <input
              className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
              onChange={(event) => {
                setMonth(event.target.value);
                setMemberPage(1);
              }}
              type="month"
              value={month}
            />
          </label>
          {message && <EmptyState title="상태" description={message} />}
          {summary && (
            <div className="grid gap-6">
              <Card>
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <h2 className="font-normal">카카오톡 메시지</h2>
                    <p className="mt-1 text-sm leading-6 text-[var(--color-charcoal)]">미참여자 멘션과 안내 문구를 복사합니다.</p>
                  </div>
                  <RecordButton className="shrink-0" onClick={copyMessage}>복사</RecordButton>
                </div>
                <label className="mt-3 grid gap-2 text-sm">
                  안내 문구
                  <textarea
                    className="min-h-16 w-full border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3 text-sm leading-6"
                    onChange={(event) => setMessageBody(event.target.value)}
                    value={messageBody}
                  />
                </label>
                <textarea
                  className="mt-3 min-h-24 w-full border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3 text-sm leading-6"
                  onChange={() => undefined}
                  readOnly
                  value={mentionLine ? kakaoMessage : "미참여자가 없습니다."}
                />
              </Card>
              <div className="grid gap-4 md:grid-cols-3">
                <Metric label="대상 회원" value={summary.totalTargetMemberCount} />
                <Metric label="참여 완료" value={summary.completedCount} />
                <Metric label="참여 필요" value={summary.incompleteCount} />
              </div>
              <Card>
                <div className="flex flex-wrap items-start justify-between gap-4">
                  <div>
                    <h2 className="font-normal">{summary.month} 미참여자 관리</h2>
                    <p className="mt-2 text-sm leading-6 text-[var(--color-charcoal)]">
                      미참여자 확인, 운영진 참여 처리, 메모를 한 곳에서 관리합니다.
                    </p>
                  </div>
                  <Tag>{incompleteMembers.length}명</Tag>
                </div>
                <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                  <input
                    className="min-h-10 w-full border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 text-sm sm:max-w-xs"
                    onChange={(event) => {
                      setMemberSearch(event.target.value);
                      setMemberPage(1);
                    }}
                    placeholder="이름 또는 닉네임 검색"
                    value={memberSearch}
                  />
                  <p className="text-xs text-[var(--color-charcoal)]">
                    {filteredManagedMembers.length}명 중 {pagedManagedMembers.length}명 표시
                  </p>
                </div>
                {pagedManagedMembers.length > 0 ? (
                  <div className="mt-4 grid gap-2">
                    {pagedManagedMembers.map((member) => (
                      <div className="grid gap-3 border border-[rgba(229,222,209,0.82)] bg-[rgba(255,254,250,0.58)] px-3 py-2 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-center" key={member.memberId}>
                        <div className="min-w-0 flex-1">
                          <div className="flex flex-wrap items-center gap-2">
                            <p className="text-sm font-normal">@{member.displayName}</p>
                            {member.manuallyCompleted && <Tag>운영진 처리 완료</Tag>}
                          </div>
                          <p className="mt-1 text-xs text-[var(--color-charcoal)]">
                            독서기록 {member.readingRecordCount}개 · 실행계획 {member.hasActionPlan ? "있음" : "없음"}
                          </p>
                          <NoteForm compact initialNote={member.adminMemo ?? ""} key={`${member.memberId}-${member.adminMemo ?? ""}`} memberId={member.memberId} onSave={saveNote} />
                        </div>
                        <div className="flex items-center justify-between gap-2 sm:justify-end">
                          {!member.manuallyCompleted && <span className="text-xs text-[var(--color-bronze)]">{member.coffeeSupportItem}</span>}
                          <RecordButton
                            onClick={() => (member.manuallyCompleted ? clearManualCompletion(member.memberId) : markManualCompletion(member.memberId))}
                            style={member.manuallyCompleted ? { borderColor: "rgba(150,64,56,0.36)", color: "#964038" } : undefined}
                          >
                            {member.manuallyCompleted ? "미참여 처리" : "참여 처리"}
                          </RecordButton>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="mt-4 text-sm text-[var(--color-charcoal)]">관리할 미참여자가 없습니다.</p>
                )}
                {filteredManagedMembers.length > memberPageSize && (
                  <div className="mt-4 flex items-center justify-between border-t border-[var(--color-line)] pt-3">
                    <button
                      className="text-[11px] font-normal text-[var(--color-deep-green)] disabled:text-[var(--color-charcoal)] disabled:opacity-45"
                      disabled={currentMemberPage === 1}
                      onClick={() => setMemberPage((page) => Math.max(1, page - 1))}
                      type="button"
                    >
                      이전
                    </button>
                    <span className="text-[11px] text-[var(--color-charcoal)]">
                      {currentMemberPage} / {totalMemberPages}
                    </span>
                    <button
                      className="text-[11px] font-normal text-[var(--color-deep-green)] disabled:text-[var(--color-charcoal)] disabled:opacity-45"
                      disabled={currentMemberPage === totalMemberPages}
                      onClick={() => setMemberPage((page) => Math.min(totalMemberPages, page + 1))}
                      type="button"
                    >
                      다음
                    </button>
                  </div>
                )}
              </Card>
            </div>
          )}
        </div>
      </Section>
    </main>
  );
}

function previousMonthValue() {
  const date = new Date();
  date.setMonth(date.getMonth() - 1);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
}

function createMentionLine(members: AdminParticipationMember[]) {
  return members.map((member) => `@${member.displayName}`).join(" ");
}

function createKakaoMessage(mentionLine: string, messageBody: string) {
  return [mentionLine, messageBody.trim()].filter(Boolean).join("\n");
}

function filterMembers(members: AdminParticipationMember[], keyword: string) {
  const normalized = keyword.trim().toLowerCase();
  if (!normalized) {
    return members;
  }
  return members.filter((member) => {
    const values = [member.displayName, member.nickname, member.adminMemo ?? ""];
    return values.some((value) => value.toLowerCase().includes(normalized));
  });
}

function Metric({ label, value }: { label: string; value: number }) {
  return (
    <div className="rounded-[var(--radius-card)] border border-[rgba(233,225,214,0.8)] bg-[rgba(255,254,250,0.86)] px-3 py-2.5 shadow-[0_10px_24px_rgba(52,38,22,0.05)] sm:px-4 sm:py-3">
      <p className="text-sm text-[var(--color-charcoal)]">{label}</p>
      <p className="mt-1 text-xl font-normal sm:text-2xl">{value}</p>
    </div>
  );
}

function NoteForm({ memberId, initialNote, compact = false, onSave }: { memberId: number; initialNote: string; compact?: boolean; onSave: (memberId: number, note: string) => Promise<void> }) {
  const [note, setNote] = useState(initialNote);

  async function submit(event: FormEvent) {
    event.preventDefault();
    await onSave(memberId, note);
  }

  return (
    <form className={compact ? "mt-3 grid min-w-0 grid-cols-[minmax(0,1fr)_auto] items-center gap-1.5" : "mt-4 flex flex-col gap-3 sm:flex-row"} onSubmit={submit}>
      <input className={`${compact ? "min-h-8 min-w-0 text-xs" : "min-h-11"} flex-1 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3`} onChange={(event) => setNote(event.target.value)} placeholder="운영 메모" value={note} />
      {compact ? (
        <RecordButton className="shrink-0" type="submit">저장</RecordButton>
      ) : (
        <Button type="submit" variant="secondary">
          저장
        </Button>
      )}
    </form>
  );
}
