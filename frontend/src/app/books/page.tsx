"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiGet, type BookSummary } from "@/lib/api";

export default function BooksPage() {
  const [query, setQuery] = useState("");
  const [books, setBooks] = useState<BookSummary[]>([]);
  const [message, setMessage] = useState("검색어를 입력하면 내부 책 아카이브를 검색합니다.");

  async function search(event: FormEvent) {
    event.preventDefault();
    if (query.trim().length < 2) {
      setMessage("검색어는 2자 이상 입력해 주세요.");
      setBooks([]);
      return;
    }
    const result = await apiGet<BookSummary[]>(`/books?query=${encodeURIComponent(query.trim())}`);
    if (!result.success) {
      setMessage(result.error?.message ?? "책을 검색하지 못했습니다.");
      return;
    }
    setBooks(result.data);
    setMessage(result.data.length === 0 ? "내부 아카이브에서 찾지 못했습니다. 성장하는 사람들은 독서기록 작성 화면에서 카카오 검색 또는 직접 등록을 사용할 수 있습니다." : "");
  }

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <PageHeader
            eyebrow="Books"
            title="책 아카이브"
            description="내부에 저장된 책을 검색하고 책별 독서기록으로 이동합니다."
          />
          <form className="flex flex-col gap-3 sm:flex-row" onSubmit={search}>
            <input
              className="min-h-11 flex-1 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
              onChange={(event) => setQuery(event.target.value)}
              placeholder="책 제목, 저자, ISBN"
              value={query}
            />
            <Button type="submit">검색</Button>
          </form>
          {message && <EmptyState title="검색 안내" description={message} />}
          <div className="grid gap-4 md:grid-cols-2">
            {books.map((book) => (
              <Card key={book.id}>
                <div className="flex flex-wrap items-center gap-2">
                  <Tag>{book.status}</Tag>
                  {book.publisher && <Tag>{book.publisher}</Tag>}
                </div>
                <Link className="mt-4 block text-lg font-normal" href={`/books/${book.id}`}>
                  {book.title}
                </Link>
                <p className="mt-2 text-sm text-[var(--color-charcoal)]">{book.authorsText}</p>
              </Card>
            ))}
          </div>
        </div>
      </Section>
    </main>
  );
}
