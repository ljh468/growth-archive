"use client";

import { useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { MobileBackButton } from "@/components/MobileBackButton";
import { Button, EmptyState, PageHeader, RecordButton, Section, SkeletonBlock, Tag } from "@/components/ui/primitives";
import { apiGet, type MonthlyActionPlan } from "@/lib/api";

export default function MyActionRecordsPage() {
  return (
    <AuthGate required="MEMBER">
      {() => <MyActionRecordsContent />}
    </AuthGate>
  );
}

function MyActionRecordsContent() {
  const currentMonth = formatMonthKst(new Date().toISOString());
  const [selectedMonth, setSelectedMonth] = useState(currentMonth);
  const [plan, setPlan] = useState<MonthlyActionPlan | null>(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const canMoveNext = selectedMonth < currentMonth;

  useEffect(() => {
    setLoading(true);
    setMessage(null);
    apiGet<MonthlyActionPlan | null>(`/me/action-plans/${selectedMonth}`)
      .then((result) => {
        if (!result.success) {
          setMessage(result.error?.message ?? "실행기록을 불러오지 못했습니다.");
          setPlan(null);
          return;
        }
        setPlan(result.data);
      })
      .catch(() => {
        setMessage("실행기록을 불러오지 못했습니다.");
        setPlan(null);
      })
      .finally(() => setLoading(false));
  }, [selectedMonth]);

  function moveMonth(delta: -1 | 1) {
    setSelectedMonth((month) => {
      const nextMonth = addMonths(month, delta);
      return nextMonth > currentMonth ? currentMonth : nextMonth;
    });
  }

  return (
    <main>
      <Section>
        <div className="grid gap-5 sm:gap-8">
          <MobileBackButton fallbackHref="/mypage" />
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <PageHeader eyebrow="My Action" title="내 실행기록" description="월별로 남긴 실행계획을 차분히 다시 확인합니다." />
            <Button href={`/mypage/action-plans?month=${currentMonth}`}>이번 달 작성</Button>
          </div>

          {message && (
            <p className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.78)] px-4 py-3 text-sm leading-6 text-[var(--color-charcoal)]">
              {message}
            </p>
          )}

          <div className="rounded-[var(--radius-card)] border border-[rgba(229,222,209,0.82)] bg-[rgba(255,254,250,0.72)] px-3 py-2.5 shadow-[var(--shadow-soft)] sm:px-4">
            <div className="grid grid-cols-[2rem_1fr_2rem] items-center gap-2">
              <button
                aria-label="이전달 보기"
                className="grid size-8 place-items-center rounded-full border border-[rgba(229,222,209,0.9)] bg-[rgba(255,253,248,0.7)] text-[var(--color-charcoal)] transition hover:border-[rgba(31,77,58,0.3)] hover:bg-[rgba(47,90,67,0.055)] hover:text-[var(--color-deep-green)]"
                onClick={() => moveMonth(-1)}
                type="button"
              >
                <span aria-hidden="true" className="block size-2.5 rotate-45 border-b border-l border-current" />
              </button>
              <div className="min-w-0 text-center">
                <p className="text-[10px] leading-4 text-[var(--color-bronze)] sm:text-[11px]">월별 실행기록</p>
                <p className="text-[13px] leading-5 text-[var(--color-charcoal)] sm:text-sm">
                  {formatMonthLabel(selectedMonth)} · {plan ? "1개" : "0개"}
                </p>
              </div>
              <button
                aria-label="다음달 보기"
                className="grid size-8 place-items-center rounded-full border border-[rgba(229,222,209,0.9)] bg-[rgba(255,253,248,0.7)] text-[var(--color-charcoal)] transition hover:border-[rgba(31,77,58,0.3)] hover:bg-[rgba(47,90,67,0.055)] hover:text-[var(--color-deep-green)] disabled:cursor-not-allowed disabled:opacity-30"
                disabled={!canMoveNext}
                onClick={() => moveMonth(1)}
                type="button"
              >
                <span aria-hidden="true" className="block size-2.5 -rotate-45 border-b border-r border-current" />
              </button>
            </div>
          </div>

          {loading ? (
            <ActionRecordSkeleton />
          ) : plan ? (
            <article className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4 shadow-[var(--shadow-soft)] sm:p-5">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div className="flex flex-wrap items-center gap-2">
                  <Tag>{formatMonthLabel(selectedMonth)}</Tag>
                  <Tag>실행계획</Tag>
                </div>
                <RecordButton href={`/mypage/action-plans?month=${selectedMonth}`}>수정/삭제</RecordButton>
              </div>
              <h2 className="mt-4 font-display text-lg font-normal leading-snug text-[var(--color-ink)] sm:text-2xl">
                {plan.title || "이번 달 실행계획"}
              </h2>
              <p className="mt-4 whitespace-pre-wrap break-words text-sm leading-7 text-[var(--color-charcoal)]">
                {plan.content}
              </p>
              <p className="mt-5 text-xs text-[var(--color-muted)]">
                마지막 수정 {formatDate(plan.updatedAt)}
              </p>
            </article>
          ) : (
            <EmptyState
              title={`${formatMonthLabel(selectedMonth)} 실행기록이 없습니다`}
              description="다른 달을 살펴보거나 선택한 달의 실행계획을 새로 남겨보세요."
            />
          )}
        </div>
      </Section>
    </main>
  );
}

function ActionRecordSkeleton() {
  return (
    <article className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4 shadow-[var(--shadow-soft)] sm:p-5" aria-label="실행기록 로딩 중">
      <div className="flex gap-2">
        <SkeletonBlock className="h-7 w-20 rounded-full" />
        <SkeletonBlock className="h-7 w-24 rounded-full" />
      </div>
      <SkeletonBlock className="mt-5 h-7 w-3/4" />
      <div className="mt-5 grid gap-3">
        <SkeletonBlock className="h-4 w-full" />
        <SkeletonBlock className="h-4 w-11/12" />
        <SkeletonBlock className="h-4 w-4/5" />
      </div>
    </article>
  );
}

function formatMonthKst(value: string) {
  return new Intl.DateTimeFormat("en-CA", {
    year: "numeric",
    month: "2-digit",
    timeZone: "Asia/Seoul",
  }).format(new Date(value));
}

function formatMonthLabel(value: string) {
  const [year, month] = value.split("-");
  return `${year}년 ${Number(month)}월`;
}

function addMonths(value: string, delta: number) {
  const [year, month] = value.split("-").map(Number);
  const date = new Date(Date.UTC(year, month - 1 + delta, 1));
  return new Intl.DateTimeFormat("en-CA", {
    year: "numeric",
    month: "2-digit",
    timeZone: "UTC",
  }).format(date);
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeZone: "Asia/Seoul",
  }).format(new Date(value));
}
