"use client";

import Image from "next/image";
import { useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, EmptyState, Section, Tag } from "@/components/ui/primitives";
import { apiGet, apiPost, type ActivitySummary, type MyDashboard } from "@/lib/api";

export default function MyPage() {
  return (
    <AuthGate required="MEMBER">
      {() => <MyPageContent />}
    </AuthGate>
  );
}

function MyPageContent() {
  const currentMonth = new Date().toISOString().slice(0, 7);
  const [dashboard, setDashboard] = useState<MyDashboard | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [withdrawConfirmOpen, setWithdrawConfirmOpen] = useState(false);
  const [withdrawPhrase, setWithdrawPhrase] = useState("");
  const [withdrawMessage, setWithdrawMessage] = useState<string | null>(null);
  const monthlyReadingRecords = dashboard?.currentReadingRecords ?? (dashboard?.currentReadingRecord ? [dashboard.currentReadingRecord] : []);
  const hasMonthlyRecord = Boolean(monthlyReadingRecords.length > 0 || dashboard?.currentActionPlan);

  useEffect(() => {
    apiGet<MyDashboard>(`/me/dashboard?month=${currentMonth}-01`)
      .then((result) => {
        if (!result.success) {
          setError(result.error?.message ?? "마이페이지를 불러오지 못했습니다.");
          return;
        }
        setDashboard(result.data);
      })
      .catch(() => setError("마이페이지를 불러오지 못했습니다."));
  }, [currentMonth]);

  async function withdraw() {
    setWithdrawMessage(null);
    const result = await apiPost<void>("/me/withdraw");
    if (!result.success) {
      setWithdrawMessage(result.error?.message ?? "탈퇴 처리를 완료하지 못했습니다.");
      return;
    }
    window.location.href = "/login";
  }

  return (
    <main>
      <Section>
        {error && <EmptyState title="불러오기 실패" description={error} />}
        {!dashboard && !error && <EmptyState title="불러오는 중입니다." description="내 성장 기록을 확인하고 있습니다." />}
        {dashboard && (
          <div className="grid gap-4 sm:gap-6">
            <section className="overflow-hidden rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[linear-gradient(135deg,rgba(255,254,250,0.96),rgba(47,90,67,0.07))] p-4 shadow-[var(--shadow-soft)] sm:p-7">
              <div className="grid gap-4 sm:grid-cols-[auto_1fr] sm:items-center sm:gap-5">
                <ProfileImage name={dashboard.profile.displayName} src={dashboard.profile.profileImageUrl} />
                <div className="min-w-0">
                  <p className="font-latin text-2xl leading-none text-[var(--color-bronze)] sm:text-4xl">My Archive</p>
                  <div className="mt-2 flex flex-wrap items-center gap-2 sm:mt-3">
                    <h1 className="font-display text-xl font-normal leading-snug sm:text-4xl">{dashboard.profile.displayName}님의 성장 기록</h1>
                    {dashboard.profile.role === "ADMIN" && (
                      <span className="inline-flex rounded-full border border-[rgba(31,77,58,0.22)] bg-[rgba(31,77,58,0.1)] px-2.5 py-1 text-[10px] font-medium leading-none text-[var(--color-deep-green)] sm:text-[11px]">
                        운영진
                      </span>
                    )}
                  </div>
                  <p className="mt-2 max-w-2xl text-sm leading-6 text-[var(--color-muted)] sm:mt-3 sm:leading-7">{dashboard.profile.oneLineIntro}</p>
                  {dashboard.profile.role === "ADMIN" && (
                    <div className="mt-4">
                      <Button href="/admin">운영 관리</Button>
                    </div>
                  )}
                </div>
              </div>
            </section>

            <section className="grid gap-4 lg:grid-cols-[1.1fr_0.9fr]">
              <div className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4 shadow-[var(--shadow-soft)] sm:p-5">
                <div className="flex flex-wrap items-center gap-2">
                  <Tag>{dashboard.participation.month}</Tag>
                  {hasMonthlyRecord ? <Tag>기록 남김</Tag> : <Tag>기록 대기 중</Tag>}
                </div>
                <h2 className="mt-3 font-display text-lg font-normal leading-snug sm:mt-4 sm:text-2xl">
                  {hasMonthlyRecord ? "이번 달의 흔적이 남았습니다" : "작은 기록을 기다리고 있어요"}
                </h2>
                <p className="mt-3 text-sm leading-7 text-[var(--color-charcoal)]">
                  {hasMonthlyRecord ? "독서기록과 실행계획을 이달의 기록장에 보관합니다." : "독서기록이나 실행계획 중 하나만 남겨도 이번 달 기록이 시작됩니다."}
                </p>
                <div className="mt-4 grid gap-3 sm:mt-5">
                  {monthlyReadingRecords.length > 0 ? (
                    <section className="grid gap-2 rounded-[var(--radius-card)] border border-[rgba(229,222,209,0.82)] bg-[rgba(255,254,250,0.56)] p-2.5">
                      <div className="flex items-center justify-between gap-3">
                        <p className="text-[11px] text-[var(--color-bronze)]">독서기록 · 이번 달 {monthlyReadingRecords.length}개</p>
                        <a className="archive-record-action" href="/mypage/reading-records">
                          독서기록 수정/삭제
                        </a>
                      </div>
                      {monthlyReadingRecords.map((record) => (
                        <MonthlyRecordCard
                          eyebrow="독서기록"
                          href={`/books/${record.bookId}`}
                          key={record.id}
                          title={record.bookTitle}
                        >
                          {record.oneLineReview}
                        </MonthlyRecordCard>
                      ))}
                    </section>
                  ) : (
                    <MonthlyEmptyCard href="/reading-records/new" label="독서기록" message="이번 달 독서기록은 아직 비어 있습니다." />
                  )}
                  {dashboard.currentActionPlan ? (
                    <section className="grid gap-2 rounded-[var(--radius-card)] border border-[rgba(229,222,209,0.82)] bg-[rgba(255,254,250,0.56)] p-2.5">
                      <div className="flex items-center justify-between gap-3">
                        <p className="text-[11px] text-[var(--color-bronze)]">실행계획 · 이번 달</p>
                        <a className="archive-record-action" href="/mypage/action-plans">
                          실행계획 수정/삭제
                        </a>
                      </div>
                      <MonthlyRecordCard
                        eyebrow="실행계획"
                        href="/mypage/action-plans"
                        title={dashboard.currentActionPlan.title || "이번 달 실행계획"}
                      >
                        {dashboard.currentActionPlan.content}
                      </MonthlyRecordCard>
                    </section>
                  ) : (
                    <MonthlyEmptyCard href="/mypage/action-plans" label="실행계획" message="이번 달 실행계획은 아직 비어 있습니다." />
                  )}
                </div>
                {!hasMonthlyRecord && dashboard.participation.coffeeSupportTarget && (
                  <p className="mt-4 rounded-[var(--radius-card)] bg-[rgba(138,106,69,0.08)] px-4 py-3 text-sm leading-6 text-[var(--color-charcoal)]">
                    이번 달은 아직 기록이 비어 있어요. 작은 한 문장부터 남겨보세요.
                  </p>
                )}
              </div>

              <div className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4 shadow-[var(--shadow-soft)] sm:p-5">
                <p className="text-sm text-[var(--color-muted)]">오늘 이어가기</p>
                <h2 className="mt-1 font-display text-xl font-normal sm:mt-2 sm:text-2xl">무엇을 남길까요?</h2>
                <div className="mt-4 grid gap-2 sm:mt-5">
                  <ActionRow href={monthlyReadingRecords.length > 0 ? "/mypage/reading-records" : "/reading-records/new"} label="독서기록" status={monthlyReadingRecords.length > 0 ? "남김" : "작성"} />
                  <ActionRow href="/mypage/action-plans" label="실행계획" status={dashboard.currentActionPlan ? "남김 · 수정" : "작성"} />
                  <ActionRow href="/mypage/reflections" label="월간회고" status="남기기" />
                  <ActionRow href="/meetings/new" label="소소모임" status="만들기" />
                </div>
              </div>
            </section>

            <section className="grid gap-4 lg:grid-cols-[0.9fr_1.1fr]">
              <div className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4 shadow-[var(--shadow-soft)] sm:p-5">
                <div>
                  <p className="text-xs text-[var(--color-muted)] sm:text-sm">누적 기록</p>
                  <h2 className="mt-1 font-display text-lg font-normal sm:text-xl">쌓인 흔적</h2>
                </div>
                <div className="mt-3 grid gap-1.5 sm:mt-4">
                  <Stat label="독서" value={dashboard.quickStats.readingRecordCount} />
                  <Stat label="액션플랜" value={dashboard.quickStats.actionPlanCount} />
                  <Stat label="회고" value={dashboard.quickStats.monthlyReflectionCount} />
                  <Stat label="후기" value={dashboard.quickStats.meetingReviewCount} />
                  <Stat label="만든 소소모임" value={dashboard.quickStats.smallMeetingCreatedCount} />
                </div>
              </div>

              <div className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4 shadow-[var(--shadow-soft)] sm:p-5">
                <div className="flex items-end justify-between gap-4">
                  <div>
                    <p className="text-sm text-[var(--color-muted)]">최근 활동</p>
                    <h2 className="mt-1 font-display text-xl font-normal sm:mt-2 sm:text-2xl">최근 남긴 기록</h2>
                  </div>
                  <Button href="/people" variant="secondary">사람들 보기</Button>
                </div>
                {dashboard.recentActivities.length === 0 ? (
                  <p className="mt-5 text-sm leading-7 text-[var(--color-muted)]">아직 최근 활동이 없습니다. 첫 기록을 남기면 이곳에 쌓입니다.</p>
                ) : (
                  <div className="mt-5 grid gap-3">
                    {dashboard.recentActivities.slice(0, 5).map((activity) => (
                      <ActivityItem activity={activity} key={`${activity.type}-${activity.href}-${activity.occurredAt}`} />
                    ))}
                  </div>
                )}
              </div>
            </section>

            <section className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4 shadow-[var(--shadow-soft)] sm:p-5">
              <div className="grid gap-3 sm:grid-cols-[1fr_auto] sm:items-center">
                <div>
                  <p className="text-sm text-[var(--color-muted)]">계정 관리</p>
                  <p className="mt-1 text-sm leading-6 text-[var(--color-charcoal)]">
                    탈퇴하면 계정은 비활성화되고 실명, 생년월일, 프로필 이미지는 정리됩니다. 남긴 기록은 아카이브에 보관됩니다.
                  </p>
                  {withdrawMessage && <p className="mt-2 text-sm text-[var(--color-wood-brown)]">{withdrawMessage}</p>}
                </div>
                {withdrawConfirmOpen ? (
                  <div className="grid gap-2 sm:min-w-64">
                    <label className="grid gap-1 text-xs text-[var(--color-muted)]">
                      <span>계속하려면 탈퇴하기를 입력하세요.</span>
                      <input
                        className="min-h-10 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 text-sm text-[var(--color-charcoal)] outline-none transition focus:border-[var(--color-wood-brown)]"
                        onChange={(event) => setWithdrawPhrase(event.target.value)}
                        value={withdrawPhrase}
                      />
                    </label>
                    <div className="flex flex-wrap gap-2">
                      <button
                        className="inline-flex min-h-10 items-center justify-center rounded-[var(--radius-card)] bg-[#B42318] px-4 text-sm text-[var(--color-warm-white)] transition hover:bg-[#8F1D14] disabled:cursor-not-allowed disabled:bg-[rgba(180,35,24,0.28)] disabled:text-[rgba(255,253,248,0.72)]"
                        disabled={withdrawPhrase !== "탈퇴하기"}
                        onClick={withdraw}
                        type="button"
                      >
                        탈퇴하기
                      </button>
                      <button
                        className="inline-flex min-h-10 items-center justify-center rounded-[var(--radius-card)] border border-[var(--color-line)] px-4 text-sm text-[var(--color-charcoal)] transition hover:border-[var(--color-wood-brown)] hover:text-[var(--color-wood-brown)]"
                        onClick={() => {
                          setWithdrawConfirmOpen(false);
                          setWithdrawPhrase("");
                        }}
                        type="button"
                      >
                        취소
                      </button>
                    </div>
                  </div>
                ) : (
                  <button
                    className="inline-flex min-h-10 items-center justify-center rounded-[var(--radius-card)] bg-[#B42318] px-4 text-sm text-[var(--color-warm-white)] transition hover:bg-[#8F1D14]"
                    onClick={() => setWithdrawConfirmOpen(true)}
                    type="button"
                  >
                    탈퇴하기
                  </button>
                )}
              </div>
            </section>
          </div>
        )}
      </Section>
    </main>
  );
}

