"use client";

import { AuthGate } from "@/components/AuthGate";

export default function MyPage() {
  return (
    <main className="min-h-screen bg-[var(--color-ivory)] px-5 py-8 text-[var(--color-ink)]">
      <section className="mx-auto max-w-3xl">
        <AuthGate required="MEMBER">
          {(user) => (
            <>
              <p className="text-sm font-medium text-[var(--color-bronze)]">My Archive</p>
              <h1 className="mt-3 text-3xl font-semibold">{user.displayName}님의 성장 기록</h1>
              <p className="mt-4 text-sm text-[var(--color-charcoal)]">온보딩을 완료한 멤버 전용 화면입니다.</p>
            </>
          )}
        </AuthGate>
      </section>
    </main>
  );
}
