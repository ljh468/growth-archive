import { ReviewDetailClient } from "./ReviewDetailClient";

type ReviewDetailPageProps = {
  params: Promise<{ reviewId: string }>;
};

export default async function ReviewDetailPage({ params }: ReviewDetailPageProps) {
  const { reviewId } = await params;

  return <ReviewDetailClient reviewId={reviewId} />;
}