function Stat({ label, value }: { label: string; value: number }) {
  return (
    <div className="flex items-center justify-between rounded-[var(--radius-card)] border border-[rgba(229,222,209,0.72)] bg-[rgba(255,254,250,0.58)] px-3 py-2">
      <p className="text-xs leading-none text-[var(--color-charcoal)] sm:text-sm">{label}</p>
      <p className="font-display text-base font-normal leading-none text-[var(--color-deep-green)] sm:text-lg">{value}</p>
    </div>
  );
}

function MonthlyRecordCard({ children, eyebrow, href, title }: { children: string; eyebrow: string; href: string; title: string }) {
  return (
    <a className="block rounded-[var(--radius-card)] border border-[rgba(229,222,209,0.78)] bg-[rgba(47,90,67,0.045)] p-3 transition hover:border-[var(--color-deep-green)]" href={href}>
      <p className="text-[11px] text-[var(--color-bronze)]">{eyebrow}</p>
      <p className="mt-1 text-sm font-normal leading-5 text-[var(--color-ink)]">{title}</p>
      <p className="mt-2 line-clamp-3 whitespace-pre-wrap break-words text-xs leading-6 text-[var(--color-charcoal)]">{children}</p>
    </a>
  );
}

function MonthlyEmptyCard({ href, label, message }: { href: string; label: string; message: string }) {
  return (
    <a className="block rounded-[var(--radius-card)] border border-dashed border-[var(--color-line)] bg-[rgba(255,254,250,0.58)] p-3 transition hover:border-[var(--color-deep-green)]" href={href}>
      <p className="text-[11px] text-[var(--color-bronze)]">{label}</p>
      <p className="mt-1 text-xs leading-5 text-[var(--color-muted)]">{message}</p>
    </a>
  );
}

