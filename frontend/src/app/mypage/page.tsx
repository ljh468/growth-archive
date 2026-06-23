import { ProtectedPlaceholderPage } from "@/components/ui/ProtectedPlaceholderPage";

export default function MyPage() {
  return (
    <ProtectedPlaceholderPage
      required="MEMBER"
      eyebrow="My Archive"
      title="마이페이지"
      description="월간 참여 상태, 내 독서기록, 액션플랜, 회고, 모임 활동을 확인하는 멤버 전용 화면입니다."
    />
  );
}
