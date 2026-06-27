"use client";

import { FormEvent, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiGet, apiPost, apiPostForm, type MeetingReviewDetail, type MeetingSummary, type UploadedImage } from "@/lib/api";

export default function NewReviewPage() {
  return (
    <AuthGate required="MEMBER">
      {() => <NewReviewContent />}
    </AuthGate>
  );
}

function NewReviewContent() {
  const [meetings, setMeetings] = useState<MeetingSummary[]>([]);
  const [uploadedImages, setUploadedImages] = useState<UploadedImage[]>([]);
  const [files, setFiles] = useState<FileList | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [form, setForm] = useState({ meetingId: "", title: "", content: "" });

  useEffect(() => {
    apiGet<MeetingSummary[]>("/meetings?page=0&size=50").then((result) => {
      if (result.success) {
        setMeetings(result.data.filter((meeting) => meeting.status === "SCHEDULED" || meeting.status === "HELD"));
      }
    });
  }, []);

  async function uploadImages() {
    if (!files || files.length === 0) {
      return uploadedImages;
    }
    if (files.length > 10) {
      setMessage("모임 후기 사진은 최대 10장까지 가능합니다.");
      return null;
    }
    const formData = new FormData();
    Array.from(files).forEach((file) => formData.append("files", file));
    const result = await apiPostForm<UploadedImage[]>("/uploads/review-images", formData);
    if (!result.success) {
      setMessage(result.error?.message ?? "사진 업로드에 실패했습니다.");
      return null;
    }
    setUploadedImages(result.data);
    return result.data;
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    const images = await uploadImages();
    if (!images) {
      return;
    }
    const result = await apiPost<MeetingReviewDetail>("/reviews", {
      meetingId: Number(form.meetingId),
      title: form.title,
      content: form.content,
      imageIds: images.map((image) => image.imageId),
    });
    if (!result.success) {
      setMessage(result.error?.message ?? "후기를 저장하지 못했습니다.");
      return;
    }
    window.location.href = `/reviews/${result.data.id}`;
  }

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <PageHeader eyebrow="Meeting Review" title="모임 후기 작성" description="참석 버튼 여부와 무관하게 성장하는 사람들은 후기를 작성할 수 있습니다." />
          <EmptyState title="사진 공개 안내" description="업로드한 사진은 공개 모임 후기에 노출될 수 있어요. 함께 나온 사람들에게 공개 가능 여부를 확인해주세요." />
          {message && <EmptyState title="상태" description={message} />}
          <Card>
            <form className="grid gap-3" onSubmit={submit}>
              <label className="grid gap-2 text-sm">
                연결 모임
                <select className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setForm((current) => ({ ...current, meetingId: event.target.value }))} required value={form.meetingId}>
                  <option value="">모임 선택</option>
                  {meetings.map((meeting) => (
                    <option key={meeting.id} value={meeting.id}>{meeting.title}</option>
                  ))}
                </select>
              </label>
              <label className="grid gap-2 text-sm">
                제목
                <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" maxLength={100} onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} required value={form.title} />
              </label>
              <label className="grid gap-2 text-sm">
                내용
                <textarea className="min-h-56 border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3" maxLength={5000} onChange={(event) => setForm((current) => ({ ...current, content: event.target.value }))} required value={form.content} />
              </label>
              <label className="grid gap-2 text-sm">
                사진
                <input accept="image/*" multiple onChange={(event) => setFiles(event.target.files)} type="file" />
              </label>
              {uploadedImages.length > 0 && <Tag>{uploadedImages.length}장 업로드됨</Tag>}
              <Button type="submit">후기 저장</Button>
            </form>
          </Card>
        </div>
      </Section>
    </main>
  );
}
