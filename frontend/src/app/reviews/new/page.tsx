"use client";

import { FormEvent, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { MobileBackButton } from "@/components/MobileBackButton";
import { Button, Card, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiGet, apiPost, type MeetingReviewDetail, type MeetingSummary, type UploadedImage, uploadImage } from "@/lib/api";
import { isHeicFile } from "@/lib/heicImage";

type SelectedReviewImage = {
  id: string;
  file: File;
  status: "pending" | "uploading" | "uploaded" | "failed";
  uploadedImage: UploadedImage | null;
  message: string | null;
};

export default function NewReviewPage() {
  return (
    <AuthGate required="MEMBER">
      {() => <NewReviewContent />}
    </AuthGate>
  );
}

function NewReviewContent() {
  const [meetings, setMeetings] = useState<MeetingSummary[]>([]);
  const [selectedImages, setSelectedImages] = useState<SelectedReviewImage[]>([]);
  const [message, setMessage] = useState<string | null>(null);
  const [savedReviewId, setSavedReviewId] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState({ meetingId: "", title: "", content: "" });

  useEffect(() => {
    apiGet<MeetingSummary[]>("/meetings?page=0&size=50").then((result) => {
      if (result.success) {
        setMeetings(result.data.filter((meeting) => meeting.status === "SCHEDULED" || meeting.status === "HELD"));
      }
    });
  }, []);

  function selectImages(fileList: FileList | null) {
    const nextFiles = Array.from(fileList ?? []);
    if (nextFiles.length > 10) {
      setMessage("모임 후기 사진은 최대 10장까지 가능합니다.");
      return;
    }
    setMessage(null);
    setSavedReviewId(null);
    setSelectedImages(nextFiles.map((file, index) => ({
      id: `${file.name}-${file.size}-${file.lastModified}-${index}`,
      file,
      status: "pending",
      uploadedImage: null,
      message: null,
    })));
  }

  async function uploadImages() {
    if (selectedImages.length === 0) {
      return { uploadedImages: [] as UploadedImage[], failedCount: 0 };
    }
    let failedCount = 0;
    const uploadedImages: Array<UploadedImage | null> = Array(selectedImages.length).fill(null);
    let cursor = 0;

    async function worker() {
      while (cursor < selectedImages.length) {
        const imageIndex = cursor;
        const image = selectedImages[cursor];
        cursor += 1;
        if (!image || image.status === "uploaded") {
          if (image?.uploadedImage) {
            uploadedImages[imageIndex] = image.uploadedImage;
          }
          continue;
        }

        setSelectedImages((current) => updateImageStatus(current, image.id, { status: "uploading", message: "압축 및 업로드 중" }));
        const result = await uploadImage(image.file, "REVIEW");
        if (result.success) {
          const uploadedImage = { imageId: result.data.imageId, imageUrl: result.data.url };
          uploadedImages[imageIndex] = uploadedImage;
          setSelectedImages((current) => updateImageStatus(current, image.id, { status: "uploaded", uploadedImage, message: "업로드 완료" }));
          continue;
        }

        failedCount += 1;
        setSelectedImages((current) => updateImageStatus(current, image.id, {
          status: "failed",
          message: result.error?.message ?? "업로드 실패",
        }));
      }
    }

    const workerCount = selectedImages.some((image) => isHeicFile(image.file))
      ? 1
      : Math.min(2, selectedImages.length);
    await Promise.all(Array.from({ length: workerCount }, () => worker()));
    return { uploadedImages: uploadedImages.filter((image): image is UploadedImage => image !== null), failedCount };
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (submitting) {
      return;
    }
    setSubmitting(true);
    setMessage(null);
    setSavedReviewId(null);
    const { uploadedImages, failedCount } = await uploadImages();
    const result = await apiPost<MeetingReviewDetail>("/reviews", {
      meetingId: Number(form.meetingId),
      title: form.title,
      content: form.content,
      imageIds: uploadedImages.map((image) => image.imageId),
    });
    if (!result.success) {
      setMessage(result.error?.message ?? "후기를 저장하지 못했습니다.");
      setSubmitting(false);
      return;
    }
    if (failedCount > 0) {
      setSavedReviewId(result.data.id);
      setMessage(`후기는 저장되었습니다. 업로드하지 못한 사진 ${failedCount}장은 포함되지 않았습니다. 상세 화면의 수정에서 다시 추가할 수 있습니다.`);
      setSubmitting(false);
      return;
    }
    window.location.href = `/reviews/${result.data.id}`;
  }

  return (
    <main>
      <Section>
        <div className="grid gap-5 sm:gap-8">
          <MobileBackButton fallbackHref="/reviews" />
          <PageHeader eyebrow="Meeting Review" title="모임 후기 작성" description="참석 버튼 여부와 무관하게 성장하는 사람들은 후기를 작성할 수 있습니다." />
          <p className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.78)] px-4 py-3 text-sm leading-6 text-[var(--color-muted)]">
            업로드한 사진은 공개 모임 후기에 노출될 수 있어요. 함께 나온 사람들에게 공개 가능 여부를 확인해주세요.
          </p>
          <Card>
            <form className="grid gap-3 sm:gap-4" onSubmit={submit}>
              <label className="grid gap-2 text-sm">
                연결 모임
                <select className="min-h-10 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setForm((current) => ({ ...current, meetingId: event.target.value }))} required value={form.meetingId}>
                  <option value="">모임 선택</option>
                  {meetings.map((meeting) => (
                    <option key={meeting.id} value={meeting.id}>{meeting.title}</option>
                  ))}
                </select>
              </label>
              <label className="grid gap-2 text-sm">
                제목
                <input className="min-h-10 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" maxLength={100} onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} required value={form.title} />
              </label>
              <label className="grid gap-2 text-sm">
                내용
                <textarea className="min-h-40 border border-[var(--color-line)] bg-[var(--color-warm-white)] p-3 sm:min-h-56" maxLength={5000} onChange={(event) => setForm((current) => ({ ...current, content: event.target.value }))} required value={form.content} />
              </label>
              <label className="grid gap-2 text-sm">
                사진
                <span className="inline-flex min-h-10 w-fit cursor-pointer items-center rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3 text-xs text-[var(--color-charcoal)]">
                  {selectedImages.length > 0 ? `${selectedImages.length}장 선택됨` : "사진 선택"}
                </span>
                <input accept="image/jpeg,image/png,image/webp,image/heic,image/heif,.heic,.heif" className="sr-only" multiple onChange={(event) => selectImages(event.target.files)} type="file" />
              </label>
              {selectedImages.length > 0 && (
                <div className="grid gap-2">
                  {selectedImages.map((image) => (
                    <div className="grid gap-1 rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[rgba(255,254,250,0.72)] px-3 py-2 text-xs sm:grid-cols-[minmax(0,1fr)_auto] sm:gap-3" key={image.id}>
                      <span className="min-w-0 truncate text-[var(--color-charcoal)]">{image.file.name}</span>
                      <span className={`${image.status === "failed" ? "text-[var(--color-bronze)]" : "text-[var(--color-muted)]"} leading-5 sm:text-right`}>
                        {image.message ?? statusLabel(image.status)}
                      </span>
                    </div>
                  ))}
                </div>
              )}
              {selectedImages.some((image) => image.status === "uploaded") && <Tag>{selectedImages.filter((image) => image.status === "uploaded").length}장 업로드됨</Tag>}
              {message && <p className="text-sm leading-6 text-[var(--color-muted)]">{message}</p>}
              {savedReviewId && <Button href={`/reviews/${savedReviewId}`} variant="secondary">저장된 후기 보기</Button>}
              {!savedReviewId && <Button type="submit">{submitting ? "저장 중" : "후기 저장"}</Button>}
            </form>
          </Card>
        </div>
      </Section>
    </main>
  );
}

function updateImageStatus(images: SelectedReviewImage[], id: string, patch: Partial<SelectedReviewImage>) {
  return images.map((image) => image.id === id ? { ...image, ...patch } : image);
}

function statusLabel(status: SelectedReviewImage["status"]) {
  if (status === "uploading") {
    return "압축 및 업로드 중";
  }
  if (status === "uploaded") {
    return "업로드 완료";
  }
  if (status === "failed") {
    return "업로드 실패";
  }
  return "대기";
}
