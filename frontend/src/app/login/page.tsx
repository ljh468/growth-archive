"use client";

import { kakaoLoginUrl } from "@/lib/api";
import { Suspense } from "react";
import { useEffect, useState } from "react";
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
  const [loginPending, setLoginPending] = useState(false);
  const [statusMessage, setStatusMessage] = useState("");

  useEffect(() => {
    const healthUrl = kakaoLoginUrl().replace("/auth/kakao/login", "/health");
    fetch(healthUrl, { cache: "no-store", credentials: "include" }).catch(() => undefined);
  }, []);

  function startKakaoLogin() {
    setLoginPending(true);
    setStatusMessage("카카오로 이동 중입니다.");
    window.setTimeout(() => {
      setStatusMessage("연결이 지연되고 있습니다. 잠시만 기다려 주세요.");
    }, 5000);
    window.setTimeout(() => {
      window.location.assign(kakaoLoginUrl());
    }, 50);
  }

  return (
    <main className="min-h-screen bg-[var(--color-ivory)] px-5 py-10 text-[var(--color-ink)] sm:py-16">
      <section className="mx-auto max-w-md">
        <p className="font-latin text-4xl text-[var(--color-bronze)]">Growth Archive</p>
        <h1 className="mt-3 text-3xl font-normal">카카오 로그인</h1>
        <p className="mt-4 text-sm leading-6 text-[var(--color-charcoal)]">
          부자습관 만들기 모임의 성장하는 사람들 인증과 온보딩을 위해 카카오 로그인을 먼저 진행합니다.
        </p>
        <div className="mt-6 border-y border-[var(--color-line)] py-4">
          <p className="text-xs leading-5 text-[var(--color-muted)]">
            부자습관 만들기 멤버를 위한 프라이빗 아카이브입니다.
          </p>
          <p className="mt-1 text-xs leading-5 text-[var(--color-muted)]">
            로그인 후 초대코드와 성장 프로필을 확인합니다.
          </p>
        </div>
        {deactivated && (
          <p className="mt-4 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-3 text-sm leading-6 text-[var(--color-wood-brown)]">
            비활성화된 계정입니다. 다시 이용하려면 운영진에게 문의해 주세요.
          </p>
        )}
        <button
          aria-busy={loginPending}
          className="mt-6 flex w-full items-center justify-center gap-2 rounded-[var(--radius-card)] bg-[#FEE500] px-4 py-3 text-sm font-normal text-[rgba(0,0,0,0.85)] shadow-[var(--shadow-soft)] transition hover:bg-[#F4D600] disabled:cursor-wait disabled:opacity-80 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--color-bronze)]"
          disabled={loginPending}
          onClick={startKakaoLogin}
          type="button"
        >
          {loginPending ? (
            <span className="size-4 animate-spin rounded-full border-2 border-[rgba(0,0,0,0.24)] border-t-[rgba(0,0,0,0.82)]" />
          ) : (
            <span className="grid size-5 place-items-center rounded-full bg-[rgba(0,0,0,0.82)] text-[11px] text-[#FEE500]">K</span>
          )}
          <span>{loginPending ? "카카오로 이동 중" : "카카오로 계속하기"}</span>
        </button>
        {statusMessage && (
          <p className="mt-3 text-center text-xs leading-5 text-[var(--color-muted)]">
            {statusMessage}
          </p>
        )}
      </section>
    </main>
  );
}
