import { ProtectedPlaceholderPage } from "@/components/ui/ProtectedPlaceholderPage";

export default function MyMeetingsPage() {
  return (
    <ProtectedPlaceholderPage
      required="MEMBER"
      eyebrow="My Meetings"
      title="내 모임"
      description="내 참석 모임, 생성한 소소모임, 작성 가능한 후기를 확인하는 멤버 전용 화면입니다."
    />
  );
}
