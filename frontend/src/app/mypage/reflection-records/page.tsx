"use client";

import { useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { MobileBackButton } from "@/components/MobileBackButton";
import { Button, EmptyState, PageHeader, RecordButton, Section, SkeletonBlock, Tag } from "@/components/ui/primitives";
import { apiGet, type MonthlyReflectionSlot } from "@/lib/api";

export default function MyReflectionRecordsPage() {
  return (
    <AuthGate required="MEMBER">
      {() => <MyReflectionRecordsContent />}
    </AuthGate>
  );
}

function MyReflectionRecordsContent() {
  const currentMonth = formatMonthKst(new Date().toISOString());
  const [selectedMonth, setSelectedMonth] = useState(currentMonth);
  const [slot, setSlot] = useState<MonthlyReflectionSlot | null>(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const canMoveNext = selectedMonth < currentMonth;

  useEffect(() => {
    setLoading(true);
    setMessage(null);
    apiGet<MonthlyReflectionSlot>(`/me/reflections/${selectedMonth}`)
      .then((result) => {
        if (!result.success) {
          setMessage(result.error?.message ?? "회고 기록을 불러오지 못했습니다.");
          setSlot(null);
          return;
        }
        setSlot(result.data);
      })
      .catch(() => {
        setMessage("회고 기록을 불러오지 못했습니다.");
        setSlot(null);
      })
      .finally(() => setLoading(false));
  }, [selectedMonth]);

  function moveMonth(delta: -1 | 1) {
    setSelectedMonth((month) => {
      const nextMonth = addMonths(month, delta);
      return nextMonth > currentMonth ? currentMonth : nextMonth;
    });
  }

  const reflection = slot?.reflection ?? null;

  return (
    <main>
      <Section>
        <div className="grid gap-5 sm:gap-8">
          <MobileBackButton fallbackHref="/mypage" />
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <PageHeader eyebrow="My Reflection" title="내 회고기록" description="월별로 남긴 회고를 다시 확인합니다." />
            <Button href={`/mypage/reflections?month=${currentMonth}`}>이번 달 회고</Button>
          </div>

          {message && <p className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.78)] px-4 py-3 text-sm leading-6 text-[var(--color-charcoal)]">{message}</p>}
          <MonthNavigator count={reflection ? 1 : 0} label="월별 회고" month={selectedMonth} onMove={moveMonth} canMoveNext={canMoveNext} />

          {loading ? (
            <RecordSkeleton />
          ) : reflection ? (
            <article className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4 shadow-[var(--shadow-soft)] sm:p-5">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div className="flex flex-wrap items-center gap-2">
                  <Tag>{formatMonthLabel(selectedMonth)}</Tag>
                  <Tag>월간회고</Tag>
                </div>
                <RecordButton href={`/mypage/reflections?month=${selectedMonth}`}>수정</RecordButton>
              </div>
              <ReflectionBlock label="잘한 것" value={reflection.wellDone} />
              <ReflectionBlock label="아쉬운 점" value={reflection.regret} />
              <ReflectionBlock label="다음 달 집중" value={reflection.nextFocus} />
            </article>
          ) : (
            <EmptyState title={`${formatMonthLabel(selectedMonth)} 회고가 없습니다`} description="다른 달을 살펴보거나 선택한 달의 회고를 남겨보세요." />
          )}
        </div>
      </Section>
    </main>
  );
}

function ReflectionBlock({ label, value }: { label: string; value: string | null }) {
  return (
    <div className="mt-5 border-t border-[var(--color-line)] pt-4">
      <p className="text-xs text-[var(--color-bronze)]">{label}</p>
      <p className="mt-2 whitespace-pre-wrap break-words text-sm leading-7 text-[var(--color-charcoal)]">{value || "-"}</p>
    </div>
  );
}

function MonthNavigator({ canMoveNext, count, label, month, onMove }: { canMoveNext: boolean; count: number; label: string; month: string; onMove: (delta: -1 | 1) => void }) {
  return (
    <div className="rounded-[var(--radius-card)] border border-[rgba(229,222,209,0.82)] bg-[rgba(255,254,250,0.72)] px-3 py-2.5 shadow-[var(--shadow-soft)] sm:px-4">
      <div className="grid grid-cols-[2rem_1fr_2rem] items-center gap-2">
        <button aria-label="이전달 보기" className="grid size-8 place-items-center rounded-full border border-[rgba(229,222,209,0.9)] bg-[rgba(255,253,248,0.7)] text-[var(--color-charcoal)] transition hover:border-[rgba(31,77,58,0.3)] hover:bg-[rgba(47,90,67,0.055)] hover:text-[var(--color-deep-green)]" onClick={() => onMove(-1)} type="button">
          <span aria-hidden="true" className="block size-2.5 rotate-45 border-b border-l border-current" />
        </button>
        <div className="min-w-0 text-center">
          <p className="text-[10px] leading-4 text-[var(--color-bronze)] sm:text-[11px]">{label}</p>
          <p className="text-[13px] leading-5 text-[var(--color-charcoal)] sm:text-sm">{formatMonthLabel(month)} · {count}개</p>
        </div>
        <button aria-label="다음달 보기" className="grid size-8 place-items-center rounded-full border border-[rgba(229,222,209,0.9)] bg-[rgba(255,253,248,0.7)] text-[var(--color-charcoal)] transition hover:border-[rgba(31,77,58,0.3)] hover:bg-[rgba(47,90,67,0.055)] hover:text-[var(--color-deep-green)] disabled:cursor-not-allowed disabled:opacity-30" disabled={!canMoveNext} onClick={() => onMove(1)} type="button">
          <span aria-hidden="true" className="block size-2.5 -rotate-45 border-b border-r border-current" />
        </button>
      </div>
    </div>
  );
}

function RecordSkeleton() {
  return (
    <article className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4 shadow-[var(--shadow-soft)] sm:p-5">
      <SkeletonBlock className="h-7 w-28 rounded-full" />
      <div className="mt-5 grid gap-3">
        <SkeletonBlock className="h-4 w-full" />
        <SkeletonBlock className="h-4 w-11/12" />
        <SkeletonBlock className="h-4 w-4/5" />
      </div>
    </article>
  );
}

function formatMonthKst(value: string) {
  return new Intl.DateTimeFormat("en-CA", { year: "numeric", month: "2-digit", timeZone: "Asia/Seoul" }).format(new Date(value));
}

function formatMonthLabel(value: string) {
  const [year, month] = value.split("-");
  return `${year}년 ${Number(month)}월`;
}

function addMonths(value: string, delta: number) {
  const [year, month] = value.split("-").map(Number);
  return new Intl.DateTimeFormat("en-CA", { year: "numeric", month: "2-digit", timeZone: "UTC" }).format(new Date(Date.UTC(year, month - 1 + delta, 1)));
}
