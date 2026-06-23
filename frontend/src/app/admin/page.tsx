"use client";

import { AuthGate } from "@/components/AuthGate";

export default function AdminPage() {
  return (
    <main className="min-h-screen bg-[var(--color-ivory)] px-5 py-8 text-[var(--color-ink)]">
      <section className="mx-auto max-w-3xl">
        <AuthGate required="ADMIN">
          {() => (
            <>
              <p className="text-sm font-medium text-[var(--color-bronze)]">Admin</p>
              <h1 className="mt-3 text-3xl font-semibold">운영 관리</h1>
              <p className="mt-4 text-sm text-[var(--color-charcoal)]">활성 멤버이면서 ADMIN role인 사용자만 접근합니다.</p>
            </>
          )}
        </AuthGate>
      </section>
    </main>
  );
}
