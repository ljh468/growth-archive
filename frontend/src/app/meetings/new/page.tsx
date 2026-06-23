import { ProtectedPlaceholderPage } from "@/components/ui/ProtectedPlaceholderPage";

export default function NewMeetingPage() {
  return (
    <ProtectedPlaceholderPage
      required="MEMBER"
      eyebrow="Small Meeting"
      title="소소모임 만들기"
      description="활성 멤버가 직접 소소모임을 생성하는 화면입니다."
    />
  );
}
