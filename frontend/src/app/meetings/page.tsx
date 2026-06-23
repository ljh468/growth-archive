import { PlaceholderPage } from "@/components/ui/PlaceholderPage";

export default function MeetingsPage() {
  return (
    <PlaceholderPage
      eyebrow="Meetings"
      title="모임"
      description="정기 모임과 소소모임을 공개 미리보기로 탐색하고 멤버가 참석할 수 있는 화면입니다."
      primaryHref="/meetings/new"
      primaryLabel="소소모임 만들기"
    />
  );
}
