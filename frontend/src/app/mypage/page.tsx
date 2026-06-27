"use client";

import { useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiGet, type MyDashboard } from "@/lib/api";

export default function MyPage() {
  return (
    <AuthGate required="MEMBER">
      {() => <MyPageContent />}
    </AuthGate>
  );
}

function MyPageContent() {
  const [dashboard, setDashboard] = useState<MyDashboard | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    apiGet<MyDashboard>("/me/dashboard")
      .then((result) => {
        if (!result.success) {
          setError(result.error?.message ?? "마이페이지를 불러오지 못했습니다.");
          return;
        }
        setDashboard(result.data);
      })
      .catch(() => setError("마이페이지를 불러오지 못했습니다."));
  }, []);

  return (
    <main>
      <Section>
        {error && <EmptyState title="불러오기 실패" description={error} />}
        {!dashboard && !error && <EmptyState title="불러오는 중입니다." description="내 성장 기록을 확인하고 있습니다." />}
        {dashboard && (
          <div className="grid gap-8">
            <div className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
              <div className="flex items-center gap-4">
                {dashboard.profile.profileImageUrl ? (
                  <img alt="" className="size-16 rounded-full object-cover shadow-[var(--shadow-soft)]" src={dashboard.profile.profileImageUrl} />
                ) : (
                  <div className="flex size-16 items-center justify-center rounded-full bg-[var(--color-deep-green)] text-xl text-[var(--color-warm-white)]">
                    {dashboard.profile.displayName.slice(0, 1)}
                  </div>
                )}
                <PageHeader eyebrow="My Archive" title={`${dashboard.profile.displayName}님의 성장 기록`} description={dashboard.profile.oneLineIntro} />
              </div>
            </div>
            <Card>
              <div className="flex flex-wrap items-center justify-between gap-4">
                <div>
                  <Tag>{dashboard.participation.month}</Tag>
                  <h2 className="mt-4 text-2xl font-normal">{dashboard.participation.completed ? "이번 달의 흔적이 채워졌습니다" : "이번 달 성장 기록을 기다리고 있어요"}</h2>
                  <p className="mt-2 text-sm text-[var(--color-charcoal)]">
                    {dashboard.participation.completed ? "읽고 실행한 기록이 이번 달 아카이브에 남았습니다." : "독서기록이나 실행계획 중 하나만 남기면 이번 달 참여가 채워집니다."}
                    {" "}독서기록 {dashboard.participation.readingRecordCount}개 · 액션플랜 {dashboard.participation.actionPlanCount}개
                  </p>
                </div>
                {dashboard.participation.coffeeSupportTarget && <Tag>투썸 아메리카노 1잔</Tag>}
              </div>
            </Card>
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
              <Button href="/reading-records/new">독서기록 작성</Button>
              <Button href="/mypage/action-plans" variant="secondary">
                실행계획 작성
              </Button>
              <Button href="/mypage/reflections" variant="secondary">
                월간회고 작성
              </Button>
              <Button href="/meetings/new" variant="secondary">
                소소모임 만들기
              </Button>
            </div>
            <div className="grid gap-4 md:grid-cols-5">
              <Stat label="독서" value={dashboard.quickStats.readingRecordCount} />
              <Stat label="액션플랜" value={dashboard.quickStats.actionPlanCount} />
              <Stat label="회고" value={dashboard.quickStats.monthlyReflectionCount} />
              <Stat label="후기" value={dashboard.quickStats.meetingReviewCount} />
              <Stat label="소소모임" value={dashboard.quickStats.smallMeetingCreatedCount} />
            </div>
          </div>
        )}
      </Section>
    </main>
  );
}

function Stat({ label, value }: { label: string; value: number }) {
  return (
    <Card>
      <p className="text-sm text-[var(--color-charcoal)]">{label}</p>
      <p className="mt-3 text-2xl font-normal">{value}</p>
    </Card>
  );
}
