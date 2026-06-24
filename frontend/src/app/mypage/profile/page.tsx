"use client";

import { FormEvent, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section } from "@/components/ui/primitives";
import { apiGet, apiPut, type MyProfile } from "@/lib/api";

export default function MyProfilePage() {
  return (
    <AuthGate required="MEMBER">
      {() => <MyProfileContent />}
    </AuthGate>
  );
}

function MyProfileContent() {
  const [profile, setProfile] = useState<MyProfile | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    apiGet<MyProfile>("/me/profile").then((result) => {
      if (!result.success) {
        setMessage(result.error?.message ?? "프로필을 불러오지 못했습니다.");
        return;
      }
      setProfile(result.data);
    });
  }, []);

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!profile) {
      return;
    }
    const result = await apiPut<MyProfile>("/me/profile", profile);
    if (!result.success) {
      setMessage(result.error?.message ?? "프로필을 저장하지 못했습니다.");
      return;
    }
    setProfile(result.data);
    setMessage("프로필이 저장되었습니다.");
  }

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <PageHeader eyebrow="Profile" title="프로필 수정" description="한 줄 소개는 80자, 주요 소개 항목은 1000자 기준으로 관리합니다." />
          {message && <EmptyState title="상태" description={message} />}
          {!profile && !message && <EmptyState title="불러오는 중입니다." description="내 프로필을 확인하고 있습니다." />}
          {profile && (
            <Card>
              <form className="grid gap-3" onSubmit={submit}>
                <Input label="닉네임" value={profile.nickname} onChange={(value) => setProfile({ ...profile, nickname: value })} />
                <Input label="한 줄 소개" value={profile.oneLineIntro} onChange={(value) => setProfile({ ...profile, oneLineIntro: value })} />
                <Input label="실명" value={profile.realName ?? ""} onChange={(value) => setProfile({ ...profile, realName: value })} />
                <label className="grid gap-2 text-sm">
                  공개 표시 방식
                  <select
                    className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
                    onChange={(event) => setProfile({ ...profile, displayNameType: event.target.value as MyProfile["displayNameType"] })}
                    value={profile.displayNameType}
                  >
                    <option value="NICKNAME">닉네임</option>
                    <option value="REAL_NAME">실명</option>
                  </select>
                </label>
                <Input label="직업" value={profile.job ?? ""} onChange={(value) => setProfile({ ...profile, job: value })} />
                <Input label="관심 분야 ID" value={profile.interestTagIds.join(",")} onChange={(value) => setProfile({ ...profile, interestTagIds: value.split(",").map((id) => Number(id.trim())).filter(Boolean) })} />
                <Textarea label="50살의 나" value={profile.futureMeAt50} onChange={(value) => setProfile({ ...profile, futureMeAt50: value })} />
                <Textarea label="가입 이유" value={profile.joinReason ?? ""} onChange={(value) => setProfile({ ...profile, joinReason: value })} />
                <Textarea label="현재 고민" value={profile.currentConcern ?? ""} onChange={(value) => setProfile({ ...profile, currentConcern: value })} />
                <Textarea label="3년 뒤 목표" value={profile.threeYearGoal ?? ""} onChange={(value) => setProfile({ ...profile, threeYearGoal: value })} />
                <Button type="submit">저장</Button>
              </form>
            </Card>
          )}
        </div>
      </Section>
    </main>
  );
}

function Input({ label, value, onChange }: { label: string; value: string; onChange: (value: string) => void }) {
  return (
    <label className="grid gap-2 text-sm">
      {label}
      <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => onChange(event.target.value)} value={value} />
    </label>
  );
}

function Textarea({ label, value, onChange }: { label: string; value: string; onChange: (value: string) => void }) {
  return (
    <label className="grid gap-2 text-sm">
      {label}
      <textarea className="min-h-28 border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3" onChange={(event) => onChange(event.target.value)} value={value} />
    </label>
  );
}
