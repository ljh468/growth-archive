import { ProtectedPlaceholderPage } from "@/components/ui/ProtectedPlaceholderPage";

export default function MyReflectionsPage() {
  return (
    <ProtectedPlaceholderPage
      required="MEMBER"
      eyebrow="Monthly Reflection"
      title="월간 회고"
      description="참여 상태에는 포함하지 않는 선택 회고를 월별로 작성하는 화면입니다."
    />
  );
}