function ActionRow({ href, label, status }: { href: string; label: string; status: string }) {
  const completed = status.includes("남김");
  return (
    <a className="flex min-h-10 items-center justify-between rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.72)] px-3 text-sm transition hover:border-[var(--color-deep-green)] hover:bg-[rgba(47,90,67,0.045)]" href={href}>
      <span className="text-[var(--color-charcoal)]">{label}</span>
      <span className={completed ? "text-xs text-[var(--color-deep-green)]" : "text-xs text-[var(--color-muted)]"}>{status}</span>
    </a>
  );
}

function ProfileImage({ name, src }: { name: string; src: string | null }) {
  if (src) {
    return <Image alt="" className="size-16 rounded-full object-cover shadow-[var(--shadow-soft)] sm:size-24" height={96} src={src} unoptimized width={96} />;
  }

  return (
    <div className="flex size-16 items-center justify-center rounded-full bg-[var(--color-deep-green)] text-xl text-[var(--color-warm-white)] shadow-[var(--shadow-soft)] sm:size-24 sm:text-2xl">
      {name.slice(0, 1)}
    </div>
  );
}

function ActivityItem({ activity }: { activity: ActivitySummary }) {
  return (
    <a className="grid gap-1 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.72)] p-4 transition hover:border-[var(--color-deep-green)]" href={activity.href}>
      <p className="text-xs text-[var(--color-bronze)]">{activityLabel(activity.type)} · {formatKoreanDate(activity.occurredAt)}</p>
      <p className="line-clamp-2 text-sm leading-6 text-[var(--color-charcoal)]">{activity.title}</p>
    </a>
  );
}

function activityLabel(type: string) {
  return {
    READING_RECORD: "독서기록",
    ACTION_PLAN: "실행계획",
    MONTHLY_REFLECTION: "월간회고",
    MEETING_REVIEW: "모임후기",
    SMALL_MEETING: "소소모임",
  }[type] ?? "기록";
}

function formatKoreanDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeZone: "Asia/Seoul",
  }).format(new Date(value));
}
