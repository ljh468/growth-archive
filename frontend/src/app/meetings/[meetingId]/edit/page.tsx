import { ProtectedPlaceholderPage } from "@/components/ui/ProtectedPlaceholderPage";

type EditMeetingPageProps = {
  params: Promise<{ meetingId: string }>;
};

export default async function EditMeetingPage({ params }: EditMeetingPageProps) {
  const { meetingId } = await params;

  return (
    <ProtectedPlaceholderPage
      required="MEMBER"
      eyebrow="Small Meeting"
      title={`소소모임 수정 #${meetingId}`}
      description="소소모임 생성자만 수정할 수 있고 Admin은 숨김/삭제만 수행하는 멤버 전용 화면입니다."
    />
  );
}
