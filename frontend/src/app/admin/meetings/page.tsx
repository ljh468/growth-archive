"use client";

import { FormEvent, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiDelete, apiGet, apiPost, apiPut, type MeetingDetail, type MeetingSummary } from "@/lib/api";

export default function AdminMeetingsPage() {
  return (
    <AuthGate required="ADMIN">
      {() => <AdminMeetingsContent />}
    </AuthGate>
  );
}

function AdminMeetingsContent() {
  const [meetings, setMeetings] = useState<MeetingSummary[]>([]);
  const [selected, setSelected] = useState<MeetingDetail | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [form, setForm] = useState({
    title: "",
    description: "",
    meetingAt: "",
    locationRegion: "",
    exactLocation: "",
    capacity: "",
    feeAmount: "0",
    status: "SCHEDULED",
  });

  useEffect(() => {
    loadList();
  }, []);

  async function loadList() {
    const result = await apiGet<MeetingSummary[]>("/admin/meetings?page=0&size=100");
    if (!result.success) {
      setMessage(result.error?.message ?? "모임 목록을 불러오지 못했습니다.");
      return;
    }
    setMeetings(result.data);
  }

  async function choose(id: number) {
    const result = await apiGet<MeetingDetail>(`/admin/meetings/${id}`);
    if (!result.success) {
      setMessage(result.error?.message ?? "모임 상세를 불러오지 못했습니다.");
      return;
    }
    setSelected(result.data);
    setForm({
      title: result.data.title,
      description: result.data.description ?? "",
      meetingAt: result.data.meetingAt.slice(0, 16),
      locationRegion: result.data.locationRegion,
      exactLocation: result.data.exactLocation ?? "",
      capacity: result.data.capacity?.toString() ?? "",
      feeAmount: result.data.feeAmount.toString(),
      status: result.data.status,
    });
    setMessage(null);
  }

  async function save(event: FormEvent) {
    event.preventDefault();
    if (!selected) {
      return;
    }
    if (selected.meetingType === "SMALL") {
      setMessage("Admin은 소소모임 내용을 직접 수정할 수 없습니다.");
      return;
    }
    const result = await apiPut<MeetingDetail>(`/admin/meetings/${selected.id}`, toPayload(form));
    if (!result.success) {
      setMessage(result.error?.message ?? "정기모임을 수정하지 못했습니다.");
      return;
    }
    setSelected(result.data);
    setMessage("정기모임 운영 정보가 수정되었습니다.");
    loadList();
  }

  async function hide() {
    if (!selected) {
      return;
    }
    const result = await apiPost<void>(`/admin/meetings/${selected.id}/hide`);
    setMessage(result.success ? "모임을 숨김 처리했습니다." : result.error?.message ?? "숨김 처리에 실패했습니다.");
    loadList();
  }

  async function restore() {
    if (!selected) {
      return;
    }
    const result = await apiPost<void>(`/admin/meetings/${selected.id}/restore`);
    setMessage(result.success ? "모임을 복구했습니다." : result.error?.message ?? "복구에 실패했습니다.");
    loadList();
  }

  async function remove() {
    if (!selected) {
      return;
    }
    const result = await apiDelete<void>(`/admin/meetings/${selected.id}`);
    setMessage(result.success ? "모임을 삭제했습니다." : result.error?.message ?? "삭제에 실패했습니다.");
    setSelected(null);
    loadList();
  }

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <PageHeader eyebrow="Admin" title="모임 관리" description="정기모임 운영 정보를 수정하고, 소소모임은 숨김/삭제만 처리합니다." />
          {message && <EmptyState title="상태" description={message} />}
          <div className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_minmax(360px,0.8fr)]">
            <div className="grid gap-3">
              {meetings.map((meeting) => (
                <button className="border border-[var(--color-line)] bg-[var(--color-warm-white)] p-4 text-left" key={meeting.id} onClick={() => choose(meeting.id)} type="button">
                  <div className="flex flex-wrap gap-2">
                    <Tag>{meeting.meetingType}</Tag>
                    <Tag>{meeting.status}</Tag>
                  </div>
                  <p className="mt-3 font-semibold">{meeting.title}</p>
                  <p className="mt-1 text-sm text-[var(--color-charcoal)]">{formatDateTime(meeting.meetingAt)} · {meeting.locationRegion}</p>
                </button>
              ))}
            </div>
            <Card>
              {!selected && <p className="text-sm text-[var(--color-charcoal)]">수정하거나 관리할 모임을 선택하세요.</p>}
              {selected && (
                <form className="grid gap-3" onSubmit={save}>
                  <div className="flex flex-wrap gap-2">
                    <Tag>{selected.meetingType}</Tag>
                    <Tag>{selected.status}</Tag>
                  </div>
                  {selected.meetingType === "SMALL" && <EmptyState title="소소모임 관리" description="Admin은 소소모임 내용을 직접 수정하지 않고 숨김/삭제만 수행합니다." />}
                  <Input disabled={selected.meetingType === "SMALL"} label="모임명" onChange={(value) => setForm((current) => ({ ...current, title: value }))} value={form.title} />
                  <label className="grid gap-2 text-sm">
                    설명
                    <textarea className="min-h-32 border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3 disabled:bg-[var(--color-ivory)]" disabled={selected.meetingType === "SMALL"} onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))} value={form.description} />
                  </label>
                  <Input disabled={selected.meetingType === "SMALL"} label="일시" onChange={(value) => setForm((current) => ({ ...current, meetingAt: value }))} type="datetime-local" value={form.meetingAt} />
                  <Input disabled={selected.meetingType === "SMALL"} label="지역 수준 장소" onChange={(value) => setForm((current) => ({ ...current, locationRegion: value }))} value={form.locationRegion} />
                  <Input disabled={selected.meetingType === "SMALL"} label="정확한 장소" onChange={(value) => setForm((current) => ({ ...current, exactLocation: value }))} value={form.exactLocation} />
                  <Input disabled={selected.meetingType === "SMALL"} label="정원" min="0" onChange={(value) => setForm((current) => ({ ...current, capacity: value }))} type="number" value={form.capacity} />
                  <Input disabled={selected.meetingType === "SMALL"} label="비용" min="0" onChange={(value) => setForm((current) => ({ ...current, feeAmount: value }))} type="number" value={form.feeAmount} />
                  <label className="grid gap-2 text-sm">
                    상태
                    <select className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 disabled:bg-[var(--color-ivory)]" disabled={selected.meetingType === "SMALL"} onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))} value={form.status}>
                      <option value="SCHEDULED">SCHEDULED</option>
                      <option value="HELD">HELD</option>
                      <option value="CANCELED">CANCELED</option>
                      <option value="HIDDEN">HIDDEN</option>
                      <option value="DELETED">DELETED</option>
                    </select>
                  </label>
                  <div className="flex flex-wrap gap-3">
                    {selected.meetingType !== "SMALL" && <Button type="submit">정기모임 수정</Button>}
                    {selected.status === "HIDDEN" ? (
                      <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-semibold" onClick={restore} type="button">복구</button>
                    ) : (
                      <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-semibold" onClick={hide} type="button">숨김</button>
                    )}
                    <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-semibold" onClick={remove} type="button">삭제</button>
                  </div>
                </form>
              )}
            </Card>
          </div>
        </div>
      </Section>
    </main>
  );
}

function Input({ disabled, label, onChange, type = "text", value, min }: { disabled?: boolean; label: string; onChange: (value: string) => void; type?: string; value: string; min?: string }) {
  return (
    <label className="grid gap-2 text-sm">
      {label}
      <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 disabled:bg-[var(--color-ivory)]" disabled={disabled} min={min} onChange={(event) => onChange(event.target.value)} type={type} value={value} />
    </label>
  );
}

function toPayload(form: { title: string; description: string; meetingAt: string; locationRegion: string; exactLocation: string; capacity: string; feeAmount: string; status: string }) {
  return {
    title: form.title,
    description: form.description || null,
    meetingAt: `${form.meetingAt}:00+09:00`,
    locationRegion: form.locationRegion,
    exactLocation: form.exactLocation || null,
    capacity: form.capacity ? Number(form.capacity) : null,
    feeAmount: form.feeAmount ? Number(form.feeAmount) : 0,
    status: form.status,
  };
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeStyle: "short",
    timeZone: "Asia/Seoul",
  }).format(new Date(value));
}
