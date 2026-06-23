import { ProtectedPlaceholderPage } from "@/components/ui/ProtectedPlaceholderPage";

export default function AdminReviewsPage() {
  return (
    <ProtectedPlaceholderPage
      required="ADMIN"
      eyebrow="Admin"
      title="모임 후기 관리"
      description="후기 본문을 직접 수정하지 않고 공개 상태를 숨김/삭제로 관리하는 관리자 화면입니다."
    />
  );
}
