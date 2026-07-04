"use client";

import { FormEvent, useEffect, useRef, useState, type RefObject } from "react";
import { apiGet, apiGetCurrentUser, apiPost, clearCurrentUserCache, type InterestTag, type OnboardingProfileDraft, uploadImage } from "@/lib/api";

type Step = "invite" | "terms" | "profile" | "done";
type ProfileField = "nickname" | "realName" | "birthDate" | "oneLineIntro" | "futureMeAt50" | "interestTagIds";
type FieldErrors = Partial<Record<ProfileField, string>>;

export default function OnboardingPage() {
  const [step, setStep] = useState<Step>("invite");
  const [message, setMessage] = useState<string | null>(null);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [inviteCode, setInviteCode] = useState("");
  const [interestTags, setInterestTags] = useState<InterestTag[]>([]);
  const [profileImageFile, setProfileImageFile] = useState<File | null>(null);
  const [profileSubmitting, setProfileSubmitting] = useState(false);
  const profileDraftLoadedRef = useRef(false);
  const nicknameRef = useRef<HTMLInputElement>(null);
  const realNameRef = useRef<HTMLInputElement>(null);
  const birthDateRef = useRef<HTMLInputElement>(null);
  const oneLineIntroRef = useRef<HTMLInputElement>(null);
  const futureMeAt50Ref = useRef<HTMLTextAreaElement>(null);
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
    apiGetCurrentUser().then((result) => {
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

  useEffect(() => {
    if (step !== "profile" || profileDraftLoadedRef.current) {
      return;
    }
    profileDraftLoadedRef.current = true;
    apiGet<OnboardingProfileDraft>("/onboarding/profile-draft").then((result) => {
      if (!result.success) {
        return;
      }
      const draft = result.data;
      setProfile((current) => ({
        ...current,
        nickname: draft.nickname ?? current.nickname,
        oneLineIntro: draft.oneLineIntro ?? current.oneLineIntro,
        displayNameType: draft.displayNameType ?? current.displayNameType,
        realName: "",
        birthDate: draft.birthDate ?? "",
        profileImageId: draft.profileImageId,
        futureMeAt50: draft.futureMeAt50 ?? current.futureMeAt50,
        joinReason: draft.joinReason ?? current.joinReason,
        currentConcern: draft.currentConcern ?? current.currentConcern,
        threeYearGoal: draft.threeYearGoal ?? current.threeYearGoal,
        interestTagIds: draft.interestTagIds ?? current.interestTagIds,
      }));
    });
  }, [step]);

  async function submitInvite(event: FormEvent) {
    event.preventDefault();
    const result = await apiPost<{ verified: boolean }>("/onboarding/invite-code", { code: inviteCode });
    if (!result.success) {
      setMessage(result.error?.message ?? "초대코드를 확인하지 못했습니다.");
      return;
    }
    clearCurrentUserCache();
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
    clearCurrentUserCache();
    setMessage(null);
    setStep("profile");
  }

  async function submitProfile(event: FormEvent) {
    event.preventDefault();
    if (profileSubmitting) {
      return;
    }
    setProfileSubmitting(true);
    setProfileError(null);
    if (!validateProfile()) {
      setProfileSubmitting(false);
      return;
    }
    const profileImageId = await uploadProfileImage();
    if (profileImageId === undefined) {
      setProfileSubmitting(false);
      return;
    }
    const result = await apiPost("/onboarding/profile", {
      ...profile,
      birthDate: profile.birthDate || null,
      profileImageId,
    });
    if (!result.success) {
      setProfileError(result.error?.message ?? "프로필을 저장하지 못했습니다.");
      setProfileSubmitting(false);
      return;
    }
    clearCurrentUserCache();
    setStep("done");
    setProfileSubmitting(false);
  }

  async function uploadProfileImage() {
    if (!profileImageFile) {
      return profile.profileImageId;
    }
    const result = await uploadImage(profileImageFile, "PROFILE");
    if (!result.success) {
      setProfileError(result.error?.message ?? "프로필 이미지를 업로드하지 못했습니다.");
      return undefined;
    }
    return result.data.imageId;
  }

  function updateProfile(key: keyof typeof profile, value: string) {
    setProfile((current) => ({ ...current, [key]: value }));
    setFieldErrors((current) => ({ ...current, [key]: undefined }));
  }

  function toggleInterestTag(tagId: number) {
    setFieldErrors((current) => ({ ...current, interestTagIds: undefined }));
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

  function validateProfile() {
    const errors: FieldErrors = {};
    if (!profile.nickname.trim()) {
      errors.nickname = "닉네임을 입력해 주세요.";
    }
    if (!profile.realName.trim()) {
      errors.realName = "실명을 입력해 주세요.";
    }
    if (!profile.birthDate) {
      errors.birthDate = "생년월일을 입력해 주세요.";
    }
    if (!profile.oneLineIntro.trim()) {
      errors.oneLineIntro = "한 줄 소개를 입력해 주세요.";
    }
    if (profile.interestTagIds.length === 0) {
      errors.interestTagIds = "관심 분야를 1개 이상 선택해 주세요.";
    }
    if (!profile.futureMeAt50.trim()) {
      errors.futureMeAt50 = "50살의 나를 입력해 주세요.";
    }
    setFieldErrors(errors);
    const firstInvalidField = (["nickname", "realName", "birthDate", "oneLineIntro", "futureMeAt50"] as const).find((field) => errors[field]);
    if (firstInvalidField) {
      focusProfileField(firstInvalidField);
    }
    return Object.keys(errors).length === 0;
  }

  function focusProfileField(field: Exclude<ProfileField, "interestTagIds">) {
    if (field === "nickname") {
      nicknameRef.current?.focus();
    } else if (field === "realName") {
      realNameRef.current?.focus();
    } else if (field === "birthDate") {
      birthDateRef.current?.focus();
    } else if (field === "oneLineIntro") {
      oneLineIntroRef.current?.focus();
    } else if (field === "futureMeAt50") {
      futureMeAt50Ref.current?.focus();
    }
  }

  return (
    <main className="min-h-screen w-full overflow-x-hidden bg-[var(--color-ivory)] px-4 py-8 text-[var(--color-ink)] sm:px-5 sm:py-10">
      <section className="mx-auto w-full max-w-4xl min-w-0">
        <p className="font-latin text-2xl leading-none text-[var(--color-bronze)] sm:text-4xl">People Onboarding</p>
        <div className="mt-3 max-w-2xl sm:mt-4">
          <h1 className="font-display text-2xl font-normal leading-[1.2] sm:text-4xl">성장 프로필 만들기</h1>
          <p className="mt-4 hidden text-sm leading-7 text-[var(--color-muted)] sm:block">
            모임 안에서 어떤 사람으로 기억되고 싶은지 가볍게 적어 주세요. 필수 항목만 먼저 채우고, 나머지는 나중에 마이페이지에서 고칠 수 있습니다.
          </p>
        </div>
        {message && <p className="mt-6 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4 text-sm text-[var(--color-charcoal)]">{message}</p>}

        {step === "invite" && (
          <form className="mt-6 grid max-w-xl gap-4 rounded-[var(--radius-card)] bg-[var(--color-warm-white)] p-5 shadow-[var(--shadow-soft)] sm:mt-8 sm:gap-5 sm:p-6" onSubmit={submitInvite}>
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
          <form className="mt-6 grid max-w-xl gap-4 rounded-[var(--radius-card)] bg-[var(--color-warm-white)] p-5 shadow-[var(--shadow-soft)] sm:mt-8 sm:p-6" onSubmit={submitTerms}>
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
          <form className="mt-6 grid w-full min-w-0 gap-4 sm:mt-8 sm:gap-5" noValidate onSubmit={submitProfile}>
            <div className="grid min-w-0 gap-4 overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-warm-white)] p-5 shadow-[var(--shadow-soft)] sm:p-6">
              <div>
                <h2 className="font-display text-xl font-normal sm:text-2xl">기본 정보</h2>
                <p className="mt-2 hidden text-sm leading-6 text-[var(--color-muted)] sm:block">프로필 카드와 독서기록에 함께 표시됩니다.</p>
              </div>
              <div className="grid min-w-0 gap-4 md:grid-cols-2">
                <TextField error={fieldErrors.nickname} inputRef={nicknameRef} label="닉네임" maxLength={20} onChange={(value) => updateProfile("nickname", value)} placeholder="예: 김민준" required value={profile.nickname} />
                <TextField error={fieldErrors.realName} inputRef={realNameRef} label="실명" maxLength={50} onChange={(value) => updateProfile("realName", value)} placeholder="예: 김민준" required value={profile.realName} />
                <TextField compact error={fieldErrors.birthDate} inputRef={birthDateRef} inputType="date" label="생년월일" max={new Date().toISOString().slice(0, 10)} onChange={(value) => updateProfile("birthDate", value)} required value={profile.birthDate} />
                <div className="grid min-w-0 gap-2">
                  <span className="text-sm text-[var(--color-charcoal)]">공개 표시 방식</span>
                  <div className="grid min-w-0 gap-2 sm:grid-cols-2">
                    {[
                      ["REAL_NAME", "실명 공개", "실명으로 신뢰감 있게 보여줍니다."],
                      ["NICKNAME", "닉네임 공개", "모임 안에서 쓰는 이름으로 보여줍니다."],
                    ].map(([value, title, description]) => (
                      <button
                        className={`min-w-0 rounded-[var(--radius-card)] border p-3 text-left transition ${profile.displayNameType === value ? "border-[var(--color-deep-green)] bg-[rgba(47,90,67,0.08)]" : "border-[var(--color-line)] bg-[var(--color-warm-white)]"}`}
                        key={value}
                        onClick={() => updateProfile("displayNameType", value)}
                        type="button"
                      >
                        <span className="block text-sm text-[var(--color-ink)]">{title}</span>
                        <span className="mt-1 hidden text-[11px] leading-5 text-[var(--color-muted)] sm:block">{description}</span>
                      </button>
                    ))}
                  </div>
                </div>
              </div>
              <TextField error={fieldErrors.oneLineIntro} inputRef={oneLineIntroRef} label="한 줄 소개" maxLength={80} onChange={(value) => updateProfile("oneLineIntro", value)} placeholder="예: 작게 읽고 꾸준히 실행합니다." required value={profile.oneLineIntro} />
              <label className="grid min-w-0 gap-2 text-sm text-[var(--color-charcoal)]">
                <span>프로필 사진</span>
                <input
                  accept="image/jpeg,image/png,image/webp,image/heic,image/heif,.heic,.heif"
                  className="block min-h-12 w-full min-w-0 max-w-full rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-2 py-2 text-sm outline-none transition file:mr-2 file:rounded-full file:border-0 file:bg-[var(--color-deep-green)] file:px-3 file:py-2 file:text-xs file:text-[var(--color-warm-white)] focus:border-[var(--color-deep-green)] sm:px-4 sm:text-base sm:file:mr-4 sm:file:px-4 sm:file:text-sm"
                  onChange={(event) => setProfileImageFile(event.target.files?.[0] ?? null)}
                  type="file"
                />
                <span className="hidden text-xs text-[var(--color-muted)] sm:inline">jpg, png, webp 이미지를 10MB 이하로 등록할 수 있습니다.</span>
              </label>
            </div>

            <div className="grid min-w-0 gap-4 overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-warm-white)] p-5 shadow-[var(--shadow-soft)] sm:p-6">
              <div>
                <h2 className="flex items-center gap-2 font-display text-xl font-normal sm:text-2xl">관심 분야 <RequiredMark /></h2>
                <p className="mt-1 hidden text-sm leading-6 text-[var(--color-muted)] sm:block">1개 이상, 최대 5개까지 선택할 수 있습니다.</p>
              </div>
              <div className="flex min-w-0 flex-wrap gap-2">
                {interestTags.map((tag) => {
                  const selected = profile.interestTagIds.includes(tag.id);
                  return (
                    <button
                      className={`touch-manipulation rounded-full border px-2.5 py-1 text-[11px] transition-colors duration-75 ${selected ? "border-[var(--color-deep-green)] bg-[var(--color-deep-green)] !text-[var(--color-warm-white)]" : "border-[var(--color-line)] bg-[var(--color-warm-white)] text-[var(--color-charcoal)] hover:border-[var(--color-deep-green)]"}`}
                      key={tag.id}
                      onClick={() => {
                        toggleInterestTag(tag.id);
                        setProfileError(null);
                      }}
                      style={selected ? { color: "var(--color-warm-white)" } : undefined}
                      type="button"
                    >
                      {tag.name}
                    </button>
                  );
                })}
              </div>
              {fieldErrors.interestTagIds && <p className="text-sm leading-6 text-[#9f3f2f]">{fieldErrors.interestTagIds}</p>}
            </div>

            <div className="grid min-w-0 gap-4 overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-warm-white)] p-5 shadow-[var(--shadow-soft)] sm:gap-5 sm:p-6">
              <div>
                <h2 className="font-display text-xl font-normal sm:text-2xl">성장 기록</h2>
                <p className="mt-2 hidden text-sm leading-6 text-[var(--color-muted)] sm:block">긴 문장은 부담 없이 초안처럼 적어도 됩니다.</p>
              </div>
              <TextAreaField error={fieldErrors.futureMeAt50} inputRef={futureMeAt50Ref} label="50살의 나" maxLength={1000} onChange={(value) => updateProfile("futureMeAt50", value)} placeholder="50살의 나는 어떤 모습이면 좋을까요?" required value={profile.futureMeAt50} />
              <TextAreaField label="가입 이유" maxLength={1000} onChange={(value) => updateProfile("joinReason", value)} placeholder="이 모임에서 얻고 싶은 것을 적어 주세요." value={profile.joinReason} />
              <TextAreaField label="현재 고민" maxLength={1000} onChange={(value) => updateProfile("currentConcern", value)} placeholder="요즘 가장 자주 생각하는 고민은 무엇인가요?" value={profile.currentConcern} />
              <TextAreaField label="3년 뒤 목표" maxLength={1000} onChange={(value) => updateProfile("threeYearGoal", value)} placeholder="3년 뒤에 만들고 싶은 변화나 목표를 적어 주세요." value={profile.threeYearGoal} />
            </div>

            {profileError && <p className="rounded-[var(--radius-card)] border border-[#c78473] bg-[rgba(159,63,47,0.08)] px-4 py-3 text-sm leading-6 text-[#9f3f2f]">{profileError}</p>}
            <button className="min-h-12 rounded-[var(--radius-card)] bg-[var(--color-deep-green)] px-5 text-sm font-normal !text-[var(--color-warm-white)] shadow-[var(--shadow-soft)]" style={{ color: "var(--color-warm-white)" }}>
              {profileSubmitting ? "이미지 압축 및 저장 중" : "프로필 저장"}
            </button>
          </form>
        )}

        {step === "done" && (
          <div className="mt-6 rounded-[var(--radius-card)] bg-[var(--color-warm-white)] p-5 shadow-[var(--shadow-soft)] sm:mt-8 sm:p-6">
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
  compact = false,
  error,
  inputRef,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  required?: boolean;
  maxLength?: number;
  inputType?: string;
  max?: string;
  compact?: boolean;
  error?: string;
  inputRef?: RefObject<HTMLInputElement | null>;
}) {
  return (
    <label className="grid min-w-0 gap-2 text-sm text-[var(--color-charcoal)]">
      <span className="flex items-center gap-2">{label} {required && <RequiredMark />}</span>
      <input
        aria-invalid={Boolean(error)}
        className={`${compact ? "min-h-10 px-3 text-sm" : "min-h-10 px-3 text-sm sm:min-h-12 sm:px-4 sm:text-base"} w-full min-w-0 rounded-[var(--radius-card)] border ${error ? "border-[#c78473]" : "border-[var(--color-line)]"} bg-[var(--color-warm-white)] outline-none transition placeholder:text-[rgba(111,103,93,0.58)] focus:border-[var(--color-deep-green)]`}
        max={max}
        maxLength={maxLength}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        ref={inputRef}
        required={required}
        type={inputType}
        value={value}
      />
      {error && <span className="text-sm leading-6 text-[#9f3f2f]">{error}</span>}
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
  error,
  inputRef,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  required?: boolean;
  maxLength?: number;
  error?: string;
  inputRef?: RefObject<HTMLTextAreaElement | null>;
}) {
  return (
    <label className="grid min-w-0 gap-2 text-sm text-[var(--color-charcoal)]">
      <span className="flex items-center gap-2">{label} {required && <RequiredMark />}</span>
      <textarea
        aria-invalid={Boolean(error)}
        className={`min-h-24 w-full min-w-0 resize-y rounded-[var(--radius-card)] border ${error ? "border-[#c78473]" : "border-[var(--color-line)]"} bg-[var(--color-warm-white)] px-3 py-3 text-sm leading-6 outline-none transition placeholder:text-[rgba(111,103,93,0.58)] focus:border-[var(--color-deep-green)] sm:min-h-32 sm:px-4 sm:text-base sm:leading-7`}
        maxLength={maxLength}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        ref={inputRef}
        required={required}
        value={value}
      />
      {error && <span className="text-sm leading-6 text-[#9f3f2f]">{error}</span>}
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
