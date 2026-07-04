"use client";

import { kakaoLoginUrl } from "@/lib/api";
import { Suspense } from "react";
import { useSearchParams } from "next/navigation";

export default function LoginPage() {
  return (
    <Suspense fallback={null}>
      <LoginContent />
    </Suspense>
  );
}

function LoginContent() {
  const searchParams = useSearchParams();
  const deactivated = searchParams.get("deactivated") === "true";

  return (
    <main className="min-h-screen bg-[var(--color-ivory)] px-5 py-10 text-[var(--color-ink)]">
      <section className="mx-auto max-w-md">
        <p className="font-latin text-4xl text-[var(--color-bronze)]">Growth Archive</p>
        <h1 className="mt-3 text-3xl font-normal">카카오 로그인</h1>
        <p className="mt-4 text-sm leading-6 text-[var(--color-charcoal)]">
          부자습관 만들기 모임의 성장하는 사람들 인증과 온보딩을 위해 카카오 로그인을 먼저 진행합니다.
        </p>
        {deactivated && (
          <p className="mt-4 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-3 text-sm leading-6 text-[var(--color-wood-brown)]">
            비활성화된 계정입니다. 다시 이용하려면 운영진에게 문의해 주세요.
          </p>
        )}
        <button
          className="mt-8 flex w-full items-center justify-center gap-2 rounded-[var(--radius-card)] bg-[#FEE500] px-4 py-3 text-sm font-normal text-[rgba(0,0,0,0.85)] shadow-[var(--shadow-soft)] transition hover:bg-[#F4D600] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--color-bronze)]"
          onClick={() => {
            window.location.href = kakaoLoginUrl();
          }}
          type="button"
        >
          <span className="grid size-5 place-items-center rounded-full bg-[rgba(0,0,0,0.82)] text-[11px] text-[#FEE500]">K</span>
          <span>카카오로 계속하기</span>
        </button>
      </section>
    </main>
  );
}
