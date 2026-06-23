"use client";

import { AuthGate } from "@/components/AuthGate";
import { Button, Card, PageHeader, Section } from "@/components/ui/primitives";

export default function AdminPage() {
  return (
    <AuthGate required="ADMIN">
      {() => (
        <main>
          <Section>
            <div className="grid gap-8">
              <PageHeader eyebrow="Admin" title="운영 관리" description="활성 멤버이면서 ADMIN role인 사용자만 접근하는 운영 콘솔입니다." />
              <div className="grid gap-4 md:grid-cols-2">
                <Card>
                  <h2 className="text-lg font-semibold">이달의 추천책</h2>
                  <p className="mt-2 text-sm text-[var(--color-charcoal)]">라이브러리에 노출할 추천책을 관리합니다.</p>
                  <div className="mt-5">
                    <Button href="/admin/recommended-books">관리하기</Button>
                  </div>
                </Card>
                <Card>
                  <h2 className="text-lg font-semibold">콘텐츠 가시성</h2>
                  <p className="mt-2 text-sm text-[var(--color-charcoal)]">독서기록은 내용 수정 없이 숨김/삭제만 처리합니다.</p>
                </Card>
              </div>
            </div>
          </Section>
        </main>
      )}
    </AuthGate>
  );
}
