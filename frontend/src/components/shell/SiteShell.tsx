"use client";

import type { ReactNode } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { apiGetCurrentUser, apiPost, clearCurrentUserCache } from "@/lib/api";

const desktopNav = [
  ["독서기록 라이브러리", "/library"],
  ["모임", "/meetings"],
  ["사람들", "/people"],
  ["모임 후기", "/reviews"],
  ["소개", "/about"],
];

const bottomNav = [
  ["home", "홈", "/"],
  ["library", "라이브러리", "/library"],
  ["meetings", "모임", "/meetings"],
  ["people", "사람들", "/people"],
  ["my", "마이", "/mypage"],
];

const helperNav = [
  ["모임 후기", "/reviews"],
  ["소개", "/about"],
  ["이용약관", "/terms"],
  ["개인정보처리방침", "/privacy"],
];

export function SiteShell({ children }: { children: ReactNode }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const [memberReady, setMemberReady] = useState(false);
  const [adminReady, setAdminReady] = useState(false);
  const [pendingHref, setPendingHref] = useState("");
  const pathname = usePathname();
  const router = useRouter();
  const mobileBackHref = getMobileBackHref(pathname);

  useEffect(() => {
    apiGetCurrentUser().then((result) => {
      if (!result.success) {
        return;
      }
      const activeMember = result.data.onboardingCompleted && !result.data.deactivated;
      setMemberReady(activeMember);
      setAdminReady(result.data.accessLevel === "ADMIN" || (activeMember && result.data.role === "ADMIN"));
    });
  }, []);

  useEffect(() => {
    bottomNav.forEach(([, , href]) => router.prefetch(href));
  }, [router]);

  useEffect(() => {
    setPendingHref("");
  }, [pathname]);

  useEffect(() => {
    if (!pendingHref) {
      return;
    }
    const timer = window.setTimeout(() => setPendingHref(""), 8000);
    return () => window.clearTimeout(timer);
  }, [pendingHref]);

  async function logout() {
    await apiPost("/auth/logout");
    clearCurrentUserCache();
    setMemberReady(false);
    setAdminReady(false);
    window.location.href = "/";
  }

  function goMobileBack() {
    const referrer = document.referrer;
    const fromSameOrigin = referrer ? new URL(referrer).origin === window.location.origin : false;
    if (fromSameOrigin && window.history.length > 1) {
      router.back();
      return;
    }
    router.push(mobileBackHref ?? "/");
  }

  return (
    <div className="min-h-screen bg-[var(--color-ivory)] text-[var(--color-ink)]">
      <header className="fixed left-0 right-0 top-0 z-40 border-b border-[rgba(74,52,36,0.14)] bg-[rgba(255,254,250,0.98)] shadow-[0_10px_26px_rgba(63,47,34,0.08)] backdrop-blur md:sticky md:border-[var(--color-line)] md:bg-[var(--color-warm-white)] md:shadow-[0_8px_24px_rgba(63,47,34,0.04)]">
        <div className="mx-auto flex h-[3.25rem] max-w-6xl items-center justify-between px-3 sm:px-8 md:h-20 lg:px-10">
          <div className="flex min-w-0 items-center gap-1.5">
            {mobileBackHref && (
              <button
                aria-label="이전 화면으로 이동"
                className="grid size-9 shrink-0 touch-manipulation place-items-center rounded-full text-[var(--color-deep-green)] transition active:scale-[0.96] md:hidden"
                onClick={goMobileBack}
                type="button"
              >
                <span aria-hidden="true" className="translate-x-[-1px] text-[1.55rem] font-normal leading-none">‹</span>
              </button>
            )}
            <Link href="/" className="hidden min-w-0 leading-tight md:block">
              <span className="block truncate font-latin text-2xl text-[var(--color-bronze)]">Growth Archive</span>
            </Link>
            {!mobileBackHref && (
              <Link href="/" className="min-w-0 leading-tight md:hidden">
                <span className="block truncate font-latin text-[1.18rem] text-[var(--color-bronze)]">Growth Archive</span>
              </Link>
            )}
          </div>
          <nav className="hidden items-center gap-7 text-sm text-[var(--color-charcoal)] md:flex">
            {desktopNav.map(([label, href]) => (
              <Link className="transition hover:text-[var(--color-ink)]" key={href} href={href}>
                {label}
              </Link>
            ))}
          </nav>
          <div className="hidden items-center gap-4 text-sm md:flex">
            {adminReady && (
              <Link className="font-normal text-[var(--color-deep-green)] transition hover:text-[var(--color-wood-brown)]" href="/admin">
                운영 관리
              </Link>
            )}
            {memberReady ? (
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
            className="grid size-9 place-items-center rounded-full bg-[rgba(47,90,67,0.055)] text-[var(--color-deep-green)] transition active:scale-[0.96] md:hidden"
            onClick={() => setMenuOpen((value) => !value)}
            type="button"
          >
            <span className="grid gap-1" aria-hidden="true">
              <span className="block h-px w-4 bg-current" />
              <span className="block h-px w-4 bg-current" />
              <span className="block h-px w-4 bg-current" />
            </span>
          </button>
        </div>
        {menuOpen && (
          <nav className="grid border-t border-[var(--color-line)] bg-[var(--color-warm-white)] px-5 py-3 text-sm md:hidden">
            {adminReady && (
              <Link className="py-3 text-[var(--color-deep-green)]" href="/admin" onClick={() => setMenuOpen(false)}>
                운영 관리
              </Link>
            )}
            {helperNav.map(([label, href]) => (
              <Link className="py-3" key={href} href={href} onClick={() => setMenuOpen(false)}>
                {label}
              </Link>
            ))}
            {memberReady ? (
              <button className="py-3 text-left" onClick={logout} type="button">
                로그아웃
              </button>
            ) : (
              <Link className="py-3" href="/login" onClick={() => setMenuOpen(false)}>로그인</Link>
            )}
          </nav>
        )}
      </header>

      <div className="pt-[3.25rem] pb-[calc(6.25rem+env(safe-area-inset-bottom))] md:pt-0 md:pb-0">{children}</div>

      <nav
        className="fixed bottom-0 left-0 z-30 grid max-w-[100dvw] grid-cols-5 overflow-hidden border-t border-[rgba(74,52,36,0.16)] bg-[rgba(255,254,250,0.98)] px-1.5 pb-[calc(0.65rem+env(safe-area-inset-bottom))] pt-1.5 text-center text-[10px] shadow-[0_-14px_34px_rgba(63,47,34,0.12)] backdrop-blur md:hidden"
        style={{ width: "100dvw" }}
      >
        {pendingHref && (
          <span className="absolute left-0 top-0 h-px w-full overflow-hidden bg-[rgba(31,77,58,0.12)]">
            <span className="block h-full w-1/2 animate-[bottomNavProgress_1.1s_ease-in-out_infinite] bg-[var(--color-deep-green)]" />
          </span>
        )}
        {bottomNav.map(([icon, label, href]) => {
          const active = href === "/" ? pathname === "/" : pathname === href || pathname.startsWith(`${href}/`);
          const pending = pendingHref === href && !active;

          return (
          <Link
            aria-current={active ? "page" : undefined}
            aria-busy={pending}
            className={[
              "relative flex min-w-0 flex-col items-center justify-center gap-0.5 rounded-[var(--radius-card)] px-0.5 py-1.5 leading-none transition active:scale-[0.98]",
              active || pending ? "bg-[rgba(47,90,67,0.1)] text-[var(--color-deep-green)]" : "text-[var(--color-muted)] hover:text-[var(--color-charcoal)]",
            ].join(" ")}
            href={href}
            key={href}
            onClick={(event) => {
              if (active || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) {
                return;
              }
              setPendingHref(href);
            }}
          >
            <BottomNavIcon name={icon} active={active} />
            {label}
          </Link>
          );
        })}
      </nav>

      <footer className="hidden border-t border-[var(--color-line)] bg-[var(--color-warm-white)] md:block">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-10 py-6 text-sm text-[var(--color-charcoal)]">
          <p>부자습관 만들기 모임</p>
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

function getMobileBackHref(pathname: string) {
  if (pathname === "/") {
    return null;
  }

  const tabRoots = new Set(["/library", "/meetings", "/people", "/mypage", "/my"]);
  if (tabRoots.has(pathname)) {
    return null;
  }

  if (pathname === "/reviews" || pathname === "/about" || pathname === "/terms" || pathname === "/privacy" || pathname === "/login" || pathname === "/onboarding") {
    return null;
  }

  if (pathname.startsWith("/books/")) {
    return "/library";
  }
  if (pathname === "/reading-records/new") {
    return "/library";
  }
  if (pathname === "/meetings/new" || pathname.startsWith("/meetings/")) {
    return "/meetings";
  }
  if (pathname === "/reviews/new" || pathname.startsWith("/reviews/")) {
    return "/reviews";
  }
  if (pathname.startsWith("/people/")) {
    return "/people";
  }
  if (pathname.startsWith("/mypage/")) {
    return "/mypage";
  }
  if (pathname.startsWith("/admin/")) {
    return "/admin";
  }
  if (pathname.startsWith("/onboarding/")) {
    return "/onboarding";
  }

  return "/";
}


function BottomNavIcon({ active, name }: { active: boolean; name: string }) {
  const strokeWidth = active ? 2.2 : 1.8;

  if (name === "home") {
    return (
      <svg aria-hidden="true" className="size-5" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth={strokeWidth} viewBox="0 0 24 24">
        <path d="M4 10.5 12 4l8 6.5" />
        <path d="M6.5 10.5V20h11v-9.5" />
      </svg>
    );
  }

  if (name === "library") {
    return (
      <svg aria-hidden="true" className="size-5" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth={strokeWidth} viewBox="0 0 24 24">
        <path d="M5 5.5h7a3 3 0 0 1 3 3V19a3 3 0 0 0-3-3H5z" />
        <path d="M15 8.5a3 3 0 0 1 3-3h1v10.5h-1a3 3 0 0 0-3 3" />
      </svg>
    );
  }

  if (name === "meetings") {
    return (
      <svg aria-hidden="true" className="size-5" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth={strokeWidth} viewBox="0 0 24 24">
        <path d="M8 10a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
        <path d="M16 11a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z" />
        <path d="M3.5 20a4.5 4.5 0 0 1 9 0" />
        <path d="M13.5 19a3.5 3.5 0 0 1 7 0" />
      </svg>
    );
  }

  if (name === "people") {
    return (
      <svg aria-hidden="true" className="size-5" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth={strokeWidth} viewBox="0 0 24 24">
        <path d="M12 11a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7Z" />
        <path d="M5 20a7 7 0 0 1 14 0" />
      </svg>
    );
  }

  return (
    <svg aria-hidden="true" className="size-5" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth={strokeWidth} viewBox="0 0 24 24">
      <path d="M12 12a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7Z" />
      <path d="M7 20h10" />
      <path d="M8.5 17.5a5 5 0 0 1 7 0" />
    </svg>
  );
}
