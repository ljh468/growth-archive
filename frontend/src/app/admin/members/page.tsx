"use client";

import { FormEvent, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
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

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <PageHeader eyebrow="Admin" title="회원 관리" description="회원 상태를 확인하고 비활성화, 재활성화, 참여 시작월을 조정합니다." />
          {message && <EmptyState title="상태" description={message} />}
          <form className="flex flex-col gap-3 sm:flex-row" onSubmit={submitSearch}>
            <input className="min-h-11 flex-1 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setKeyword(event.target.value)} placeholder="닉네임, 이름, 직업 검색" value={keyword} />
            <Button type="submit">검색</Button>
          </form>
          <div className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_minmax(360px,0.8fr)]">
            <div className="grid gap-3">
              {members.map((member) => (
                <button className="border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4 text-left" key={member.memberId} onClick={() => choose(member.memberId)} type="button">
                  <div className="flex flex-wrap gap-2">
                    <Tag>{member.role}</Tag>
                    <Tag>{member.deactivatedAt ? "DEACTIVATED" : member.onboardingCompletedAt ? "ACTIVE" : "ONBOARDING"}</Tag>
                  </div>
                  <p className="mt-3 font-normal">{member.displayName}</p>
                  <p className="mt-1 text-sm text-[var(--color-charcoal)]">@{member.nickname} · 참여 시작 {member.participationStartMonth.slice(0, 7)}</p>
                </button>
              ))}
            </div>
            <Card>
              {!selected && <p className="text-sm text-[var(--color-charcoal)]">관리할 회원을 선택하세요.</p>}
              {selected && (
                <div className="grid gap-5">
                  <div>
                    <div className="flex flex-wrap gap-2">
                      <Tag>{selected.role}</Tag>
                      <Tag>{selected.deactivatedAt ? "DEACTIVATED" : selected.onboardingCompletedAt ? "ACTIVE" : "ONBOARDING"}</Tag>
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
                  <div className="flex flex-wrap gap-3">
                    {selected.deactivatedAt ? (
                      <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-normal" onClick={reactivate} type="button">재활성화 실행</button>
                    ) : (
                      <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-normal" onClick={deactivate} type="button">비활성화 실행</button>
                    )}
                  </div>
                </div>
              )}
            </Card>
          </div>
        </div>
      </Section>
    </main>
  );
}
