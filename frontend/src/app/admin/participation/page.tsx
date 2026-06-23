import { ProtectedPlaceholderPage } from "@/components/ui/ProtectedPlaceholderPage";

export default function AdminParticipationPage() {
  return (
    <ProtectedPlaceholderPage
      required="ADMIN"
      eyebrow="Admin"
      title="참여 현황 관리"
      description="월간 참여 완료 여부와 커피 서포트 대상자를 운영자가 확인하는 화면입니다."
    />
  );
}
