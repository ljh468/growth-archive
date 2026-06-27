"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section } from "@/components/ui/primitives";
import { apiDelete, apiGet, apiPut, type MeetingDetail, uploadImage } from "@/lib/api";

export function EditMeetingClient({ meetingId }: { meetingId: string }) {
  return (
    <AuthGate required="MEMBER">
      {() => <EditMeetingContent meetingId={meetingId} />}
    </AuthGate>
  );
}

function EditMeetingContent({ meetingId }: { meetingId: string }) {
  const router = useRouter();
  const [message, setMessage] = useState<string | null>(null);
  const [coverImageFile, setCoverImageFile] = useState<File | null>(null);
  const [existingCoverImageId, setExistingCoverImageId] = useState<number | null>(null);
  const [form, setForm] = useState({
    title: "",
    description: "",
    meetingAt: "",
    locationRegion: "",
    exactLocation: "",
    capacity: "",
    feeAmount: "0",
  });

  useEffect(() => {
    apiGet<MeetingDetail>(`/meetings/${meetingId}`).then((result) => {
      if (!result.success) {
        setMessage(result.error?.message ?? "모임을 불러오지 못했습니다.");
        return;
      }
      if (!result.data.canEdit) {
        setMessage("소소모임 생성자만 수정할 수 있습니다.");
      }
      setForm({
        title: result.data.title,
        description: result.data.description ?? "",
        meetingAt: result.data.meetingAt.slice(0, 16),
        locationRegion: result.data.locationRegion,
        exactLocation: result.data.exactLocation ?? "",
        capacity: result.data.capacity?.toString() ?? "",
        feeAmount: result.data.feeAmount.toString(),
      });
      setExistingCoverImageId(result.data.coverImageId);
    });
  }, [meetingId]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const thumbnailImageId = await uploadCoverImage();
    if (thumbnailImageId === undefined) {
      return;
    }
    const result = await apiPut<MeetingDetail>(`/meetings/${meetingId}`, toPayload(form, thumbnailImageId ?? existingCoverImageId));
    if (!result.success) {
      setMessage(result.error?.message ?? "소소모임을 수정하지 못했습니다.");
      return;
    }
    router.push(`/meetings/${result.data.id}`);
  }

  async function uploadCoverImage() {
    if (!coverImageFile) {
      return null;
    }
    const result = await uploadImage(coverImageFile, "MEETING");
    if (!result.success) {
      setMessage(result.error?.message ?? "모임 이미지를 업로드하지 못했습니다.");
      return undefined;
    }
    return result.data.imageId;
  }

  async function remove() {
    const result = await apiDelete<void>(`/meetings/${meetingId}`);
    if (!result.success) {
      setMessage(result.error?.message ?? "소소모임을 삭제하지 못했습니다.");
      return;
    }
    router.push("/meetings");
  }

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <PageHeader eyebrow="Small Meeting" title="소소모임 수정" description="소소모임 생성자만 내용을 수정할 수 있습니다." />
          {message && <EmptyState title="상태" description={message} />}
          <Card>
            <form className="grid gap-3" onSubmit={submit}>
              <Input label="모임명" onChange={(value) => setForm((current) => ({ ...current, title: value }))} required value={form.title} />
              <label className="grid gap-2 text-sm">
                설명
                <textarea className="min-h-36 border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3" onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))} value={form.description} />
              </label>
              <Input label="일시" onChange={(value) => setForm((current) => ({ ...current, meetingAt: value }))} required type="datetime-local" value={form.meetingAt} />
              <Input label="지역 수준 장소" onChange={(value) => setForm((current) => ({ ...current, locationRegion: value }))} required value={form.locationRegion} />
              <Input label="정확한 장소" onChange={(value) => setForm((current) => ({ ...current, exactLocation: value }))} value={form.exactLocation} />
              <Input label="정원" min="0" onChange={(value) => setForm((current) => ({ ...current, capacity: value }))} type="number" value={form.capacity} />
              <Input label="비용" min="0" onChange={(value) => setForm((current) => ({ ...current, feeAmount: value }))} type="number" value={form.feeAmount} />
              <label className="grid gap-2 text-sm">
                커버 이미지
                <input accept="image/jpeg,image/png,image/webp" onChange={(event) => setCoverImageFile(event.target.files?.[0] ?? null)} type="file" />
              </label>
              <div className="flex flex-wrap gap-3">
                <Button type="submit">수정 저장</Button>
                <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-normal" onClick={remove} type="button">
                  삭제
                </button>
              </div>
            </form>
          </Card>
        </div>
      </Section>
    </main>
  );
}

function Input({ label, onChange, required, type = "text", value, min }: { label: string; onChange: (value: string) => void; required?: boolean; type?: string; value: string; min?: string }) {
  return (
    <label className="grid gap-2 text-sm">
      {label}
      <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" min={min} onChange={(event) => onChange(event.target.value)} required={required} type={type} value={value} />
    </label>
  );
}

function toPayload(form: { title: string; description: string; meetingAt: string; locationRegion: string; exactLocation: string; capacity: string; feeAmount: string }, thumbnailImageId: number | null) {
  return {
    title: form.title,
    description: form.description || null,
    meetingAt: `${form.meetingAt}:00+09:00`,
    locationRegion: form.locationRegion,
    exactLocation: form.exactLocation || null,
    capacity: form.capacity ? Number(form.capacity) : null,
    feeAmount: form.feeAmount ? Number(form.feeAmount) : 0,
    thumbnailImageId,
  };
}
