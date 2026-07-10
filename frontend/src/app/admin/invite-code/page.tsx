"use client";

import { FormEvent, useEffect, useState } from "react";
import { AuthGate } from "@/components/AuthGate";
import { MobileBackButton } from "@/components/MobileBackButton";
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
  const [memberCodePreview, setMemberCodePreview] = useState<string | null>(null);
  const [adminCodePreview, setAdminCodePreview] = useState<string | null>(null);
  const [role, setRole] = useState<"MEMBER" | "ADMIN">("MEMBER");
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
    setMemberCodePreview(result.data.memberCodePreview);
    setAdminCodePreview(result.data.adminCodePreview);
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    const result = await apiPut<void>("/admin/invite-code", { role, code });
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
          <MobileBackButton fallbackHref="/admin" />
          <PageHeader eyebrow="Admin" title="초대코드 관리" description="일반 회원용 코드와 운영진 코드를 분리해 관리합니다." />
          {message && <EmptyState title="상태" description={message} />}
          <div className="grid gap-3 sm:grid-cols-2">
            <Card>
              <p className="text-sm text-[var(--color-charcoal)]">일반 회원 초대코드</p>
              <p className="mt-3 text-2xl font-normal">{memberCodePreview ?? "없음"}</p>
            </Card>
            <Card>
              <p className="text-sm text-[var(--color-charcoal)]">운영진 초대코드</p>
              <p className="mt-3 text-2xl font-normal">{adminCodePreview ?? "없음"}</p>
            </Card>
          </div>
          <Card>
            <form className="grid gap-3" onSubmit={submit}>
              <label className="grid gap-2 text-sm">
                권한
                <select
                  className="min-h-11 border border-[var(--color-line)] bg-[var(--color-warm-white)] px-3"
                  onChange={(event) => setRole(event.target.value as "MEMBER" | "ADMIN")}
                  value={role}
                >
                  <option value="MEMBER">일반 회원</option>
                  <option value="ADMIN">운영진</option>
                </select>
              </label>
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
