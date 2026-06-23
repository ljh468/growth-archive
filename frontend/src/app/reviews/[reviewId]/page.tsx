import { PlaceholderPage } from "@/components/ui/PlaceholderPage";

type ReviewDetailPageProps = {
  params: Promise<{ reviewId: string }>;
};

export default async function ReviewDetailPage({ params }: ReviewDetailPageProps) {
  const { reviewId } = await params;

  return (
    <PlaceholderPage
      eyebrow="Review Detail"
      title={`모임 후기 #${reviewId}`}
      description="후기 본문, 작성자, 공개 사진, 사진 개인정보 안내를 연결할 상세 화면입니다."
    />
  );
}
