import { PlaceholderPage } from "@/components/ui/PlaceholderPage";

type MeetingDetailPageProps = {
  params: Promise<{ meetingId: string }>;
};

export default async function MeetingDetailPage({ params }: MeetingDetailPageProps) {
  const { meetingId } = await params;

  return (
    <PlaceholderPage
      eyebrow="Meeting Detail"
      title={`모임 상세 #${meetingId}`}
      description="모임 정보, 참석 상태, 후기 작성 진입점을 연결할 상세 화면입니다."
    />
  );
}
