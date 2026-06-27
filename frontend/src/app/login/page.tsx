"use client";

import { kakaoLoginUrl } from "@/lib/api";

export default function LoginPage() {
  return (
    <main className="min-h-screen bg-[var(--color-ivory)] px-5 py-10 text-[var(--color-ink)]">
      <section className="mx-auto max-w-md">
        <p className="font-latin text-4xl text-[var(--color-bronze)]">Growth Archive</p>
        <h1 className="mt-3 text-3xl font-normal">카카오 로그인</h1>
        <p className="mt-4 text-sm leading-6 text-[var(--color-charcoal)]">
          부자습관 만들기 모임의 성장하는 사람들 인증과 온보딩을 위해 카카오 로그인을 먼저 진행합니다.
        </p>
        <button
          className="mt-8 w-full rounded-[var(--radius-card)] bg-[var(--color-deep-green)] px-4 py-3 text-sm font-normal !text-[var(--color-warm-white)] shadow-[var(--shadow-soft)] transition hover:bg-[var(--color-wood-brown)] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--color-bronze)]"
          onClick={() => {
            window.location.href = kakaoLoginUrl();
          }}
          style={{ color: "var(--color-warm-white)" }}
          type="button"
        >
          카카오로 계속하기
        </button>
      </section>
    </main>
  );
}
