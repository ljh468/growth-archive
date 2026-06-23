"use client";

import { useEffect, useState } from "react";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiGet, type LibraryResponse } from "@/lib/api";

export default function LibraryPage() {
  const [library, setLibrary] = useState<LibraryResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    apiGet<LibraryResponse>("/library")
      .then((result) => {
        if (!result.success) {
          setError(result.error?.message ?? "라이브러리를 불러오지 못했습니다.");
          return;
        }
        setLibrary(result.data);
      })
      .catch(() => setError("라이브러리를 불러오지 못했습니다."));
  }, []);

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <div className="flex flex-wrap items-end justify-between gap-4">
            <PageHeader
              eyebrow="Reading Library"
              title="독서기록 라이브러리"
              description="멤버들이 남긴 공개 독서기록과 책별 성장 기록을 탐색합니다."
            />
            <Button href="/reading-records/new">독서기록 작성</Button>
          </div>

          {error && <EmptyState title="불러오기 실패" description={error} />}
          {!library && !error && <EmptyState title="불러오는 중입니다." description="공개 독서기록 데이터를 확인하고 있습니다." />}

          {library && (
            <div className="grid gap-8">
              <section className="grid gap-4">
                <h2 className="text-xl font-semibold">이달의 추천책</h2>
                {library.recommendedBooks.length === 0 ? (
                  <EmptyState title="추천책 준비 중" description="운영진 추천책이 등록되면 이곳에 표시됩니다." />
                ) : (
                  <div className="grid gap-4 md:grid-cols-3">
                    {library.recommendedBooks.map((book) => (
                      <Card key={book.id}>
                        <Tag>추천 {book.displayOrder}</Tag>
                        <h3 className="mt-4 font-semibold">{book.title}</h3>
                        <p className="mt-2 text-sm text-[var(--color-charcoal)]">{book.authorsText}</p>
                        <p className="mt-4 text-sm leading-6">{book.reason}</p>
                      </Card>
                    ))}
                  </div>
                )}
              </section>

              <section className="grid gap-4">
                <h2 className="text-xl font-semibold">인기 도서 TOP5</h2>
                {library.popularBooks.length === 0 ? (
                  <EmptyState title="인기 도서가 아직 없습니다." description="공개 독서기록이 쌓이면 자동으로 계산됩니다." />
                ) : (
                  <div className="grid gap-4 md:grid-cols-5">
                    {library.popularBooks.map((book, index) => (
                      <Card key={book.id}>
                        <Tag>TOP {index + 1}</Tag>
                        <a className="mt-4 block font-semibold" href={`/books/${book.id}`}>
                          {book.title}
                        </a>
                        <p className="mt-2 text-sm text-[var(--color-charcoal)]">{book.readingRecordCount} records</p>
                      </Card>
                    ))}
                  </div>
                )}
              </section>

              <section className="grid gap-4">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <h2 className="text-xl font-semibold">최근 독서기록</h2>
                  <Button href="/books" variant="secondary">
                    책 검색
                  </Button>
                </div>
                {library.recentReadingRecords.length === 0 ? (
                  <EmptyState title="독서기록이 아직 없습니다." description="첫 독서기록이 작성되면 공개 피드에 표시됩니다." />
                ) : (
                  <div className="grid gap-4">
                    {library.recentReadingRecords.map((record) => (
                      <Card key={record.id}>
                        <div className="flex flex-wrap justify-between gap-3">
                          <div>
                            <a className="font-semibold" href={`/books/${record.bookId}`}>
                              {record.bookTitle}
                            </a>
                            <p className="mt-1 text-sm text-[var(--color-charcoal)]">{record.memberDisplayName}</p>
                          </div>
                          {record.rating && <Tag>{record.rating}/5</Tag>}
                        </div>
                        <p className="mt-4 text-sm leading-6">{record.oneLineReview}</p>
                        <a className="mt-4 inline-block text-sm font-semibold text-[var(--color-deep-green)]" href={record.blogUrl}>
                          블로그 원문
                        </a>
                      </Card>
                    ))}
                  </div>
                )}
              </section>
            </div>
          )}
        </div>
      </Section>
    </main>
  );
}
