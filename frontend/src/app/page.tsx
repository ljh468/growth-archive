"use client";

import type { ReactNode } from "react";
import { useEffect, useState } from "react";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiGet, type LibraryResponse, type MeetingReviewSummary, type MeetingSummary, type ProfileCard } from "@/lib/api";

export default function HomePage() {
  const [library, setLibrary] = useState<LibraryResponse | null>(null);
  const [meetings, setMeetings] = useState<MeetingSummary[]>([]);
  const [reviews, setReviews] = useState<MeetingReviewSummary[]>([]);
  const [people, setPeople] = useState<ProfileCard[]>([]);

  useEffect(() => {
    apiGet<LibraryResponse>("/library").then((result) => result.success && setLibrary(result.data));
    apiGet<MeetingSummary[]>("/meetings?page=0&size=3").then((result) => result.success && setMeetings(result.data));
    apiGet<MeetingReviewSummary[]>("/reviews?page=0&size=3").then((result) => result.success && setReviews(result.data));
    apiGet<ProfileCard[]>("/people?page=0&size=3").then((result) => result.success && setPeople(result.data));
  }, []);

  return (
    <main>
      <Section>
        <div className="grid gap-10">
          <div className="grid min-h-[calc(100vh-12rem)] items-center gap-8 py-10 lg:grid-cols-[1fr_0.9fr]">
            <PageHeader
              eyebrow="부자습관 만들기"
              title="읽고, 실행하고, 성장한 기록을 남기는 사람들"
              description="Growth Archive는 독서기록, 실행계획, 회고, 모임 후기를 조용히 쌓아가는 멤버 전용 성장 아카이브입니다."
            />
            <div className="grid gap-3 border-y border-[var(--color-line)] py-6">
              <p className="text-sm font-semibold text-[var(--color-bronze)]">이번 달 아카이브</p>
              <div className="grid gap-3 sm:grid-cols-3">
                <Metric label="추천책" value={library?.recommendedBooks.length ?? 0} />
                <Metric label="최근 기록" value={library?.recentReadingRecords.length ?? 0} />
                <Metric label="모임" value={meetings.length} />
              </div>
              <div className="flex flex-wrap gap-3 pt-3">
                <Button href="/library">라이브러리</Button>
                <Button href="/meetings" variant="secondary">모임 보기</Button>
              </div>
            </div>
          </div>

          <PreviewSection title="이달의 추천책" href="/library">
            {library?.recommendedBooks.length ? (
              <div className="grid gap-4 md:grid-cols-3">
                {library.recommendedBooks.slice(0, 3).map((book) => (
                  <Card key={book.id}>
                    <Tag>추천 {book.displayOrder}</Tag>
                    <h3 className="mt-4 font-semibold">{book.title}</h3>
                    <p className="mt-2 text-sm text-[var(--color-charcoal)]">{book.authorsText}</p>
                    <p className="mt-4 text-sm leading-6">{book.reason}</p>
                  </Card>
                ))}
              </div>
            ) : (
              <EmptyState title="추천책 준비 중" description="운영진 추천책이 등록되면 이곳에 표시됩니다." />
            )}
          </PreviewSection>

          <PreviewSection title="최근 성장 기록" href="/library">
            {library?.recentReadingRecords.length ? (
              <div className="grid gap-4">
                {library.recentReadingRecords.slice(0, 3).map((record) => (
                  <Card key={record.id}>
                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <div>
                        <a className="font-semibold" href={`/books/${record.bookId}`}>{record.bookTitle}</a>
                        <p className="mt-1 text-sm text-[var(--color-charcoal)]">{record.memberDisplayName}</p>
                      </div>
                      {record.rating && <Tag>{record.rating}/5</Tag>}
                    </div>
                    <p className="mt-4 text-sm leading-6">{record.oneLineReview}</p>
                  </Card>
                ))}
              </div>
            ) : (
              <EmptyState title="기록을 기다리는 중" description="공개 독서기록이 작성되면 최근 성장 기록에 표시됩니다." />
            )}
          </PreviewSection>

          <PreviewSection title="인기 도서 TOP5" href="/library">
            {library?.popularBooks.length ? (
              <div className="grid gap-4 md:grid-cols-5">
                {library.popularBooks.slice(0, 5).map((book, index) => (
                  <Card key={book.id}>
                    <Tag>TOP {index + 1}</Tag>
                    <a className="mt-4 block font-semibold" href={`/books/${book.id}`}>{book.title}</a>
                    <p className="mt-2 text-sm text-[var(--color-charcoal)]">{book.authorsText}</p>
                    <p className="mt-4 text-sm">{book.readingRecordCount} records</p>
                  </Card>
                ))}
              </div>
            ) : (
              <EmptyState title="인기 도서 집계 전" description="공개 독서기록이 쌓이면 TOP5가 표시됩니다." />
            )}
          </PreviewSection>

          <div className="grid gap-8 lg:grid-cols-2">
            <PreviewSection title="다가오는 모임" href="/meetings">
              <div className="grid gap-4">
                {meetings.length ? meetings.map((meeting) => (
                  <Card key={meeting.id}>
                    <div className="flex flex-wrap gap-2">
                      <Tag>{meeting.meetingType}</Tag>
                      <Tag>{meeting.status}</Tag>
                    </div>
                    <a className="mt-4 block font-semibold" href={`/meetings/${meeting.id}`}>{meeting.title}</a>
                    <p className="mt-2 text-sm text-[var(--color-charcoal)]">{formatDate(meeting.meetingAt)} · {meeting.locationRegion}</p>
                  </Card>
                )) : <EmptyState title="모임 준비 중" description="정기모임과 소소모임이 등록되면 표시됩니다." />}
              </div>
            </PreviewSection>

            <PreviewSection title="최근 모임 후기" href="/reviews">
              <div className="grid gap-4">
                {reviews.length ? reviews.map((review) => (
                  <Card key={review.id}>
                    {review.representativeImageUrl && (
                      <div
                        aria-hidden="true"
                        className="mb-4 aspect-[16/9] w-full bg-cover bg-center"
                        style={{ backgroundImage: `url(${review.representativeImageUrl})` }}
                      />
                    )}
                    <a className="font-semibold" href={`/reviews/${review.id}`}>{review.title}</a>
                    <p className="mt-2 text-sm text-[var(--color-charcoal)]">{review.meetingTitle} · {review.memberDisplayName}</p>
                    <p className="mt-4 text-sm leading-6">{review.contentSummary}</p>
                  </Card>
                )) : <EmptyState title="후기를 기다리는 중" description="모임 후기가 작성되면 최근 후기에 표시됩니다." />}
              </div>
            </PreviewSection>
          </div>

          <PreviewSection title="성장하는 사람들" href="/people">
            {people.length ? (
              <div className="grid gap-4 md:grid-cols-3">
                {people.map((person) => (
                  <Card key={person.memberId}>
                    <a className="font-semibold" href={`/people/${person.memberId}`}>{person.displayName}</a>
                    <p className="mt-3 text-sm leading-6">{person.oneLineIntro}</p>
                    {person.interestTags.length > 0 && <p className="mt-4 text-sm text-[var(--color-charcoal)]">{person.interestTags.join(", ")}</p>}
                  </Card>
                ))}
              </div>
            ) : (
              <EmptyState title="프로필 준비 중" description="온보딩을 마친 멤버 프로필이 이곳에 표시됩니다." />
            )}
          </PreviewSection>
        </div>
      </Section>
    </main>
  );
}

function PreviewSection({ title, href, children }: { title: string; href: string; children: ReactNode }) {
  return (
    <section className="grid gap-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-xl font-semibold">{title}</h2>
        <Button href={href} variant="secondary">전체 보기</Button>
      </div>
      {children}
    </section>
  );
}

function Metric({ label, value }: { label: string; value: number }) {
  return (
    <div className="border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4">
      <p className="text-xs text-[var(--color-charcoal)]">{label}</p>
      <p className="mt-2 text-2xl font-semibold">{value}</p>
    </div>
  );
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeStyle: "short",
    timeZone: "Asia/Seoul",
  }).format(new Date(value));
}
