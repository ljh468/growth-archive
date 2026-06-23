import { ProtectedPlaceholderPage } from "@/components/ui/ProtectedPlaceholderPage";

export default function MyActionPlansPage() {
  return (
    <ProtectedPlaceholderPage
      required="MEMBER"
      eyebrow="Monthly Action Plan"
      title="월간 액션플랜"
      description="멤버별 월 1개 액션플랜을 작성하고 참여 상태에 반영하는 화면입니다."
    />
  );
}
