"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect, useState } from "react";
import { Avatar, Card, EmptyState, PageHeader, Section, SkeletonBlock, Tag } from "@/components/ui/primitives";
import { apiGet, type ProfileDetail } from "@/lib/api";

export function ProfileDetailClient({ memberId }: { memberId: string }) {
  const [profile, setProfile] = useState<ProfileDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    apiGet<ProfileDetail>(`/people/${memberId}`)
      .then((result) => {
        if (!result.success) {
          setError(result.error?.message ?? "프로필을 불러오지 못했습니다.");
          return;
        }
        setProfile(result.data);
      })
      .catch(() => setError("프로필을 불러오지 못했습니다."));
  }, [memberId]);

  return (
    <main>
      <Section>
        {error && <EmptyState title="불러오기 실패" description={error} />}
        {!profile && !error && <ProfileDetailSkeleton />}
        {profile && (
          <div className="grid gap-5 sm:gap-8">
            <div className="flex items-start gap-3 sm:gap-5">
              {profile.profileImageUrl ? (
                <Image
                  alt={`${profile.displayName} 프로필 이미지`}
                  className="size-14 shrink-0 rounded-full object-cover shadow-[var(--shadow-soft)] ring-1 ring-[var(--color-line)] sm:size-20"
                  height={80}
                  src={profile.profileImageUrl}
                  unoptimized
                  width={80}
                />
              ) : (
                <Avatar name={profile.displayName} />
              )}
              <PageHeader eyebrow="Growth Profile" title={profile.displayName} description={profile.oneLineIntro} />
            </div>
            <div className="flex flex-wrap gap-1.5 sm:gap-2">
              {profile.interestTags.map((tag) => (
                <Tag key={tag}>{tag}</Tag>
              ))}
            </div>
            <Card>
              <Tag>50살의 나</Tag>
              <p className="mt-3 text-sm leading-6 text-[var(--color-charcoal)] sm:mt-5 sm:text-base sm:leading-8">{profile.futureMeAt50}</p>
            </Card>
            <div className="grid gap-1.5 sm:grid-cols-3 sm:gap-2 md:grid-cols-5">
              <Stat label="독서" value={profile.growthStats.readingRecordCount} />
              <Stat label="액션플랜" value={profile.growthStats.actionPlanCount} />
              <Stat label="회고" value={profile.growthStats.monthlyReflectionCount} />
              <Stat label="후기" value={profile.growthStats.meetingReviewCount} />
              <Stat label="만든 소소모임" value={profile.growthStats.smallMeetingCreatedCount} />
            </div>
            <section className="grid gap-3 sm:gap-4">
              <h2 className="text-lg font-normal sm:text-xl">최근 공개 독서기록</h2>
              {profile.recentReadingRecords.length === 0 ? (
                <EmptyState title="공개 독서기록이 없습니다." description="ACTIVE 독서기록이 작성되면 표시됩니다." />
              ) : (
                <div className="grid gap-2 sm:gap-4">
                  {profile.recentReadingRecords.map((record) => (
                    <Card key={record.id}>
                      <Link className="font-normal" href={`/books/${record.bookId}`}>
                        {record.bookTitle}
                      </Link>
                      <p className="mt-2 text-sm leading-6 text-[var(--color-charcoal)]">{record.oneLineReview}</p>
                    </Card>
                  ))}
                </div>
              )}
            </section>
            {profile.memberOnly && (
              <section className="grid gap-3 sm:gap-4">
                <h2 className="text-lg font-normal sm:text-xl">성장하는 사람들 전용 정보</h2>
                <div className="grid gap-2 sm:gap-4 md:grid-cols-2">
                  <Info title="가입 이유" value={profile.memberOnly.joinReason} />
                  <Info title="현재 고민" value={profile.memberOnly.currentConcern} />
                  <Info title="3년 뒤 목표" value={profile.memberOnly.threeYearGoal} />
                </div>
              </section>
            )}
          </div>
        )}
      </Section>
    </main>
  );
}

function Stat({ label, value }: { label: string; value: number }) {
  return (
    <div className="flex items-center justify-between rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.82)] px-3 py-2 shadow-[var(--shadow-soft)] sm:block sm:p-4">
      <p className="text-xs text-[var(--color-muted)]">{label}</p>
      <p className="font-display text-base font-normal leading-none text-[var(--color-deep-green)] sm:mt-2 sm:text-2xl">{value}</p>
    </div>
  );
}

function Info({ title, value }: { title: string; value: string | null }) {
  return (
    <Card>
      <h3 className="text-sm font-normal sm:text-base">{title}</h3>
      <p className="mt-2 text-sm leading-6 text-[var(--color-charcoal)] sm:mt-3">{value || "아직 입력되지 않았습니다."}</p>
    </Card>
  );
}

function ProfileDetailSkeleton() {
  return (
    <div className="grid gap-5 sm:gap-8" aria-label="성장 프로필 로딩 중">
      <div className="flex items-start gap-3 sm:gap-5">
        <SkeletonBlock className="size-14 shrink-0 rounded-full sm:size-20" />
        <div className="grid flex-1 gap-3">
          <SkeletonBlock className="h-8 w-36" />
          <SkeletonBlock className="h-8 w-48 sm:h-10" />
          <SkeletonBlock className="hidden h-4 w-full max-w-xl sm:block" />
        </div>
      </div>
      <div className="flex flex-wrap gap-2">
        {Array.from({ length: 5 }).map((_, index) => (
          <SkeletonBlock className="h-7 w-16 rounded-full" key={index} />
        ))}
      </div>
      <div className="rounded-[var(--radius-card)] border border-[rgba(233,225,214,0.8)] bg-[rgba(255,254,250,0.94)] p-4 shadow-[var(--shadow-soft)] sm:p-6">
        <SkeletonBlock className="h-7 w-24 rounded-full" />
        <div className="mt-5 grid gap-2">
          <SkeletonBlock className="h-4 w-full" />
          <SkeletonBlock className="h-4 w-11/12" />
          <SkeletonBlock className="h-4 w-4/5" />
        </div>
      </div>
      <div className="grid gap-1.5 sm:grid-cols-3 sm:gap-2 md:grid-cols-5">
        {Array.from({ length: 5 }).map((_, index) => (
          <SkeletonBlock className="h-10 sm:h-24" key={index} />
        ))}
      </div>
      <section className="grid gap-3 sm:gap-4">
        <SkeletonBlock className="h-7 w-40" />
        {Array.from({ length: 2 }).map((_, index) => (
          <div className="rounded-[var(--radius-card)] border border-[rgba(233,225,214,0.8)] bg-[rgba(255,254,250,0.94)] p-4 shadow-[var(--shadow-soft)] sm:p-6" key={index}>
            <SkeletonBlock className="h-5 w-48" />
            <SkeletonBlock className="mt-3 h-4 w-full" />
          </div>
        ))}
      </section>
    </div>
  );
}
