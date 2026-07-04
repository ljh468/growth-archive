"use client";

import { type Dispatch, type FormEvent, type SetStateAction, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";
import { apiDelete, apiGet, apiPost, apiPut, type AdminInterestTag } from "@/lib/api";

export default function AdminInterestTagsPage() {
  return (
    <AuthGate required="ADMIN">
      {() => <AdminInterestTagsContent />}
    </AuthGate>
  );
}

function AdminInterestTagsContent() {
  const [tags, setTags] = useState<AdminInterestTag[]>([]);
  const [selected, setSelected] = useState<AdminInterestTag | null>(null);
  const [form, setForm] = useState({ name: "", slug: "", displayOrder: "100", active: true });
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    const result = await apiGet<AdminInterestTag[]>("/admin/interest-tags");
    if (!result.success) {
      setMessage(result.error?.message ?? "관심 태그를 불러오지 못했습니다.");
      return;
    }
    setTags(result.data);
  }

  function choose(tag: AdminInterestTag) {
    if (selected?.id === tag.id) {
      reset();
      return;
    }
    setSelected(tag);
    setForm({ name: tag.name, slug: tag.slug, displayOrder: String(tag.displayOrder), active: tag.active });
  }

  function reset() {
    setSelected(null);
    setForm({ name: "", slug: "", displayOrder: "100", active: true });
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    const payload = {
      name: form.name,
      slug: form.slug,
      displayOrder: Number(form.displayOrder),
      active: form.active,
    };
    const result = selected
      ? await apiPut<AdminInterestTag>(`/admin/interest-tags/${selected.id}`, payload)
      : await apiPost<AdminInterestTag>("/admin/interest-tags", payload);
    setMessage(result.success ? "관심 태그가 저장되었습니다." : result.error?.message ?? "관심 태그를 저장하지 못했습니다.");
    if (result.success) {
      reset();
      await load();
    }
  }

  async function deactivate(tagId: number) {
    const result = await apiDelete<void>(`/admin/interest-tags/${tagId}`);
    setMessage(result.success ? "관심 태그를 비활성화했습니다." : result.error?.message ?? "관심 태그를 비활성화하지 못했습니다.");
    if (result.success) {
      await load();
    }
  }

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <PageHeader eyebrow="Admin" title="관심 태그 관리" description="회원은 운영진이 등록한 활성 태그 중에서만 선택할 수 있습니다." />
          {message && <EmptyState title="상태" description={message} />}
          <div className="grid gap-5">
            <div className="grid gap-3">
              {tags.map((tag) => (
                <div
                  className={[
                    "grid gap-3",
                    selected?.id === tag.id ? "lg:grid-cols-[minmax(0,1fr)_360px] lg:items-start" : "",
                  ].join(" ")}
                  key={tag.id}
                >
                  <div
                    className={[
                      "flex flex-wrap items-center justify-between gap-3 border bg-[var(--color-warm-white)] p-4 transition",
                      selected?.id === tag.id ? "border-[var(--color-deep-green)] shadow-[var(--shadow-soft)]" : "border-[var(--color-line)]",
                    ].join(" ")}
                  >
                    <button className="text-left" onClick={() => choose(tag)} type="button">
                      <div className="flex flex-wrap gap-2">
                        <Tag>{tag.active ? "ACTIVE" : "HIDDEN"}</Tag>
                        <Tag>#{tag.displayOrder}</Tag>
                      </div>
                      <p className="mt-3 font-normal">{tag.name}</p>
                      <p className="mt-1 text-sm text-[var(--color-charcoal)]">{tag.slug}</p>
                    </button>
                    {tag.active && (
                      <button className="inline-flex min-h-10 items-center border border-[var(--color-line)] px-3 text-sm font-normal" onClick={() => deactivate(tag.id)} type="button">
                        비활성화
                      </button>
                    )}
                  </div>
                  {selected?.id === tag.id && <InterestTagEditor form={form} reset={reset} selected={selected} setForm={setForm} submit={submit} />}
                </div>
              ))}
            </div>
          </div>
        </div>
      </Section>
    </main>
  );
}

function InterestTagEditor({
  form,
  reset,
  selected,
  setForm,
  submit,
}: {
  form: { name: string; slug: string; displayOrder: string; active: boolean };
  reset: () => void;
  selected: AdminInterestTag | null;
  setForm: Dispatch<SetStateAction<{ name: string; slug: string; displayOrder: string; active: boolean }>>;
  submit: (event: FormEvent) => Promise<void>;
}) {
  return (
    <Card>
      <form className="grid gap-3" onSubmit={submit}>
        <h2 className="text-lg font-normal">{selected ? "태그 수정" : "태그 추가"}</h2>
        <Input label="태그명" onChange={(value) => setForm((current) => ({ ...current, name: value }))} value={form.name} />
        <Input label="Slug" onChange={(value) => setForm((current) => ({ ...current, slug: value }))} value={form.slug} />
        <Input label="노출 순서" onChange={(value) => setForm((current) => ({ ...current, displayOrder: value }))} type="number" value={form.displayOrder} />
        <label className="flex items-center gap-2 text-sm">
          <input checked={form.active} onChange={(event) => setForm((current) => ({ ...current, active: event.target.checked }))} type="checkbox" />
          활성 태그
        </label>
        <div className="flex flex-wrap gap-3">
          <Button type="submit">{selected ? "수정" : "추가"}</Button>
          <button className="inline-flex min-h-11 items-center justify-center border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 py-2 text-sm font-normal" onClick={reset} type="button">
            새 태그
          </button>
        </div>
      </form>
    </Card>
  );
}

function Input({ label, onChange, value, type = "text" }: { label: string; onChange: (value: string) => void; value: string; type?: string }) {
  return (
    <label className="grid gap-2 text-sm">
      {label}
      <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => onChange(event.target.value)} type={type} value={value} />
    </label>
  );
}
