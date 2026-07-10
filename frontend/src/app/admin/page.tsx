"use client";

import { useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section, SkeletonBlock } from "@/components/ui/primitives";
import { apiGet, type AdminDashboard } from "@/lib/api";

export default function AdminPage() {
  return (
    <AuthGate required="ADMIN">
      {() => <AdminDashboardContent />}
    </AuthGate>
  );
}

function AdminDashboardContent() {
  const [dashboard, setDashboard] = useState<AdminDashboard | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    apiGet<AdminDashboard>("/admin/dashboard")
      .then((result) => {
        if (!result.success) {
          setMessage(result.error?.message ?? "관리자 지표를 불러오지 못했습니다.");
          return;
        }
        setDashboard(result.data);
      })
      .catch(() => setMessage("관리자 지표를 불러오지 못했습니다."));
  }, []);

  return (
    <main>
      <Section>
        <div className="grid gap-5 sm:gap-8">
          <PageHeader eyebrow="Admin" title="운영 관리" description="회원, 초대코드, 태그, 추천책, 참여 현황, 모임과 후기를 관리합니다." />
          {message && <EmptyState title="상태" description={message} />}
          {dashboard ? (
            <div className="grid gap-2 sm:gap-4 md:grid-cols-4">
              <Metric label="활성 회원" value={dashboard.activeMemberCount} />
              <Metric label="독서기록" value={dashboard.activeReadingRecordCount} />
              <Metric label="모임" value={dashboard.meetingCount} />
              <Metric label="후기" value={dashboard.activeReviewCount} />
              <Metric label={`${dashboard.month} 참여 완료`} value={dashboard.participationCompletedCount} />
              <Metric label={`${dashboard.month} 참여 필요`} value={dashboard.participationIncompleteCount} />
              <Metric label="참여 대상" value={dashboard.participationTargetCount} />
              <Metric label="UNVERIFIED 책" value={dashboard.unverifiedBookCount} />
            </div>
          ) : (
            !message && <MetricSkeletonGrid />
          )}
          <div className="grid gap-2 sm:gap-4 md:grid-cols-3">
            <AdminLink title="회원 관리" description="회원 목록, 비활성화, 재활성화, 참여 시작월을 관리합니다." href="/admin/members" />
            <AdminLink title="초대코드" description="일반 회원용 코드와 운영진 코드를 확인하고 변경합니다." href="/admin/invite-code" />
            <AdminLink title="관심 태그" description="온보딩에서 선택 가능한 태그를 관리합니다." href="/admin/interest-tags" />
            <AdminLink title="추천책" description="라이브러리에 노출할 추천책의 순서, 문구, 노출 상태를 관리합니다." href="/admin/recommended-books" />
            <AdminLink title="책 검증" description="직접 등록된 UNVERIFIED 책을 확인하고 VERIFIED 처리합니다." href="/admin/books" />
            <AdminLink title="참여 현황" description="월별 참여 완료와 커피 후원 대상자를 확인합니다." href="/admin/participation" />
            <AdminLink title="모임 관리" description="정기모임 운영 정보와 소소모임 숨김/삭제를 처리합니다." href="/admin/meetings" />
            <AdminLink title="후기 관리" description="모임 후기를 내용 수정 없이 숨김/복구/삭제합니다." href="/admin/reviews" />
          </div>
        </div>
      </Section>
    </main>
  );
}

function MetricSkeletonGrid() {
  return (
    <div className="grid gap-2 sm:gap-4 md:grid-cols-4" aria-label="운영 지표 로딩 중">
      {Array.from({ length: 8 }).map((_, index) => (
        <Card key={index}>
          <div className="flex min-h-[28px] items-center justify-between gap-3 sm:block sm:min-h-[66px]">
            <SkeletonBlock className="h-4 w-20" />
            <SkeletonBlock className="h-5 w-8 sm:mt-3 sm:h-7 sm:w-12" />
          </div>
        </Card>
      ))}
    </div>
  );
}

function Metric({ label, value }: { label: string; value: number }) {
  return (
    <Card>
      <div className="flex items-center justify-between gap-3 sm:block">
        <p className="text-sm text-[var(--color-charcoal)]">{label}</p>
        <p className="font-display text-lg font-normal leading-none text-[var(--color-deep-green)] sm:mt-3 sm:text-2xl sm:text-[var(--color-ink)]">{value}</p>
      </div>
    </Card>
  );
}

function AdminLink({ title, description, href }: { title: string; description: string; href: string }) {
  return (
    <Card>
      <h2 className="text-base font-normal sm:text-lg">{title}</h2>
      <p className="mt-2 hidden min-h-12 text-sm leading-6 text-[var(--color-charcoal)] sm:block">{description}</p>
      <div className="mt-3 sm:mt-5">
        <Button href={href}>관리하기</Button>
      </div>
    </Card>
  );
}
