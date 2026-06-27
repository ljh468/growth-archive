"use client";

import { FormEvent, useEffect, useState } from "react";
import { apiGet, apiPost, type CurrentUser, type InterestTag, uploadImage } from "@/lib/api";

type Step = "invite" | "terms" | "profile" | "done";

export default function OnboardingPage() {
  const [step, setStep] = useState<Step>("invite");
  const [message, setMessage] = useState<string | null>(null);
  const [inviteCode, setInviteCode] = useState("");
  const [interestTags, setInterestTags] = useState<InterestTag[]>([]);
  const [profileImageFile, setProfileImageFile] = useState<File | null>(null);
  const [profile, setProfile] = useState({
    nickname: "",
    oneLineIntro: "",
    displayNameType: "REAL_NAME",
    realName: "",
    birthDate: "",
    profileImageId: null as number | null,
    futureMeAt50: "",
    joinReason: "",
    currentConcern: "",
    threeYearGoal: "",
    interestTagIds: [] as number[],
  });

  useEffect(() => {
    apiGet<CurrentUser>("/auth/me").then((result) => {
      if (!result.success || !result.data.authenticated) {
        window.location.replace("/login");
        return;
      }
      if (result.data.onboardingCompleted && !result.data.deactivated) {
        window.location.replace("/mypage");
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
    apiGet<InterestTag[]>("/interest-tags").then((result) => result.success && setInterestTags(result.data));
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
    if (profile.interestTagIds.length === 0) {
      setMessage("관심 분야를 1개 이상 선택해 주세요.");
      return;
    }
    const profileImageId = await uploadProfileImage();
    if (profileImageId === undefined) {
      return;
    }
    const result = await apiPost("/onboarding/profile", {
      ...profile,
      birthDate: profile.birthDate || null,
      profileImageId,
    });
    if (!result.success) {
      setMessage(result.error?.message ?? "프로필을 저장하지 못했습니다.");
      return;
    }
    setStep("done");
  }

  async function uploadProfileImage() {
    if (!profileImageFile) {
      return profile.profileImageId;
    }
    const result = await uploadImage(profileImageFile, "PROFILE");
    if (!result.success) {
      setMessage(result.error?.message ?? "프로필 이미지를 업로드하지 못했습니다.");
      return undefined;
    }
    return result.data.imageId;
  }

  function updateProfile(key: keyof typeof profile, value: string) {
    setProfile((current) => ({ ...current, [key]: value }));
  }

  function toggleInterestTag(tagId: number) {
    setProfile((current) => {
      if (current.interestTagIds.includes(tagId)) {
        return { ...current, interestTagIds: current.interestTagIds.filter((id) => id !== tagId) };
      }
      if (current.interestTagIds.length >= 5) {
        return current;
      }
      return { ...current, interestTagIds: [...current.interestTagIds, tagId] };
    });
  }

  return (
    <main className="min-h-screen bg-[var(--color-ivory)] px-5 py-10 text-[var(--color-ink)]">
      <section className="mx-auto max-w-4xl">
        <p className="font-latin text-4xl text-[var(--color-bronze)]">People Onboarding</p>
        <div className="mt-4 max-w-2xl">
          <h1 className="font-display text-3xl font-normal leading-[1.2] sm:text-4xl">성장 프로필 만들기</h1>
          <p className="mt-4 text-sm leading-7 text-[var(--color-muted)]">
            모임 안에서 어떤 사람으로 기억되고 싶은지 가볍게 적어 주세요. 필수 항목만 먼저 채우고, 나머지는 나중에 마이페이지에서 고칠 수 있습니다.
          </p>
        </div>
        {message && <p className="mt-6 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4 text-sm text-[var(--color-charcoal)]">{message}</p>}

        {step === "invite" && (
          <form className="mt-8 grid max-w-xl gap-5 rounded-[var(--radius-card)] bg-[var(--color-warm-white)] p-6 shadow-[var(--shadow-soft)]" onSubmit={submitInvite}>
            <label className="grid gap-2 text-sm text-[var(--color-charcoal)]">
              운영진에게 받은 초대코드
              <input
                className="min-h-12 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 text-base outline-none transition focus:border-[var(--color-deep-green)]"
                onChange={(event) => setInviteCode(event.target.value)}
                placeholder="초대코드 입력"
                value={inviteCode}
              />
            </label>
            <button className="min-h-11 rounded-[var(--radius-card)] bg-[var(--color-deep-green)] px-5 text-sm font-normal !text-[var(--color-warm-white)]" style={{ color: "var(--color-warm-white)" }}>
              초대코드 확인
            </button>
          </form>
        )}

        {step === "terms" && (
          <form className="mt-8 grid max-w-xl gap-4 rounded-[var(--radius-card)] bg-[var(--color-warm-white)] p-6 shadow-[var(--shadow-soft)]" onSubmit={submitTerms}>
            <label className="flex gap-3 text-sm leading-6 text-[var(--color-charcoal)]">
              <input required type="checkbox" /> 이용약관에 동의합니다.
            </label>
            <label className="flex gap-3 text-sm leading-6 text-[var(--color-charcoal)]">
              <input required type="checkbox" /> 개인정보처리방침에 동의합니다.
            </label>
            <button className="mt-2 min-h-11 rounded-[var(--radius-card)] bg-[var(--color-deep-green)] px-5 text-sm font-normal !text-[var(--color-warm-white)]" style={{ color: "var(--color-warm-white)" }}>
              동의하고 계속
            </button>
          </form>
        )}

        {step === "profile" && (
          <form className="mt-8 grid gap-6" onSubmit={submitProfile}>
            <div className="grid gap-5 rounded-[var(--radius-card)] bg-[var(--color-warm-white)] p-6 shadow-[var(--shadow-soft)]">
              <div>
                <h2 className="font-display text-2xl font-normal">기본 정보</h2>
                <p className="mt-2 text-sm leading-6 text-[var(--color-muted)]">프로필 카드와 독서기록에 함께 표시됩니다.</p>
              </div>
              <div className="grid gap-4 md:grid-cols-2">
                <TextField label="닉네임" maxLength={20} onChange={(value) => updateProfile("nickname", value)} placeholder="예: 김민준" required value={profile.nickname} />
                <TextField label="실명" maxLength={50} onChange={(value) => updateProfile("realName", value)} placeholder="예: 김민준" required value={profile.realName} />
                <TextField inputType="date" label="생년월일" max={new Date().toISOString().slice(0, 10)} onChange={(value) => updateProfile("birthDate", value)} value={profile.birthDate} />
                <div className="grid gap-2">
                  <span className="text-sm text-[var(--color-charcoal)]">공개 표시 방식</span>
                  <div className="grid gap-2 sm:grid-cols-2">
                    {[
                      ["REAL_NAME", "실명 공개", "실명으로 신뢰감 있게 보여줍니다."],
                      ["NICKNAME", "닉네임 공개", "모임 안에서 쓰는 이름으로 보여줍니다."],
                    ].map(([value, title, description]) => (
                      <button
                        className={`rounded-[var(--radius-card)] border p-4 text-left transition ${profile.displayNameType === value ? "border-[var(--color-deep-green)] bg-[rgba(47,90,67,0.08)]" : "border-[var(--color-line)] bg-[var(--color-warm-white)]"}`}
                        key={value}
                        onClick={() => updateProfile("displayNameType", value)}
                        type="button"
                      >
                        <span className="block text-sm text-[var(--color-ink)]">{title}</span>
                        <span className="mt-1 block text-xs leading-5 text-[var(--color-muted)]">{description}</span>
                      </button>
                    ))}
                  </div>
                </div>
              </div>
              <TextField label="한 줄 소개" maxLength={80} onChange={(value) => updateProfile("oneLineIntro", value)} placeholder="예: 작게 읽고 꾸준히 실행합니다." required value={profile.oneLineIntro} />
              <label className="grid gap-2 text-sm text-[var(--color-charcoal)]">
                <span>프로필 사진</span>
                <input
                  accept="image/jpeg,image/png,image/webp"
                  className="min-h-12 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-base outline-none transition file:mr-4 file:rounded-full file:border-0 file:bg-[var(--color-deep-green)] file:px-4 file:py-2 file:text-sm file:text-[var(--color-warm-white)] focus:border-[var(--color-deep-green)]"
                  onChange={(event) => setProfileImageFile(event.target.files?.[0] ?? null)}
                  type="file"
                />
                <span className="text-xs text-[var(--color-muted)]">jpg, png, webp 이미지를 10MB 이하로 등록할 수 있습니다.</span>
              </label>
            </div>

            <div className="grid gap-5 rounded-[var(--radius-card)] bg-[var(--color-warm-white)] p-6 shadow-[var(--shadow-soft)]">
              <div>
                <h2 className="flex items-center gap-2 font-display text-2xl font-normal">관심 분야 <RequiredMark /></h2>
                <p className="mt-2 text-sm leading-6 text-[var(--color-muted)]">1개 이상, 최대 5개까지 선택할 수 있습니다.</p>
              </div>
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

            <div className="grid gap-5 rounded-[var(--radius-card)] bg-[var(--color-warm-white)] p-6 shadow-[var(--shadow-soft)]">
              <div>
                <h2 className="font-display text-2xl font-normal">성장 기록</h2>
                <p className="mt-2 text-sm leading-6 text-[var(--color-muted)]">긴 문장은 부담 없이 초안처럼 적어도 됩니다.</p>
              </div>
              <TextAreaField label="50살의 나" maxLength={1000} onChange={(value) => updateProfile("futureMeAt50", value)} placeholder="50살의 나는 어떤 모습이면 좋을까요?" required value={profile.futureMeAt50} />
              <TextAreaField label="가입 이유" maxLength={1000} onChange={(value) => updateProfile("joinReason", value)} placeholder="이 모임에서 얻고 싶은 것을 적어 주세요." value={profile.joinReason} />
              <TextAreaField label="현재 고민" maxLength={1000} onChange={(value) => updateProfile("currentConcern", value)} placeholder="요즘 가장 자주 생각하는 고민은 무엇인가요?" value={profile.currentConcern} />
              <TextAreaField label="3년 뒤 목표" maxLength={1000} onChange={(value) => updateProfile("threeYearGoal", value)} placeholder="3년 뒤에 만들고 싶은 변화나 목표를 적어 주세요." value={profile.threeYearGoal} />
            </div>

            <button className="min-h-12 rounded-[var(--radius-card)] bg-[var(--color-deep-green)] px-5 text-sm font-normal !text-[var(--color-warm-white)] shadow-[var(--shadow-soft)]" style={{ color: "var(--color-warm-white)" }}>
              프로필 저장
            </button>
          </form>
        )}

        {step === "done" && (
          <div className="mt-8 rounded-[var(--radius-card)] bg-[var(--color-warm-white)] p-6 shadow-[var(--shadow-soft)]">
            <h2 className="text-xl font-normal">성장 프로필이 만들어졌습니다.</h2>
            <a className="mt-5 inline-flex min-h-11 items-center rounded-[var(--radius-card)] bg-[var(--color-deep-green)] px-5 text-sm !text-[var(--color-warm-white)]" href="/mypage" style={{ color: "var(--color-warm-white)" }}>
              마이페이지로 이동
            </a>
          </div>
        )}
      </section>
    </main>
  );
}

function TextField({
  label,
  value,
  onChange,
  placeholder,
  required = false,
  maxLength,
  inputType = "text",
  max,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  required?: boolean;
  maxLength?: number;
  inputType?: string;
  max?: string;
}) {
  return (
    <label className="grid gap-2 text-sm text-[var(--color-charcoal)]">
      <span className="flex items-center gap-2">{label} {required && <RequiredMark />}</span>
      <input
        className="min-h-12 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 text-base outline-none transition placeholder:text-[rgba(111,103,93,0.58)] focus:border-[var(--color-deep-green)]"
        max={max}
        maxLength={maxLength}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        required={required}
        type={inputType}
        value={value}
      />
      {maxLength && <span className="text-xs text-[var(--color-muted)]">{value.length}/{maxLength}</span>}
    </label>
  );
}

function TextAreaField({
  label,
  value,
  onChange,
  placeholder,
  required = false,
  maxLength,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  required?: boolean;
  maxLength?: number;
}) {
  return (
    <label className="grid gap-2 text-sm text-[var(--color-charcoal)]">
      <span className="flex items-center gap-2">{label} {required && <RequiredMark />}</span>
      <textarea
        className="min-h-32 resize-y rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-3 text-base leading-7 outline-none transition placeholder:text-[rgba(111,103,93,0.58)] focus:border-[var(--color-deep-green)]"
        maxLength={maxLength}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        required={required}
        value={value}
      />
      {maxLength && <span className="text-xs text-[var(--color-muted)]">{value.length}/{maxLength}</span>}
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
