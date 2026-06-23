"use client";

import { useEffect, useState } from "react";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiGet, type BookDetailResponse } from "@/lib/api";

export function BookDetailClient({ bookId }: { bookId: string }) {
  const [detail, setDetail] = useState<BookDetailResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    apiGet<BookDetailResponse>(`/books/${bookId}`)
      .then((result) => {
        if (!result.success) {
          setError(result.error?.message ?? "책 상세를 불러오지 못했습니다.");
          return;
        }
        setDetail(result.data);
      })
      .catch(() => setError("책 상세를 불러오지 못했습니다."));
  }, [bookId]);

  return (
    <main>
      <Section>
        {error && <EmptyState title="불러오기 실패" description={error} />}
        {!detail && !error && <EmptyState title="불러오는 중입니다." description="책 상세와 독서기록을 확인하고 있습니다." />}
        {detail && (
          <div className="grid gap-8">
            <div className="flex flex-wrap items-end justify-between gap-4">
              <PageHeader
                eyebrow="Book Detail"
                title={detail.book.title}
                description={`${detail.book.authorsText}${detail.book.publisher ? ` · ${detail.book.publisher}` : ""}`}
              />
              <Button href="/reading-records/new">이 책으로 기록하기</Button>
            </div>

            <div className="grid gap-4 md:grid-cols-3">
              <Card>
                <Tag>평균 평점</Tag>
                <p className="mt-4 text-2xl font-semibold">{detail.book.averageRating?.toFixed(1) ?? "-"}</p>
              </Card>
              <Card>
                <Tag>독서기록</Tag>
                <p className="mt-4 text-2xl font-semibold">{detail.book.readingRecordCount}</p>
              </Card>
              <Card>
                <Tag>읽은 사람</Tag>
                <p className="mt-4 text-2xl font-semibold">{detail.book.readerCount}</p>
              </Card>
            </div>

            <section className="grid gap-4">
              <h2 className="text-xl font-semibold">작성자별 독서기록</h2>
              {detail.readingRecords.length === 0 ? (
                <EmptyState title="공개 독서기록이 없습니다." description="ACTIVE 독서기록이 작성되면 여기에 표시됩니다." />
              ) : (
                <div className="grid gap-4">
                  {detail.readingRecords.map((record) => (
                    <details className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-5" key={record.id}>
                      <summary className="cursor-pointer font-semibold">
                        {record.memberDisplayName} · {record.rating ? `${record.rating}/5` : "평점 없음"}
                      </summary>
                      <p className="mt-4 text-sm leading-6">{record.oneLineReview}</p>
                      <a className="mt-4 inline-block text-sm font-semibold text-[var(--color-deep-green)]" href={record.blogUrl}>
                        블로그 원문
                      </a>
                    </details>
                  ))}
                </div>
              )}
            </section>
          </div>
        )}
      </Section>
    </main>
  );
}
