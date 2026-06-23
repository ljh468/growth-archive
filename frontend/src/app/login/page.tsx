"use client";

import { kakaoLoginUrl } from "@/lib/api";

export default function LoginPage() {
  return (
    <main className="min-h-screen bg-[var(--color-ivory)] px-5 py-10 text-[var(--color-ink)]">
      <section className="mx-auto max-w-md">
        <p className="text-sm font-medium text-[var(--color-bronze)]">Growth Archive</p>
        <h1 className="mt-3 text-3xl font-semibold">카카오 로그인</h1>
        <p className="mt-4 text-sm leading-6 text-[var(--color-charcoal)]">
          부자습관 만들기 멤버 인증과 온보딩을 위해 카카오 로그인을 먼저 진행합니다.
        </p>
        <button
          className="mt-8 w-full border border-[var(--color-ink)] bg-[var(--color-ink)] px-4 py-3 text-sm font-semibold text-[var(--color-warm-white)]"
          onClick={() => {
            window.location.href = kakaoLoginUrl();
          }}
          type="button"
        >
          카카오로 계속하기
        </button>
      </section>
    </main>
  );
}
