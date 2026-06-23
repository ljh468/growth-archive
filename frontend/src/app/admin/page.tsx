import { ProtectedPlaceholderPage } from "@/components/ui/ProtectedPlaceholderPage";

export default function AdminPage() {
  return (
    <ProtectedPlaceholderPage
      required="ADMIN"
      eyebrow="Admin"
      title="운영 관리"
      description="활성 멤버이면서 ADMIN role인 사용자만 접근하는 운영 콘솔입니다."
    />
  );
}
