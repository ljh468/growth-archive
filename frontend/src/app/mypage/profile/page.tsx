"use client";

import { FormEvent, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section } from "@/components/ui/primitives";
import { apiGet, apiPut, type InterestTag, type MyProfile, uploadImage } from "@/lib/api";

export default function MyProfilePage() {
  return (
    <AuthGate required="MEMBER">
      {() => <MyProfileContent />}
    </AuthGate>
  );
}

function MyProfileContent() {
  const [profile, setProfile] = useState<MyProfile | null>(null);
  const [interestTags, setInterestTags] = useState<InterestTag[]>([]);
  const [message, setMessage] = useState<string | null>(null);
  const [profileImageFile, setProfileImageFile] = useState<File | null>(null);

  useEffect(() => {
    apiGet<MyProfile>("/me/profile").then((result) => {
      if (!result.success) {
        setMessage(result.error?.message ?? "프로필을 불러오지 못했습니다.");
        return;
      }
      setProfile(result.data);
    });
    apiGet<InterestTag[]>("/interest-tags").then((result) => result.success && setInterestTags(result.data));
  }, []);

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!profile) {
      return;
    }
    const profileImageId = await uploadProfileImage();
    if (profileImageId === undefined) {
      return;
    }
    const result = await apiPut<MyProfile>("/me/profile", { ...profile, profileImageId });
    if (!result.success) {
      setMessage(result.error?.message ?? "프로필을 저장하지 못했습니다.");
      return;
    }
    setProfile(result.data);
    setProfileImageFile(null);
    setMessage("프로필이 저장되었습니다.");
  }

  async function uploadProfileImage() {
    if (!profileImageFile) {
      return profile?.profileImageId ?? null;
    }
    const result = await uploadImage(profileImageFile, "PROFILE");
    if (!result.success) {
      setMessage(result.error?.message ?? "프로필 이미지를 업로드하지 못했습니다.");
      return undefined;
    }
    return result.data.imageId;
  }

  function toggleInterestTag(tagId: number) {
    if (!profile) {
      return;
    }
    if (profile.interestTagIds.includes(tagId)) {
      setProfile({ ...profile, interestTagIds: profile.interestTagIds.filter((id) => id !== tagId) });
      return;
    }
    if (profile.interestTagIds.length >= 5) {
      return;
    }
    setProfile({ ...profile, interestTagIds: [...profile.interestTagIds, tagId] });
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
                <Input label="닉네임" required value={profile.nickname} onChange={(value) => setProfile({ ...profile, nickname: value })} />
                <Input label="한 줄 소개" required value={profile.oneLineIntro} onChange={(value) => setProfile({ ...profile, oneLineIntro: value })} />
                <label className="grid gap-2 text-sm">
                  프로필 이미지
                  {profile.profileImageUrl && <img alt="" className="size-20 rounded-full object-cover shadow-[var(--shadow-soft)]" src={profile.profileImageUrl} />}
                  <input accept="image/jpeg,image/png,image/webp" onChange={(event) => setProfileImageFile(event.target.files?.[0] ?? null)} type="file" />
                </label>
                <Input label="실명" required value={profile.realName ?? ""} onChange={(value) => setProfile({ ...profile, realName: value })} />
                <Input label="생년월일" max={new Date().toISOString().slice(0, 10)} type="date" value={profile.birthDate ?? ""} onChange={(value) => setProfile({ ...profile, birthDate: value || null })} />
                <label className="grid gap-2 text-sm">
                  공개 표시 방식
                  <select
                    className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
                    onChange={(event) => setProfile({ ...profile, displayNameType: event.target.value as MyProfile["displayNameType"] })}
                    value={profile.displayNameType}
                  >
                    <option value="REAL_NAME">실명</option>
                    <option value="NICKNAME">닉네임</option>
                  </select>
                </label>
                <div className="grid gap-2 text-sm">
                  <span className="flex items-center gap-2">관심 분야 <RequiredMark /></span>
                  <div className="flex flex-wrap gap-2">
                    {interestTags.map((tag) => {
                      const selected = profile.interestTagIds.includes(tag.id);
                      return (
                        <button
                          className={`rounded-full border px-3 py-1.5 text-xs transition ${selected ? "border-[var(--color-deep-green)] bg-[var(--color-deep-green)] !text-[var(--color-warm-white)]" : "border-[var(--color-line)] bg-[var(--color-warm-white)] text-[var(--color-charcoal)] hover:border-[var(--color-deep-green)]"}`}
                          key={tag.id}
                          onClick={() => toggleInterestTag(tag.id)}
                          style={selected ? { color: "var(--color-warm-white)" } : undefined}
                          type="button"
                        >
                          {tag.name}
                        </button>
                      );
                    })}
                  </div>
                </div>
                <Textarea label="50살의 나" required value={profile.futureMeAt50} onChange={(value) => setProfile({ ...profile, futureMeAt50: value })} />
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

function Input({ label, value, onChange, type = "text", max, required = false }: { label: string; value: string; onChange: (value: string) => void; type?: string; max?: string; required?: boolean }) {
  return (
    <label className="grid gap-2 text-sm">
      <span className="flex items-center gap-2">{label} {required && <RequiredMark />}</span>
      <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" max={max} onChange={(event) => onChange(event.target.value)} required={required} type={type} value={value} />
    </label>
  );
}

function Textarea({ label, value, onChange, required = false }: { label: string; value: string; onChange: (value: string) => void; required?: boolean }) {
  return (
    <label className="grid gap-2 text-sm">
      <span className="flex items-center gap-2">{label} {required && <RequiredMark />}</span>
      <textarea className="min-h-28 border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3" onChange={(event) => onChange(event.target.value)} required={required} value={value} />
    </label>
  );
}

function RequiredMark() {
  return (
    <span className="rounded-full bg-[rgba(47,90,67,0.1)] px-2 py-0.5 text-[11px] font-normal leading-none text-[var(--color-deep-green)]">
      필수
    </span>
  );
}
