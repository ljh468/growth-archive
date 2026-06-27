"use client";

import type { ReactNode } from "react";
import Link from "next/link";
import { useEffect, useState } from "react";
import { apiGet, apiPost, type CurrentUser } from "@/lib/api";

const desktopNav = [
  ["독서기록 라이브러리", "/library"],
  ["모임", "/meetings"],
  ["성장하는 사람들", "/people"],
  ["모임 후기", "/reviews"],
  ["소개", "/about"],
];

const bottomNav = [
  ["홈", "/"],
  ["라이브러리", "/library"],
  ["모임", "/meetings"],
  ["사람들", "/people"],
  ["마이", "/mypage"],
];

const helperNav = [
  ["모임 후기", "/reviews"],
  ["소개", "/about"],
  ["이용약관", "/terms"],
  ["개인정보처리방침", "/privacy"],
];

export function SiteShell({ children }: { children: ReactNode }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);
  const [memberReady, setMemberReady] = useState(false);

  useEffect(() => {
    apiGet<CurrentUser>("/auth/me").then((result) => {
      if (!result.success) {
        return;
      }
      setAuthenticated(result.data.authenticated);
      setMemberReady(result.data.onboardingCompleted && !result.data.deactivated);
    });
  }, []);

  async function logout() {
    await apiPost("/auth/logout");
    setAuthenticated(false);
    setMemberReady(false);
    window.location.href = "/";
  }

  return (
    <div className="min-h-screen bg-[var(--color-ivory)] text-[var(--color-ink)]">
      <header className="sticky top-0 z-20 border-b border-[var(--color-line)] bg-[var(--color-warm-white)] shadow-[0_8px_24px_rgba(63,47,34,0.04)]">
        <div className="mx-auto flex h-20 max-w-6xl items-center justify-between px-5 sm:px-8 lg:px-10">
          <Link href="/" className="leading-tight">
            <span className="block font-latin text-2xl text-[var(--color-bronze)]">Growth Archive</span>
            <span className="block text-sm font-normal">부자습관 만들기 모임</span>
          </Link>
          <nav className="hidden items-center gap-7 text-sm text-[var(--color-charcoal)] md:flex">
            {desktopNav.map(([label, href]) => (
              <Link className="transition hover:text-[var(--color-ink)]" key={href} href={href}>
                {label}
              </Link>
            ))}
          </nav>
          <div className="hidden items-center gap-4 text-sm md:flex">
            {authenticated ? (
              <button className="text-[var(--color-charcoal)] transition hover:text-[var(--color-ink)]" onClick={logout} type="button">
                로그아웃
              </button>
            ) : (
              <Link className="text-[var(--color-charcoal)]" href="/login">로그인</Link>
            )}
            <Link className="font-normal text-[var(--color-charcoal)] transition hover:text-[var(--color-ink)]" href={memberReady ? "/mypage" : "/onboarding"}>
              마이페이지
            </Link>
          </div>
          <button
            aria-expanded={menuOpen}
            aria-label="보조 메뉴"
            className="grid size-11 place-items-center rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] md:hidden"
            onClick={() => setMenuOpen((value) => !value)}
            type="button"
          >
            <span className="grid gap-1" aria-hidden="true">
              <span className="block h-px w-5 bg-[var(--color-ink)]" />
              <span className="block h-px w-5 bg-[var(--color-ink)]" />
              <span className="block h-px w-5 bg-[var(--color-ink)]" />
            </span>
          </button>
        </div>
        {menuOpen && (
          <nav className="grid border-t border-[var(--color-line)] bg-[var(--color-warm-white)] px-5 py-3 text-sm md:hidden">
            {helperNav.map(([label, href]) => (
              <Link className="py-3" key={href} href={href}>
                {label}
              </Link>
            ))}
            {authenticated ? (
              <button className="py-3 text-left" onClick={logout} type="button">
                로그아웃
              </button>
            ) : (
              <Link className="py-3" href="/login">로그인</Link>
            )}
          </nav>
        )}
      </header>

      <div className="pb-[calc(5.5rem+env(safe-area-inset-bottom))] md:pb-0">{children}</div>

      <nav className="fixed inset-x-0 bottom-0 z-20 grid grid-cols-5 border-t border-[var(--color-line)] bg-[var(--color-warm-white)] px-2 pb-[calc(0.5rem+env(safe-area-inset-bottom))] pt-2 text-center text-xs text-[var(--color-charcoal)] shadow-[0_-8px_24px_rgba(63,47,34,0.05)] md:hidden">
        {bottomNav.map(([label, href]) => (
          <Link className="py-2" href={href} key={href}>
            {label}
          </Link>
        ))}
      </nav>

      <footer className="hidden border-t border-[var(--color-line)] bg-[var(--color-warm-white)] md:block">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-10 py-6 text-sm text-[var(--color-charcoal)]">
          <p>
            부자습관 만들기 모임 <span className="font-latin text-2xl text-[var(--color-bronze)]">Growth Archive</span>
          </p>
          <nav className="flex gap-5">
            {helperNav.map(([label, href]) => (
              <Link key={href} href={href}>
                {label}
              </Link>
            ))}
          </nav>
        </div>
      </footer>
    </div>
  );
}
