import { ProfileDetailClient } from "./ProfileDetailClient";

type PeopleDetailPageProps = {
  params: Promise<{ memberId: string }>;
};

export default async function PeopleDetailPage({ params }: PeopleDetailPageProps) {
  const { memberId } = await params;

  return <ProfileDetailClient memberId={memberId} />;
}
