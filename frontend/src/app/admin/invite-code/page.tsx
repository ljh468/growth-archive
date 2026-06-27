"use client";

import { FormEvent, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { Button, Card, EmptyState, PageHeader, Section } from "@/components/ui/primitives";
import { apiGet, apiPut, type AdminInviteCode } from "@/lib/api";

export default function AdminInviteCodePage() {
  return (
    <AuthGate required="ADMIN">
      {() => <AdminInviteCodeContent />}
    </AuthGate>
  );
}

function AdminInviteCodeContent() {
  const [codePreview, setCodePreview] = useState<string | null>(null);
  const [code, setCode] = useState("");
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    const result = await apiGet<AdminInviteCode>("/admin/invite-code");
    if (!result.success) {
      setMessage(result.error?.message ?? "초대코드를 불러오지 못했습니다.");
      return;
    }
    setCodePreview(result.data.codePreview);
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    const result = await apiPut<void>("/admin/invite-code", { code });
    setMessage(result.success ? "초대코드가 변경되었습니다." : result.error?.message ?? "초대코드를 변경하지 못했습니다.");
    if (result.success) {
      setCode("");
      await load();
    }
  }

  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <PageHeader eyebrow="Admin" title="초대코드 관리" description="활성 초대코드는 한 번에 1개만 유지되며, 변경 즉시 이전 코드는 무효화됩니다." />
          {message && <EmptyState title="상태" description={message} />}
          <Card>
            <p className="text-sm text-[var(--color-charcoal)]">현재 활성 코드 미리보기</p>
            <p className="mt-3 text-2xl font-normal">{codePreview ?? "없음"}</p>
          </Card>
          <Card>
            <form className="grid gap-3" onSubmit={submit}>
              <label className="grid gap-2 text-sm">
                새 초대코드
                <input className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3" onChange={(event) => setCode(event.target.value)} value={code} />
              </label>
              <Button type="submit">초대코드 변경</Button>
            </form>
          </Card>
        </div>
      </Section>
    </main>
  );
}
