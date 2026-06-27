"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { Avatar, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
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
        {!profile && !error && <EmptyState title="불러오는 중입니다." description="성장 프로필을 확인하고 있습니다." />}
        {profile && (
          <div className="grid gap-8">
            <div className="flex items-start gap-5">
              <Avatar name={profile.displayName} />
              <PageHeader eyebrow="Growth Profile" title={profile.displayName} description={profile.oneLineIntro} />
            </div>
            <div className="flex flex-wrap gap-2">
              {profile.interestTags.map((tag) => (
                <Tag key={tag}>{tag}</Tag>
              ))}
            </div>
            <Card>
              <Tag>50살의 나</Tag>
              <p className="mt-5 text-xl leading-8">{profile.futureMeAt50}</p>
            </Card>
            <div className="grid gap-4 md:grid-cols-5">
              <Stat label="독서" value={profile.growthStats.readingRecordCount} />
              <Stat label="액션플랜" value={profile.growthStats.actionPlanCount} />
              <Stat label="회고" value={profile.growthStats.monthlyReflectionCount} />
              <Stat label="후기" value={profile.growthStats.meetingReviewCount} />
              <Stat label="소소모임" value={profile.growthStats.smallMeetingCreatedCount} />
            </div>
            <section className="grid gap-4">
              <h2 className="text-xl font-normal">최근 공개 독서기록</h2>
              {profile.recentReadingRecords.length === 0 ? (
                <EmptyState title="공개 독서기록이 없습니다." description="ACTIVE 독서기록이 작성되면 표시됩니다." />
              ) : (
                <div className="grid gap-4">
                  {profile.recentReadingRecords.map((record) => (
                    <Card key={record.id}>
                      <Link className="font-normal" href={`/books/${record.bookId}`}>
                        {record.bookTitle}
                      </Link>
                      <p className="mt-3 text-sm leading-6">{record.oneLineReview}</p>
                    </Card>
                  ))}
                </div>
              )}
            </section>
            {profile.memberOnly && (
              <section className="grid gap-4">
                <h2 className="text-xl font-normal">성장하는 사람들 전용 정보</h2>
                <div className="grid gap-4 md:grid-cols-2">
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
    <Card>
      <p className="text-sm text-[var(--color-charcoal)]">{label}</p>
      <p className="mt-3 text-2xl font-normal">{value}</p>
    </Card>
  );
}

function Info({ title, value }: { title: string; value: string | null }) {
  return (
    <Card>
      <h3 className="font-normal">{title}</h3>
      <p className="mt-3 text-sm leading-6 text-[var(--color-charcoal)]">{value || "아직 입력되지 않았습니다."}</p>
    </Card>
  );
}
