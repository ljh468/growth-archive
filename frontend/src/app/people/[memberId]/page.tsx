import { PlaceholderPage } from "@/components/ui/PlaceholderPage";

type PeopleDetailPageProps = {
  params: Promise<{ memberId: string }>;
};

export default async function PeopleDetailPage({ params }: PeopleDetailPageProps) {
  const { memberId } = await params;

  return (
    <PlaceholderPage
      eyebrow="Growth Profile"
      title={`성장 프로필 #${memberId}`}
      description="Guest 공개 필드와 Member 전용 상세 필드를 권한별로 분리해 보여줄 프로필 화면입니다."
    />
  );
}
