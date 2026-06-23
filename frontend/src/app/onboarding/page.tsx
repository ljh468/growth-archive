"use client";

import { FormEvent, useEffect, useState } from "react";
import { apiGet, apiPost, type CurrentUser } from "@/lib/api";

type Step = "invite" | "terms" | "profile" | "done";

export default function OnboardingPage() {
  const [step, setStep] = useState<Step>("invite");
  const [message, setMessage] = useState<string | null>(null);
  const [inviteCode, setInviteCode] = useState("");
  const [profile, setProfile] = useState({
    nickname: "",
    oneLineIntro: "",
    displayNameType: "NICKNAME",
    realName: "",
    job: "",
    futureMeAt50: "",
    joinReason: "",
    currentConcern: "",
    threeYearGoal: "",
    interestTagIds: "1",
  });

  useEffect(() => {
    apiGet<CurrentUser>("/auth/me").then((result) => {
      if (!result.success || !result.data.authenticated) {
        window.location.replace("/login");
        return;
      }
      if (result.data.onboardingCompleted && !result.data.deactivated) {
        window.location.replace("/my");
        return;
      }
      if (!result.data.inviteVerified) {
        setStep("invite");
      } else if (!result.data.termsAgreed || !result.data.privacyAgreed) {
        setStep("terms");
      } else {
        setStep("profile");
      }
    });
  }, []);

  async function submitInvite(event: FormEvent) {
    event.preventDefault();
    const result = await apiPost<{ verified: boolean }>("/onboarding/invite-code", { code: inviteCode });
    if (!result.success) {
      setMessage(result.error?.message ?? "초대코드를 확인하지 못했습니다.");
      return;
    }
    setMessage(result.message);
    setStep("terms");
  }

  async function submitTerms(event: FormEvent) {
    event.preventDefault();
    const result = await apiPost("/onboarding/terms", { termsAgreed: true, privacyAgreed: true });
    if (!result.success) {
      setMessage(result.error?.message ?? "동의 정보를 저장하지 못했습니다.");
      return;
    }
    setMessage(null);
    setStep("profile");
  }

  async function submitProfile(event: FormEvent) {
    event.preventDefault();
    const result = await apiPost("/onboarding/profile", {
      ...profile,
      interestTagIds: profile.interestTagIds
        .split(",")
        .map((id) => Number(id.trim()))
        .filter(Boolean),
    });
    if (!result.success) {
      setMessage(result.error?.message ?? "프로필을 저장하지 못했습니다.");
      return;
    }
    setStep("done");
  }

  return (
    <main className="min-h-screen bg-[var(--color-ivory)] px-5 py-8 text-[var(--color-ink)]">
      <section className="mx-auto max-w-2xl">
        <p className="text-sm font-medium text-[var(--color-bronze)]">Member Onboarding</p>
        <h1 className="mt-3 text-3xl font-semibold">성장 프로필 만들기</h1>
        {message && <p className="mt-4 border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3 text-sm">{message}</p>}

        {step === "invite" && (
          <form className="mt-8 grid gap-4" onSubmit={submitInvite}>
            <label className="grid gap-2 text-sm">
              초대코드
              <input
                className="border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 py-3"
                onChange={(event) => setInviteCode(event.target.value)}
                value={inviteCode}
              />
            </label>
            <button className="bg-[var(--color-ink)] px-4 py-3 text-sm font-semibold text-[var(--color-warm-white)]">
              초대코드 확인
            </button>
          </form>
        )}

        {step === "terms" && (
          <form className="mt-8 grid gap-4" onSubmit={submitTerms}>
            <label className="flex gap-3 text-sm">
              <input required type="checkbox" /> 이용약관에 동의합니다.
            </label>
            <label className="flex gap-3 text-sm">
              <input required type="checkbox" /> 개인정보처리방침에 동의합니다.
            </label>
            <button className="bg-[var(--color-ink)] px-4 py-3 text-sm font-semibold text-[var(--color-warm-white)]">
              동의하고 계속
            </button>
          </form>
        )}

        {step === "profile" && (
          <form className="mt-8 grid gap-4" onSubmit={submitProfile}>
            {[
              ["nickname", "닉네임"],
              ["oneLineIntro", "한 줄 소개"],
              ["realName", "실명"],
              ["job", "직업"],
              ["futureMeAt50", "50살의 나"],
              ["joinReason", "가입 이유"],
              ["currentConcern", "현재 고민"],
              ["threeYearGoal", "3년 뒤 목표"],
              ["interestTagIds", "관심 분야 ID"],
            ].map(([key, label]) => (
              <label className="grid gap-2 text-sm" key={key}>
                {label}
                <input
                  className="border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 py-3"
                  onChange={(event) => setProfile((current) => ({ ...current, [key]: event.target.value }))}
                  value={profile[key as keyof typeof profile]}
                />
              </label>
            ))}
            <label className="grid gap-2 text-sm">
              공개 표시 방식
              <select
                className="border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 py-3"
                onChange={(event) => setProfile((current) => ({ ...current, displayNameType: event.target.value }))}
                value={profile.displayNameType}
              >
                <option value="NICKNAME">닉네임 공개</option>
                <option value="REAL_NAME">실명 공개</option>
              </select>
            </label>
            <button className="bg-[var(--color-ink)] px-4 py-3 text-sm font-semibold text-[var(--color-warm-white)]">
              프로필 저장
            </button>
          </form>
        )}

        {step === "done" && (
          <div className="mt-8 border border-[var(--color-line)] bg-[var(--color-warm-white)] p-5">
            <h2 className="text-xl font-semibold">성장 프로필이 만들어졌습니다.</h2>
            <a className="mt-5 inline-block bg-[var(--color-ink)] px-4 py-3 text-sm text-[var(--color-warm-white)]" href="/my">
              마이페이지로 이동
            </a>
          </div>
        )}
      </section>
    </main>
  );
}
