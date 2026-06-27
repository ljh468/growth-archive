"use client";

import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import { EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiGet, type ProfileCard } from "@/lib/api";

export default function PeoplePage() {
  const [people, setPeople] = useState<ProfileCard[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [pageIndex, setPageIndex] = useState(0);
  const dragStartX = useRef<number | null>(null);
  const pageSize = 4;
  const pageCount = Math.max(1, Math.ceil(people.length / pageSize));
  const visiblePeople = people.slice(pageIndex * pageSize, pageIndex * pageSize + pageSize);
  const visibleSlots = visiblePeople.length
    ? [...visiblePeople, ...Array<null>(pageSize - visiblePeople.length).fill(null)]
    : [];
  const showProfileImages = people.some((person) => person.profileImageUrl);

  useEffect(() => {
    apiGet<ProfileCard[]>("/people")
      .then((result) => {
        if (!result.success) {
          setError(result.error?.message ?? "성장 프로필을 불러오지 못했습니다.");
          return;
        }
        setPeople(shufflePeople(result.data));
        setPageIndex(0);
      })
      .catch(() => setError("성장 프로필을 불러오지 못했습니다."));
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
        <div className="relative mx-auto flex min-h-[400px] max-w-6xl items-end px-5 py-16 sm:px-8 lg:px-10">
          <PageHeader
            eyebrow="Growth People"
            title="성장하는 사람들"
            description="읽고, 실행하고, 성장한 기록을 남기는 사람들의 방향을 보여주는 프로필 쇼케이스입니다."
          />
        </div>
      </section>
      <Section>
        <div className="grid gap-8">
          {error && <EmptyState title="불러오기 실패" description={error} />}
          {!error && people.length === 0 && <EmptyState title="아직 보여줄 성장 프로필이 없어요." description="첫 프로필이 완성되면 이곳에 표시됩니다." />}
          <div
            className="relative select-none"
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
            {people.length > pageSize && (
              <>
                <button
                  aria-label="이전 성장 프로필 보기"
                  className="absolute inset-y-0 left-0 z-10 flex w-10 items-center justify-start bg-gradient-to-r from-[rgba(31,77,58,0.10)] to-transparent pl-1.5 opacity-75 transition hover:opacity-100 sm:w-14 sm:pl-2"
                  onClick={(event) => {
                    event.stopPropagation();
                    movePeople(-1);
                  }}
                  type="button"
                >
                  <span className="grid size-7 place-items-center rounded-full border border-[rgba(31,77,58,0.16)] bg-[rgba(255,253,248,0.82)] shadow-[var(--shadow-soft)]">
                    <span className="block size-2.5 rotate-45 border-b border-l border-[var(--color-deep-green)]" />
                  </span>
                </button>
                <button
                  aria-label="다음 성장 프로필 보기"
                  className="absolute inset-y-0 right-0 z-10 flex w-10 items-center justify-end bg-gradient-to-l from-[rgba(31,77,58,0.10)] to-transparent pr-1.5 opacity-75 transition hover:opacity-100 sm:w-14 sm:pr-2"
                  onClick={(event) => {
                    event.stopPropagation();
                    movePeople(1);
                  }}
                  type="button"
                >
                  <span className="grid size-7 place-items-center rounded-full border border-[rgba(31,77,58,0.16)] bg-[rgba(255,253,248,0.82)] shadow-[var(--shadow-soft)]">
                    <span className="block size-2.5 -rotate-45 border-b border-r border-[var(--color-deep-green)]" />
                  </span>
                </button>
              </>
            )}
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
              {visibleSlots.map((person, index) => person ? (
                <Link className="group grid h-full overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-warm-white)] shadow-[var(--shadow-soft)] transition hover:-translate-y-0.5" href={`/people/${person.memberId}`} key={person.memberId}>
                  {showProfileImages && (
                    <div className="h-24 border-b border-[var(--color-line)] bg-[rgba(248,246,238,0.76)] sm:h-28">
                      {person.profileImageUrl ? (
                        <div
                          aria-label={`${person.displayName} 프로필 이미지`}
                          className="h-full w-full bg-cover bg-center photo-muted"
                          role="img"
                          style={{ backgroundImage: `url(${person.profileImageUrl})` }}
                        />
                      ) : (
                        <div className="h-full w-full bg-[rgba(47,90,67,0.06)]" />
                      )}
                    </div>
                  )}
                  <div className="flex flex-col p-3">
                    <div className="flex min-h-10 items-start justify-between gap-2">
                      <div className="min-w-0">
                        <p className="font-latin text-lg leading-none text-[var(--color-bronze)]">People Profile</p>
                        <h2 className="mt-1 line-clamp-1 min-h-5 text-base font-normal">{person.displayName}</h2>
                      </div>
                      {person.role === "ADMIN" && (
                        <span className="shrink-0 rounded-full border border-[rgba(47,90,67,0.22)] px-2 py-0.5 text-[10px] text-[var(--color-deep-green)]">운영진</span>
                      )}
                    </div>
                    <p className="mt-1 line-clamp-1 min-h-4 text-[11px] leading-4 text-[var(--color-muted)]">{person.oneLineIntro}</p>
                    <p className="mt-2 line-clamp-2 min-h-10 text-xs leading-5 text-[var(--color-charcoal)]" style={{ wordBreak: "keep-all" }}>
                      {person.futureMeAt50Summary}
                    </p>
                    <div className="mt-2 flex flex-wrap gap-1.5">
                      {person.interestTags.slice(0, 2).map((tag) => (
                        <Tag key={tag}>{tag}</Tag>
                      ))}
                    </div>
                    <div className="mt-2 grid grid-cols-2 gap-2 border-t border-[var(--color-line)] pt-2 text-[11px] text-[var(--color-muted)]">
                      <span>독서 {person.growthStats.readingRecordCount}</span>
                      <span>후기 {person.growthStats.meetingReviewCount}</span>
                    </div>
                    {person.recentPublicActivity && (
                      <p className="mt-2 line-clamp-1 text-[11px] text-[var(--color-muted)]">최근 기록: {person.recentPublicActivity.title}</p>
                    )}
                  </div>
                </Link>
              ) : (
                <div aria-hidden="true" className="hidden h-full rounded-[var(--radius-card)] lg:block lg:invisible" key={`empty-slot-${index}`} />
              ))}
            </div>
          </div>
          {people.length > pageSize && (
            <div className="flex items-center justify-center">
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
            </div>
          )}
        </div>
      </Section>
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
