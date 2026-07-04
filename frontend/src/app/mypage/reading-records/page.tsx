"use client";

import Image from "next/image";
import { FormEvent, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, ConfirmDialog, EmptyState, PageHeader, Section, SkeletonBlock } from "@/components/ui/primitives";
import { apiDelete, apiGet, apiPut, type CurrentUser, type ReadingRecord } from "@/lib/api";

type EditForm = {
  rating: string;
  oneLineReview: string;
  blogUrl: string;
};

export default function MyReadingRecordsPage() {
  return (
    <AuthGate required="MEMBER">
      {(user) => <MyReadingRecordsContent user={user} />}
    </AuthGate>
  );
}

function MyReadingRecordsContent({ user }: { user: CurrentUser }) {
  const currentMonth = formatMonthKst(new Date().toISOString());
  const [selectedMonth, setSelectedMonth] = useState(currentMonth);
  const [records, setRecords] = useState<ReadingRecord[]>([]);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<EditForm>({ rating: "", oneLineReview: "", blogUrl: "" });
  const [message, setMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState(Boolean(user.memberId));
  const [submitting, setSubmitting] = useState(false);
  const [pendingDeleteId, setPendingDeleteId] = useState<number | null>(null);

  const monthlyRecords = records;
  const canMoveNext = selectedMonth < currentMonth;

  useEffect(() => {
    if (!user.memberId) {
      return;
    }
    loadReadingRecords(user.memberId, selectedMonth)
      .then(setRecords)
      .catch(() => setMessage("독서기록을 불러오지 못했습니다."))
      .finally(() => setLoading(false));
  }, [selectedMonth, user.memberId]);

  function startEdit(record: ReadingRecord) {
    setEditingId(record.id);
    setForm({
      rating: String(record.rating ?? ""),
      oneLineReview: record.oneLineReview,
      blogUrl: record.blogUrl,
    });
    setMessage(null);
  }

  function moveMonth(delta: -1 | 1) {
    setSelectedMonth((month) => {
      const nextMonth = addMonths(month, delta);
      return nextMonth > currentMonth ? currentMonth : nextMonth;
    });
    setLoading(true);
    setEditingId(null);
    setMessage(null);
  }

  async function save(event: FormEvent, record: ReadingRecord) {
    event.preventDefault();
    if (submitting) {
      return;
    }
    setSubmitting(true);
    setMessage(null);
    const result = await apiPut<ReadingRecord>(`/reading-records/${record.id}`, {
      bookId: record.bookId,
      rating: Number(form.rating),
      oneLineReview: form.oneLineReview,
      blogUrl: form.blogUrl,
      imageId: record.imageId,
    });
    if (!result.success) {
      setMessage(result.error?.message ?? "독서기록을 수정하지 못했습니다.");
      setSubmitting(false);
      return;
    }
    setRecords((current) => current.map((item) => (item.id === record.id ? result.data : item)));
    setEditingId(null);
    setSubmitting(false);
    setMessage("독서기록이 수정되었습니다.");
  }

  async function remove(recordId: number) {
    setMessage(null);
    const result = await apiDelete<void>(`/reading-records/${recordId}`);
    if (!result.success) {
      setMessage(result.error?.message ?? "독서기록을 삭제하지 못했습니다.");
      return;
    }
    setRecords((current) => current.filter((record) => record.id !== recordId));
    setPendingDeleteId(null);
    setMessage("독서기록이 삭제되었습니다.");
  }

  return (
    <main>
      <Section>
        <div className="grid gap-5 sm:gap-8">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <PageHeader eyebrow="My Reading" title="내 독서기록" description="월별로 내가 남긴 독서기록을 확인하고 필요한 내용만 수정합니다." />
            <Button href="/reading-records/new">독서기록 추가</Button>
          </div>

          {message && <p className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.78)] px-4 py-3 text-sm leading-6 text-[var(--color-charcoal)]">{message}</p>}
          <div className="grid gap-3 sm:gap-4">
            <div className="rounded-[var(--radius-card)] border border-[rgba(229,222,209,0.82)] bg-[rgba(255,254,250,0.72)] px-3 py-2.5 shadow-[var(--shadow-soft)] sm:px-4">
              <div className="grid grid-cols-[2rem_1fr_2rem] items-center gap-2">
                <button
                  aria-label="이전달 보기"
                  className="grid size-8 place-items-center rounded-full border border-[rgba(229,222,209,0.9)] bg-[rgba(255,253,248,0.7)] text-[var(--color-charcoal)] transition hover:border-[rgba(31,77,58,0.3)] hover:bg-[rgba(47,90,67,0.055)] hover:text-[var(--color-deep-green)]"
                  onClick={() => moveMonth(-1)}
                  type="button"
                >
                  <span aria-hidden="true" className="block size-2.5 rotate-45 border-b border-l border-current" />
                </button>
                <div className="min-w-0 text-center">
                  <p className="text-[10px] leading-4 text-[var(--color-bronze)] sm:text-[11px]">월별 기록</p>
                  <p className="text-[13px] leading-5 text-[var(--color-charcoal)] sm:text-sm">{formatMonthLabel(selectedMonth)} · {monthlyRecords.length}개</p>
                </div>
                <button
                  aria-label="다음달 보기"
                  className="grid size-8 place-items-center rounded-full border border-[rgba(229,222,209,0.9)] bg-[rgba(255,253,248,0.7)] text-[var(--color-charcoal)] transition hover:border-[rgba(31,77,58,0.3)] hover:bg-[rgba(47,90,67,0.055)] hover:text-[var(--color-deep-green)] disabled:cursor-not-allowed disabled:opacity-30"
                  disabled={!canMoveNext}
                  onClick={() => moveMonth(1)}
                  type="button"
                >
                  <span aria-hidden="true" className="block size-2.5 -rotate-45 border-b border-r border-current" />
                </button>
              </div>
            </div>

            {loading && <ReadingRecordListSkeleton />}
            {!loading && (
              <>
              {monthlyRecords.length === 0 ? (
                <EmptyState title={`${formatMonthLabel(selectedMonth)} 독서기록이 없습니다`} description="다른 달을 선택하거나 새 독서기록을 남겨보세요." />
              ) : (
                monthlyRecords.map((record) => (
                  <ReadingRecordCard
                    editing={editingId === record.id}
                    form={form}
                    key={record.id}
                    onCancel={() => setEditingId(null)}
                    onDelete={() => setPendingDeleteId(record.id)}
                    onEdit={() => startEdit(record)}
                    onFormChange={setForm}
                    onSave={(event) => save(event, record)}
                    record={record}
                    submitting={submitting}
                  />
                ))
              )}
              </>
            )}
          </div>
        </div>
      </Section>
      <ConfirmDialog
        description="삭제한 독서기록은 내 기록장과 공개 라이브러리에서 더 이상 보이지 않습니다."
        onCancel={() => setPendingDeleteId(null)}
        onConfirm={() => {
          if (pendingDeleteId !== null) {
            void remove(pendingDeleteId);
          }
        }}
        open={pendingDeleteId !== null}
        title="이 독서기록을 삭제할까요?"
      />
    </main>
  );
}

