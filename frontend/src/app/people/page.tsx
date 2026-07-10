"use client";

import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import { EmptyState, PageHeader, SkeletonBlock, Tag } from "@/components/ui/primitives";
import { apiGet, apiGetCurrentUser, type MonthlyActionPlanShowcase, type ProfileCard } from "@/lib/api";

export default function PeoplePage() {
  const currentMonth = new Date().toISOString().slice(0, 7);
  const [people, setPeople] = useState<ProfileCard[]>([]);
  const [monthlyActionPlans, setMonthlyActionPlans] = useState<MonthlyActionPlanShowcase[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [pageIndex, setPageIndex] = useState(0);
  const [memberReady, setMemberReady] = useState(false);
  const [isCompact, setIsCompact] = useState(false);
  const dragStartX = useRef<number | null>(null);
  const pageSize = isCompact ? 2 : 4;
  const pageCount = Math.max(1, Math.ceil(people.length / pageSize));
  const currentPageIndex = Math.min(pageIndex, pageCount - 1);
  const visiblePeople = people.slice(currentPageIndex * pageSize, currentPageIndex * pageSize + pageSize);
  const visibleSlots = visiblePeople.length
    ? [...visiblePeople, ...Array<null>(pageSize - visiblePeople.length).fill(null)]
    : [];
  const showProfileImages = memberReady && people.some((person) => person.profileImageUrl);

  useEffect(() => {
    Promise.all([apiGet<ProfileCard[]>("/people"), apiGetCurrentUser()])
      .then(([peopleResult, userResult]) => {
        if (!peopleResult.success) {
          setError(peopleResult.error?.message ?? "성장 프로필을 불러오지 못했습니다.");
          return;
        }
        setPeople(shufflePeople(peopleResult.data));
        const canSeeMemberOnly = userResult.success && userResult.data.onboardingCompleted && !userResult.data.deactivated;
        setMemberReady(canSeeMemberOnly);
        if (canSeeMemberOnly) {
          apiGet<MonthlyActionPlanShowcase[]>(`/people/monthly-action-plans?month=${currentMonth}`)
            .then((result) => {
              if (result.success) {
                setMonthlyActionPlans(shuffleActionPlans(result.data));
              }
            })
            .catch(() => {
              setMonthlyActionPlans([]);
            });
        }
        setPageIndex(0);
      })
      .catch(() => setError("성장 프로필을 불러오지 못했습니다."))
      .finally(() => setLoading(false));
  }, [currentMonth]);

  useEffect(() => {
    function updateCompact() {
      setIsCompact(window.innerWidth < 640);
    }
    updateCompact();
    window.addEventListener("resize", updateCompact);
    return () => window.removeEventListener("resize", updateCompact);
  }, []);

  function movePeople(direction: -1 | 1) {
    if (pageCount <= 1) {
      return;
    }
    setPageIndex((index) => (index + direction + pageCount) % pageCount);
  }

  function finishPeopleDrag(clientX: number) {
    if (dragStartX.current === null || pageCount <= 1) {
      dragStartX.current = null;
      return;
    }
    const delta = clientX - dragStartX.current;
    dragStartX.current = null;
    if (Math.abs(delta) < 40) {
      return;
    }
    movePeople(delta < 0 ? 1 : -1);
  }

  return (
    <main>
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 bg-cover bg-center photo-muted" style={{ backgroundImage: "url(/images/korean-reading-table.jpg)" }} />
        <div className="absolute inset-0 bg-[rgba(248,246,238,0.88)]" />
        <div className="relative mx-auto flex min-h-[260px] max-w-6xl items-end px-5 py-10 sm:min-h-[340px] sm:px-8 sm:py-12 lg:px-10">
          <PageHeader
            eyebrow="Growth People"
            title="성장을 기록하는 사람들"
            description="읽고, 실행하고, 성장한 기록을 남기는 사람들의 방향을 보여주는 프로필 쇼케이스입니다."
          />
        </div>
      </section>
      <section className="mx-auto w-full max-w-6xl px-5 py-7 sm:px-8 sm:py-9 lg:px-10 lg:py-10">
        <div className="grid gap-5 sm:gap-8">
          {error && <EmptyState title="불러오기 실패" description={error} />}
          {loading && <PeopleSkeleton />}
          {!loading && !error && people.length === 0 && <EmptyState title="아직 보여줄 성장 프로필이 없어요." description="첫 프로필이 완성되면 이곳에 표시됩니다." />}
          {!loading && people.length > 0 && <div className="select-none">
            <div className={people.length > pageSize ? "grid items-stretch gap-2 sm:grid-cols-[2.25rem_1fr_2.25rem]" : ""}>
              {people.length > pageSize && (
                <SideSlideButton label="이전 성장 프로필 보기" onClick={() => movePeople(-1)} side="left" />
              )}
              <div
                className="min-w-0"
                onMouseDown={(event) => {
                  dragStartX.current = event.clientX;
                }}
                onMouseLeave={() => {
                  dragStartX.current = null;
                }}
                onMouseUp={(event) => finishPeopleDrag(event.clientX)}
                onTouchEnd={(event) => {
                  const touch = event.changedTouches[0];
                  if (touch) {
                    finishPeopleDrag(touch.clientX);
                  }
                }}
                onTouchStart={(event) => {
                  dragStartX.current = event.touches[0]?.clientX ?? null;
                }}
              >
                <div className="grid gap-2.5 sm:grid-cols-2 sm:gap-3 lg:grid-cols-4">
              {visibleSlots.map((person, index) => person ? (
                <Link
                  className={[
                    "group relative overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-warm-white)] shadow-[var(--shadow-soft)] transition hover:-translate-y-0.5",
                    showProfileImages ? "grid grid-cols-[82px_1fr] sm:block" : "grid",
                  ].join(" ")}
                  href={`/people/${person.memberId}`}
                  key={person.memberId}
                >
                  {showProfileImages && (
                    <div className="flex min-h-[112px] w-[82px] items-center justify-center border-r border-[var(--color-line)] bg-[linear-gradient(180deg,rgba(248,246,238,0.9),rgba(255,253,248,0.72))] sm:h-24 sm:w-full sm:border-b sm:border-r-0">
                      {person.profileImageUrl ? (
                        <div
                          aria-label={`${person.displayName} 프로필 이미지`}
                          className="size-16 rounded-full bg-cover bg-center shadow-[0_12px_24px_rgba(63,47,34,0.14)] ring-2 ring-[var(--color-warm-white)]"
                          role="img"
                          style={{ backgroundImage: `url(${person.profileImageUrl})` }}
                        />
                      ) : (
                        <div className="grid size-16 place-items-center rounded-full bg-[rgba(47,90,67,0.1)] text-sm text-[var(--color-deep-green)] ring-2 ring-[var(--color-warm-white)]">
                          {person.displayName.slice(0, 1)}
                        </div>
                      )}
                    </div>
                  )}
                  {!showProfileImages && (
                    <div
                      aria-hidden="true"
                      className="absolute right-2.5 top-2.5 grid size-6 place-items-center rounded-full bg-[var(--color-deep-green)] text-[var(--color-warm-white)] ring-2 ring-[var(--color-warm-white)]"
                    >
                      <svg aria-hidden="true" className="size-3.5" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" viewBox="0 0 24 24">
                        <path d="M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" />
                        <path d="M5 21a7 7 0 0 1 14 0" />
                      </svg>
                    </div>
                  )}
                  <div className="flex min-w-0 flex-col p-2.5 sm:p-3">
                    <div className="flex items-start justify-between gap-2 sm:min-h-8">
                      <div className="min-w-0">
                        <h2 className="line-clamp-1 pr-1 text-sm font-normal sm:min-h-5 sm:text-base">{person.displayName}</h2>
                      </div>
                      {person.role === "ADMIN" && (
                        <span className={["shrink-0 rounded-full border border-[rgba(47,90,67,0.22)] bg-[rgba(255,254,250,0.86)] px-2 py-0.5 text-[10px] text-[var(--color-deep-green)]", !showProfileImages ? "mr-8" : ""].join(" ")}>운영진</span>
                      )}
                    </div>
                    <p className="mt-1 line-clamp-1 text-[11px] leading-4 text-[var(--color-muted)]">{person.oneLineIntro}</p>
                    <p className="mt-1 line-clamp-1 text-[11px] leading-4 text-[var(--color-charcoal)] sm:mt-2 sm:line-clamp-2 sm:min-h-10 sm:text-xs sm:leading-5" style={{ wordBreak: "keep-all" }}>
                      {person.futureMeAt50Summary}
                    </p>
                    <div className="mt-1.5 flex flex-wrap gap-1 sm:mt-2 sm:gap-1.5">
                      {person.interestTags.slice(0, 2).map((tag) => (
                        <Tag key={tag}>{tag}</Tag>
                      ))}
                    </div>
                    {person.recentPublicActivity && (
                      <p className="mt-1.5 border-t border-[var(--color-line)] pt-1.5 text-[10px] text-[var(--color-muted)] sm:mt-2 sm:pt-2 sm:text-[11px]">최근 기록: {person.recentPublicActivity.title}</p>
                    )}
                  </div>
                </Link>
              ) : (
                <div aria-hidden="true" className="hidden h-full rounded-[var(--radius-card)] lg:block lg:invisible" key={`empty-slot-${index}`} />
              ))}
                </div>
              </div>
              {people.length > pageSize && (
                <SideSlideButton label="다음 성장 프로필 보기" onClick={() => movePeople(1)} side="right" />
              )}
            </div>
          </div>}
          {!loading && people.length > pageSize && (
            <div className="flex items-center justify-center gap-3 pt-1 sm:gap-2">
              <MobileSlideButton label="이전 성장 프로필 보기" onClick={() => movePeople(-1)} side="left" />
              <div className="flex items-center gap-2" aria-label="성장하는 사람들 슬라이드">
                {Array.from({ length: pageCount }).map((_, index) => (
                  <button
                    aria-current={index === pageIndex}
                    aria-label={`${index + 1}번째 성장 프로필 보기`}
                    className={[
                      "size-2.5 rounded-full transition",
                      index === pageIndex ? "bg-[var(--color-deep-green)]" : "bg-[var(--color-line)] hover:bg-[var(--color-bronze)]",
                    ].join(" ")}
                    key={index}
                    onClick={() => setPageIndex(index)}
                    type="button"
                  />
                ))}
              </div>
              <MobileSlideButton label="다음 성장 프로필 보기" onClick={() => movePeople(1)} side="right" />
            </div>
          )}
          {!loading && !error && memberReady && (
            <MonthlyActionPlanBoard
              month={currentMonth}
              plans={monthlyActionPlans}
            />
          )}
        </div>
      </section>
    </main>
  );
}

function shufflePeople(people: ProfileCard[]) {
  const shuffled = [...people];
  for (let index = shuffled.length - 1; index > 0; index -= 1) {
    const randomIndex = Math.floor(Math.random() * (index + 1));
    [shuffled[index], shuffled[randomIndex]] = [shuffled[randomIndex], shuffled[index]];
  }
  return shuffled;
}

function shuffleActionPlans(plans: MonthlyActionPlanShowcase[]) {
  const shuffled = [...plans];
  for (let index = shuffled.length - 1; index > 0; index -= 1) {
    const randomIndex = Math.floor(Math.random() * (index + 1));
    [shuffled[index], shuffled[randomIndex]] = [shuffled[randomIndex], shuffled[index]];
  }
  return shuffled;
}

function PeopleSkeleton() {
  return (
    <div className="grid gap-2.5 sm:grid-cols-2 sm:gap-3 lg:grid-cols-4" aria-label="성장하는 사람들 로딩 중">
      {Array.from({ length: 4 }).map((_, index) => (
        <article className={["relative grid overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-warm-white)] shadow-[var(--shadow-soft)]", index > 1 ? "hidden sm:grid" : ""].join(" ")} key={index}>
          <SkeletonBlock className="absolute right-2.5 top-2.5 size-6 rounded-full" />
          <div className="grid gap-2 p-2.5 sm:p-3">
            <SkeletonBlock className="h-4 w-24" />
            <SkeletonBlock className="h-5 w-28" />
            <SkeletonBlock className="h-4 w-full" />
            <SkeletonBlock className="h-4 w-5/6" />
            <div className="flex gap-1.5">
              <SkeletonBlock className="h-7 w-16 rounded-full" />
              <SkeletonBlock className="h-7 w-14 rounded-full" />
            </div>
            <SkeletonBlock className="h-4 w-full" />
          </div>
        </article>
      ))}
    </div>
  );
}

function MonthlyActionPlanBoard({
  month,
  plans,
}: {
  month: string;
  plans: MonthlyActionPlanShowcase[];
}) {
  const [expandedPlanIds, setExpandedPlanIds] = useState<Set<number>>(new Set());
  const [pageIndex, setPageIndex] = useState(0);
  const dragStartX = useRef<number | null>(null);
  const pageSize = 3;
  const pageCount = Math.max(1, Math.ceil(plans.length / pageSize));
  const currentPageIndex = Math.min(pageIndex, pageCount - 1);
  const visiblePlans = plans.slice(currentPageIndex * pageSize, currentPageIndex * pageSize + pageSize);

  function togglePlan(planId: number) {
    setExpandedPlanIds((current) => {
      const next = new Set(current);
      if (next.has(planId)) {
        next.delete(planId);
      } else {
        next.add(planId);
      }
      return next;
    });
  }

  function movePlans(direction: -1 | 1) {
    if (pageCount <= 1) {
      return;
    }
    setPageIndex((index) => (index + direction + pageCount) % pageCount);
  }

  function finishDrag(clientX: number) {
    if (dragStartX.current === null || pageCount <= 1) {
      dragStartX.current = null;
      return;
    }
    const delta = clientX - dragStartX.current;
    dragStartX.current = null;
    if (Math.abs(delta) < 40) {
      return;
    }
    movePlans(delta < 0 ? 1 : -1);
  }

  return (
    <section className="grid gap-4 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.82)] p-4 shadow-[var(--shadow-soft)] sm:p-5">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <p className="text-xs text-[var(--color-bronze)]">{month}</p>
          <h2 className="mt-1 font-display text-lg font-normal sm:text-xl">이달의 실행 메모</h2>
        </div>
        <p className="text-xs leading-5 text-[var(--color-muted)] sm:text-sm">성장하는 사람들이 남긴 {plans.length}개의 기록</p>
      </div>

      {plans.length === 0 ? (
        <p className="rounded-[var(--radius-card)] bg-[rgba(47,90,67,0.045)] px-4 py-3 text-sm leading-6 text-[var(--color-muted)]">
          이번 달 실행계획이 아직 쌓이지 않았습니다.
        </p>
      ) : (
        <>
          <div className={pageCount > 1 ? "grid items-stretch gap-2 sm:grid-cols-[2.25rem_1fr_2.25rem]" : ""}>
            {pageCount > 1 && (
              <SideSlideButton label="이전 실행 메모 보기" onClick={() => movePlans(-1)} side="left" />
            )}
            <div
              className="min-w-0 select-none"
              onMouseDown={(event) => {
                dragStartX.current = event.clientX;
              }}
              onMouseLeave={() => {
                dragStartX.current = null;
              }}
              onMouseUp={(event) => finishDrag(event.clientX)}
              onTouchEnd={(event) => {
                const touch = event.changedTouches[0];
                if (touch) {
                  finishDrag(touch.clientX);
                }
              }}
              onTouchStart={(event) => {
                dragStartX.current = event.touches[0]?.clientX ?? null;
              }}
            >
              <div className="grid gap-3 sm:grid-cols-3">
            {visiblePlans.map((plan) => {
              const expanded = expandedPlanIds.has(plan.id);
              return (
                <article
                  aria-expanded={expanded}
                  className="cursor-pointer rounded-[var(--radius-card)] border border-[rgba(229,222,209,0.86)] bg-[linear-gradient(180deg,var(--color-warm-white),rgba(246,241,232,0.58))] p-3 shadow-[0_16px_34px_rgba(63,47,34,0.08)] transition hover:border-[rgba(31,77,58,0.22)] sm:p-4"
                  key={plan.id}
                  onClick={() => togglePlan(plan.id)}
                  onKeyDown={(event) => {
                    if (event.key === "Enter" || event.key === " ") {
                      event.preventDefault();
                      togglePlan(plan.id);
                    }
                  }}
                  role="button"
                  tabIndex={0}
                >
                  <div className="flex items-start gap-3">
                    {plan.profileImageUrl ? (
                      <div
                        aria-label={`${plan.displayName} 프로필 이미지`}
                        className="size-8 shrink-0 rounded-full bg-cover bg-center ring-1 ring-[var(--color-line)]"
                        role="img"
                        style={{ backgroundImage: `url(${plan.profileImageUrl})` }}
                      />
                    ) : (
                      <div className="flex size-8 shrink-0 items-center justify-center rounded-full bg-[var(--color-deep-green)] text-xs text-[var(--color-warm-white)]">
                        {plan.displayName.slice(0, 1)}
                      </div>
                    )}
                    <div className="min-w-0">
                      <p className="text-xs text-[var(--color-muted)]">{plan.displayName} · {formatKoreanDate(plan.updatedAt)}</p>
                      <h3 className="mt-1 text-sm font-normal leading-5 text-[var(--color-ink)]">{plan.title || "이번 달 실행계획"}</h3>
                    </div>
                  </div>
                  <p className={["mt-3 whitespace-pre-wrap break-words text-xs leading-6 text-[var(--color-charcoal)] sm:text-sm sm:leading-7", expanded ? "" : "line-clamp-2"].join(" ")}>
                    {plan.content}
                  </p>
                  <button
                    aria-expanded={expanded}
                    className="mt-1 inline-flex items-center gap-0.5 text-[var(--color-deep-green)]"
                    onClick={(event) => {
                      event.stopPropagation();
                      togglePlan(plan.id);
                    }}
                    style={{ fontSize: "8px", lineHeight: 1 }}
                    type="button"
                  >
                    {expanded ? "접기" : "더 읽기"}
                    <span aria-hidden="true" className={["block size-[3px] border-b border-r border-current transition", expanded ? "rotate-[225deg] translate-y-0.5" : "rotate-45 -translate-y-0.5"].join(" ")} />
                  </button>
                </article>
              );
            })}
              </div>
            </div>
            {pageCount > 1 && (
              <SideSlideButton label="다음 실행 메모 보기" onClick={() => movePlans(1)} side="right" />
            )}
          </div>

          {pageCount > 1 && (
            <div className="flex items-center justify-center gap-3 pt-1 sm:gap-2">
              <MobileSlideButton label="이전 실행 메모 보기" onClick={() => movePlans(-1)} side="left" />
              <div className="flex items-center gap-2" aria-label="이달의 실행 메모 슬라이드">
                {Array.from({ length: pageCount }).map((_, index) => (
                  <button
                    aria-current={index === currentPageIndex}
                    aria-label={`${index + 1}번째 실행 메모 보기`}
                    className={[
                      "size-2 rounded-full transition",
                      index === currentPageIndex ? "bg-[var(--color-deep-green)]" : "bg-[var(--color-line)] hover:bg-[var(--color-bronze)]",
                    ].join(" ")}
                    key={index}
                    onClick={() => setPageIndex(index)}
                    type="button"
                  />
                ))}
              </div>
              <MobileSlideButton label="다음 실행 메모 보기" onClick={() => movePlans(1)} side="right" />
            </div>
          )}
        </>
      )}
    </section>
  );
}

function SideSlideButton({
  label,
  onClick,
  side,
}: {
  label: string;
  onClick: () => void;
  side: "left" | "right";
}) {
  return (
    <button
      aria-label={label}
      className="group hidden min-h-full items-center justify-center text-[#466657] transition hover:text-[var(--color-deep-green)] active:translate-y-px sm:flex"
      onClick={onClick}
      type="button"
    >
      <span className="grid size-7 place-items-center rounded-full border border-[rgba(31,77,58,0.14)] bg-[rgba(255,254,250,0.72)] shadow-[0_8px_18px_rgba(63,47,34,0.06)] transition group-hover:border-[rgba(31,77,58,0.26)] group-hover:bg-[rgba(246,241,232,0.88)]">
        <ArrowIcon side={side} />
      </span>
    </button>
  );
}

function MobileSlideButton({
  label,
  onClick,
  side,
}: {
  label: string;
  onClick: () => void;
  side: "left" | "right";
}) {
  return (
    <button
      aria-label={label}
      className="group grid size-10 place-items-center rounded-full text-[#466657] transition active:scale-95 sm:hidden"
      onClick={onClick}
      type="button"
    >
      <span className="grid size-7 place-items-center rounded-full border border-[rgba(31,77,58,0.14)] bg-[rgba(255,254,250,0.82)] shadow-[0_8px_18px_rgba(63,47,34,0.06)] transition group-active:bg-[rgba(246,241,232,0.9)]">
        <ArrowIcon side={side} />
      </span>
    </button>
  );
}

function ArrowIcon({ side }: { side: "left" | "right" }) {
  return (
    <span
      aria-hidden="true"
      className={[
        "block size-2 border-b border-current",
        side === "left" ? "rotate-45 border-l" : "-rotate-45 border-r",
      ].join(" ")}
    />
  );
}

function formatKoreanDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeZone: "Asia/Seoul",
  }).format(new Date(value));
}
