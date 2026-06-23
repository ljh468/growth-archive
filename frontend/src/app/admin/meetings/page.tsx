import { ProtectedPlaceholderPage } from "@/components/ui/ProtectedPlaceholderPage";

export default function AdminMeetingsPage() {
  return (
    <ProtectedPlaceholderPage
      required="ADMIN"
      eyebrow="Admin"
      title="모임 관리"
      description="정기 모임을 운영하고 소소모임은 숨김/삭제만 수행하는 관리자 화면입니다."
    />
  );
}
