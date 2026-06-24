"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { Avatar, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiGet, type ProfileCard } from "@/lib/api";

export default function PeoplePage() {
  const [people, setPeople] = useState<ProfileCard[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    apiGet<ProfileCard[]>("/people")
      .then((result) => {
        if (!result.success) {
          setError(result.error?.message ?? "성장 프로필을 불러오지 못했습니다.");
          return;
        }
        setPeople(result.data);
      })
      .catch(() => setError("성장 프로필을 불러오지 못했습니다."));
  }, []);

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <PageHeader
            eyebrow="Growth People"
            title="성장하는 사람들"
            description="읽고, 실행하고, 성장한 기록을 남기는 사람들의 방향을 보여주는 프로필 쇼케이스입니다."
          />
          {error && <EmptyState title="불러오기 실패" description={error} />}
          {!error && people.length === 0 && <EmptyState title="아직 보여줄 성장 프로필이 없어요." description="첫 프로필이 완성되면 이곳에 표시됩니다." />}
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {people.map((person) => (
              <Card key={person.memberId}>
                <div className="flex items-start gap-4">
                  <Avatar name={person.displayName} />
                  <div>
                    <Link className="font-semibold" href={`/people/${person.memberId}`}>
                      {person.displayName}
                    </Link>
                    <p className="mt-1 text-sm leading-6 text-[var(--color-charcoal)]">{person.oneLineIntro}</p>
                  </div>
                </div>
                <div className="mt-5 flex flex-wrap gap-2">
                  {person.interestTags.map((tag) => (
                    <Tag key={tag}>{tag}</Tag>
                  ))}
                </div>
                <p className="mt-5 text-sm leading-6">{person.futureMeAt50Summary}</p>
                <div className="mt-5 grid grid-cols-2 gap-3 text-sm">
                  <span>독서 {person.growthStats.readingRecordCount}</span>
                  <span>후기 {person.growthStats.meetingReviewCount}</span>
                </div>
                {person.recentPublicActivity && (
                  <p className="mt-5 text-sm text-[var(--color-charcoal)]">최근 기록: {person.recentPublicActivity.title}</p>
                )}
              </Card>
            ))}
          </div>
        </div>
      </Section>
    </main>
  );
}