async function loadReadingRecords(memberId: number, month: string) {
  const allRecords: ReadingRecord[] = [];
  for (let page = 0; page < 10; page++) {
    const result = await apiGet<ReadingRecord[]>(`/reading-records?memberId=${memberId}&month=${month}&page=${page}&size=50`);
    if (!result.success) {
      throw new Error(result.error?.message ?? "독서기록을 불러오지 못했습니다.");
    }
    allRecords.push(...result.data);
    if (result.data.length < 50) {
      break;
    }
  }
  return allRecords;
}

function ReadingRecordCard({
  editing,
  form,
  onCancel,
  onDelete,
  onEdit,
  onFormChange,
  onSave,
  record,
  submitting,
}: {
  editing: boolean;
  form: EditForm;
  onCancel: () => void;
  onDelete: () => void;
  onEdit: () => void;
  onFormChange: (form: EditForm) => void;
  onSave: (event: FormEvent) => void;
  record: ReadingRecord;
  submitting: boolean;
}) {
  return (
    <article className="grid grid-cols-[72px_1fr] gap-4 border-b border-[var(--color-line)] pb-5 sm:grid-cols-[84px_1fr] sm:pb-6">
      <a
        aria-label={`${record.bookTitle} 상세 보기`}
        className="block aspect-[3/4] w-[72px] self-center overflow-hidden rounded-[var(--radius-card)] bg-[var(--color-line)] shadow-[0_12px_28px_rgba(63,47,34,0.08)] sm:w-[84px]"
        href={`/books/${record.bookId}`}
      >
        <Image
          alt=""
          className="h-full w-full object-cover object-center photo-muted"
          height={112}
          src={record.recordImageUrl || record.bookThumbnailUrl || "/images/reading-books.jpg"}
          unoptimized
          width={84}
        />
      </a>

      <div className="grid min-w-0 content-start gap-2">
        <div className="flex flex-wrap items-center gap-2 text-xs text-[var(--color-muted)]">
          <time dateTime={record.recordedAt}>{formatDate(record.recordedAt)}</time>
          <span aria-hidden="true">·</span>
          <span>{record.rating ? `평점 ${record.rating}/5` : "평점 없음"}</span>
        </div>
        <a className="line-clamp-2 font-display text-base font-normal leading-snug text-[var(--color-ink)] hover:text-[var(--color-deep-green)] sm:text-lg" href={`/books/${record.bookId}`}>
          {record.bookTitle}
        </a>
        <p className="line-clamp-1 text-xs text-[var(--color-muted)]">{record.authorsText}</p>

        {editing ? (
          <form className="mt-3 grid gap-3" onSubmit={onSave}>
            <div className="grid gap-3 sm:grid-cols-[120px_1fr]">
              <label className="grid gap-1.5 text-xs text-[var(--color-charcoal)]">
                평점
                <select className="min-h-10 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 text-sm" onChange={(event) => onFormChange({ ...form, rating: event.target.value })} required value={form.rating}>
                  <option disabled value="">평점 선택</option>
                  <option value="1">1</option>
                  <option value="2">2</option>
                  <option value="3">3</option>
                  <option value="4">4</option>
                  <option value="5">5</option>
                </select>
              </label>
              <label className="grid gap-1.5 text-xs text-[var(--color-charcoal)]">
                블로그 원문
                <input className="min-h-10 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 text-sm" onChange={(event) => onFormChange({ ...form, blogUrl: event.target.value })} required value={form.blogUrl} />
              </label>
            </div>
            <label className="grid gap-1.5 text-xs text-[var(--color-charcoal)]">
              한줄평
              <input className="min-h-10 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 text-sm" maxLength={300} onChange={(event) => onFormChange({ ...form, oneLineReview: event.target.value })} required value={form.oneLineReview} />
            </label>
            <div className="flex flex-wrap gap-2">
              <button className="inline-flex min-h-9 items-center justify-center rounded-[var(--radius-card)] bg-[var(--color-deep-green)] px-3 text-xs text-[var(--color-warm-white)] transition hover:bg-[var(--color-wood-brown)]" type="submit">
                {submitting ? "저장 중" : "저장"}
              </button>
              <button className="inline-flex min-h-9 items-center justify-center rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 text-xs text-[var(--color-charcoal)]" onClick={onCancel} type="button">
                취소
              </button>
            </div>
          </form>
        ) : (
          <>
            <p className="line-clamp-2 text-sm leading-6 text-[var(--color-charcoal)]">{record.oneLineReview}</p>
            <div className="mt-1 flex flex-wrap items-center justify-between gap-3">
              <a className="inline-flex w-fit border-b border-[#0b3d2e] text-xs text-[#0b3d2e] hover:text-[var(--color-deep-green)]" href={record.blogUrl} rel="noreferrer" target="_blank">
                블로그 원문
              </a>
              <div className="ml-auto flex items-center gap-3">
                <button className="archive-record-button" onClick={onEdit} type="button">
                  수정
                </button>
                <button className="archive-record-button archive-record-button--danger" onClick={onDelete} type="button">
                  삭제
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </article>
  );
}

function formatMonthKst(value: string) {
  return new Intl.DateTimeFormat("en-CA", {
    year: "numeric",
    month: "2-digit",
    timeZone: "Asia/Seoul",
  }).format(new Date(value));
}

function formatMonthLabel(value: string) {
  const [year, month] = value.split("-");
  return `${year}년 ${Number(month)}월`;
}

function addMonths(value: string, delta: number) {
  const [year, month] = value.split("-").map(Number);
  const date = new Date(Date.UTC(year, month - 1 + delta, 1));
  return new Intl.DateTimeFormat("en-CA", {
    year: "numeric",
    month: "2-digit",
    timeZone: "UTC",
  }).format(date);
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeZone: "Asia/Seoul",
  }).format(new Date(value));
}

function ReadingRecordListSkeleton() {
  return (
    <div className="grid gap-3 sm:gap-4" aria-label="내 독서기록 로딩 중">
      {Array.from({ length: 3 }).map((_, index) => (
        <article className="grid grid-cols-[72px_1fr] gap-4 border-b border-[var(--color-line)] pb-5 sm:grid-cols-[84px_1fr] sm:pb-6" key={index}>
          <SkeletonBlock className="aspect-[3/4] w-[72px] sm:w-[84px]" />
          <div className="grid gap-3">
            <SkeletonBlock className="h-5 w-3/4" />
            <SkeletonBlock className="h-4 w-1/2" />
            <SkeletonBlock className="h-4 w-full" />
            <SkeletonBlock className="h-4 w-2/3" />
          </div>
        </article>
      ))}
    </div>
  );
}
