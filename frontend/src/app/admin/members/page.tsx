"use client";

import { FormEvent, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { MobileBackButton } from "@/components/MobileBackButton";
import { Button, Card, EmptyState, PageHeader, RecordButton, Section, Tag } from "@/components/ui/primitives";
import { apiGet, apiPost, apiPut, type AdminMember } from "@/lib/api";

export default function AdminMembersPage() {
  return (
    <AuthGate required="ADMIN">
      {() => <AdminMembersContent />}
    </AuthGate>
  );
}

function AdminMembersContent() {
  const [members, setMembers] = useState<AdminMember[]>([]);
  const [keyword, setKeyword] = useState("");
  const [selected, setSelected] = useState<AdminMember | null>(null);
  const [month, setMonth] = useState("");
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function load(nextKeyword = keyword) {
    const params = new URLSearchParams({ page: "0", size: "100" });
    if (nextKeyword.trim()) {
      params.set("keyword", nextKeyword.trim());
    }
    const result = await apiGet<AdminMember[]>(`/admin/members?${params.toString()}`);
    if (!result.success) {
      setMessage(result.error?.message ?? "회원 목록을 불러오지 못했습니다.");
      return;
    }
    setMembers(result.data);
    setMessage(null);
  }

  async function choose(memberId: number) {
    if (selected?.memberId === memberId) {
      setSelected(null);
      setMessage(null);
      return;
    }
    const result = await apiGet<AdminMember>(`/admin/members/${memberId}`);
    if (!result.success) {
      setMessage(result.error?.message ?? "회원 상세를 불러오지 못했습니다.");
      return;
    }
    setSelected(result.data);
    setMonth(result.data.participationStartMonth.slice(0, 7));
  }

  async function submitSearch(event: FormEvent) {
    event.preventDefault();
    await load(keyword);
  }

  async function deactivate() {
    if (!selected) {
      return;
    }
    const result = await apiPost<void>(`/admin/members/${selected.memberId}/deactivate`);
    setMessage(result.success ? "회원이 비활성화되었습니다." : result.error?.message ?? "비활성화하지 못했습니다.");
    if (result.success) {
      await choose(selected.memberId);
      await load();
    }
  }

  async function reactivate() {
    if (!selected) {
      return;
    }
    const result = await apiPost<void>(`/admin/members/${selected.memberId}/reactivate`);
    setMessage(result.success ? "회원이 재활성화되었습니다." : result.error?.message ?? "재활성화하지 못했습니다.");
    if (result.success) {
      await choose(selected.memberId);
      await load();
    }
  }

  async function saveStartMonth(event: FormEvent) {
    event.preventDefault();
    if (!selected) {
      return;
    }
    const result = await apiPut<AdminMember>(`/admin/members/${selected.memberId}/participation-start-month`, {
      participationStartMonth: month,
    });
    setMessage(result.success ? "참여 시작월이 변경되었습니다." : result.error?.message ?? "참여 시작월을 변경하지 못했습니다.");
    if (result.success) {
      setSelected(result.data);
      await load();
    }
  }

  async function updateRole(role: "MEMBER" | "ADMIN") {
    if (!selected) {
      return;
    }
    const result = await apiPut<AdminMember>(`/admin/members/${selected.memberId}/role`, { role });
    setMessage(result.success ? "회원 권한이 변경되었습니다." : result.error?.message ?? "회원 권한을 변경하지 못했습니다.");
    if (result.success) {
      setSelected(result.data);
      await load();
    }
  }

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <MobileBackButton fallbackHref="/admin" />
          <PageHeader eyebrow="Admin" title="회원 관리" description="회원 상태를 확인하고 비활성화, 재활성화, 참여 시작월을 조정합니다." />
          {message && <EmptyState title="상태" description={message} />}
          <form className="flex flex-col gap-3 sm:flex-row" onSubmit={submitSearch}>
            <input className="min-h-11 flex-1 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setKeyword(event.target.value)} placeholder="닉네임, 이름, 직업 검색" value={keyword} />
            <Button type="submit">검색</Button>
          </form>
          <div className="grid gap-5">
            <div className="grid gap-3">
              {members.map((member) => (
                <div
                  className={[
                    "grid gap-3",
                    selected?.memberId === member.memberId ? "lg:grid-cols-[minmax(0,1fr)_minmax(360px,0.8fr)] lg:items-start" : "",
                  ].join(" ")}
                  key={member.memberId}
                >
                  <button
                    className={[
                      "border bg-[var(--color-warm-white)] p-4 text-left transition",
                      selected?.memberId === member.memberId ? "border-[var(--color-deep-green)] shadow-[var(--shadow-soft)]" : "border-[var(--color-line)]",
                    ].join(" ")}
                    onClick={() => choose(member.memberId)}
                    type="button"
                  >
                    <div className="flex flex-wrap gap-2">
                      <Tag>{member.role}</Tag>
                      <Tag>{memberStatus(member)}</Tag>
                    </div>
                    <p className="mt-3 font-normal">{member.displayName}</p>
                    <p className="mt-1 text-sm text-[var(--color-charcoal)]">@{member.nickname} · 참여 시작 {member.participationStartMonth.slice(0, 7)}</p>
                  </button>
                  {selected?.memberId === member.memberId && (
                    <MemberEditor
                      deactivate={deactivate}
                      month={month}
                      reactivate={reactivate}
                      saveStartMonth={saveStartMonth}
                      selected={selected}
                      setMonth={setMonth}
                      updateRole={updateRole}
                    />
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>
      </Section>
    </main>
  );
}

function MemberEditor({
  deactivate,
  month,
  reactivate,
  saveStartMonth,
  selected,
  setMonth,
  updateRole,
}: {
  deactivate: () => Promise<void>;
  month: string;
  reactivate: () => Promise<void>;
  saveStartMonth: (event: FormEvent) => Promise<void>;
  selected: AdminMember | null;
  setMonth: (value: string) => void;
  updateRole: (role: "MEMBER" | "ADMIN") => Promise<void>;
}) {
  return (
    <Card>
      {!selected && <p className="text-sm text-[var(--color-charcoal)]">관리할 회원을 선택하세요.</p>}
      {selected && (
        <div className="grid gap-5">
          <div>
            <div className="flex flex-wrap gap-2">
              <Tag>{selected.role}</Tag>
              <Tag>{memberStatus(selected)}</Tag>
            </div>
            <h2 className="mt-3 text-lg font-normal">{selected.displayName}</h2>
            <p className="mt-1 text-sm text-[var(--color-charcoal)]">@{selected.nickname}</p>
            <p className="mt-3 text-sm leading-6">{selected.oneLineIntro}</p>
            {selected.interestTags.length > 0 && <p className="mt-3 text-sm text-[var(--color-charcoal)]">{selected.interestTags.join(", ")}</p>}
          </div>
          <form className="grid gap-3" onSubmit={saveStartMonth}>
            <label className="grid gap-2 text-sm">
              참여 시작월
              <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setMonth(event.target.value)} type="month" value={month} />
            </label>
            <Button type="submit" variant="secondary">참여 시작월 저장</Button>
          </form>
          <div className="grid gap-3 border-t border-[var(--color-line)] pt-4">
            <div>
              <p className="text-sm font-normal">권한</p>
              <p className="mt-1 text-xs leading-5 text-[var(--color-charcoal)]">운영진 권한은 관리자 API에서 다시 검증됩니다.</p>
            </div>
            {selected.deactivatedAt || !selected.onboardingCompletedAt ? (
              <p className="text-xs text-[var(--color-charcoal)]">활성 회원만 권한을 변경할 수 있습니다.</p>
            ) : (
              <div className="flex flex-wrap gap-3">
                {selected.role === "ADMIN" ? (
                  <RecordButton onClick={() => updateRole("MEMBER")}>일반 회원으로 변경</RecordButton>
                ) : (
                  <RecordButton onClick={() => updateRole("ADMIN")}>운영진 권한 부여</RecordButton>
                )}
              </div>
            )}
          </div>
          <div className="flex flex-wrap gap-3">
            {selected.deactivatedAt ? (
              <RecordButton onClick={reactivate}>재활성화 실행</RecordButton>
            ) : (
              <RecordButton onClick={deactivate}>비활성화 실행</RecordButton>
            )}
          </div>
        </div>
      )}
    </Card>
  );
}

function memberStatus(member: AdminMember) {
  if (member.withdrawnAt) {
    return "WITHDRAWN";
  }
  if (member.deactivatedAt) {
    return "DEACTIVATED";
  }
  return member.onboardingCompletedAt ? "ACTIVE" : "ONBOARDING";
}
