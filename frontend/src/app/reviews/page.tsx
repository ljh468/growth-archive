import { PlaceholderPage } from "@/components/ui/PlaceholderPage";

export default function ReviewsPage() {
  return (
    <PlaceholderPage
      eyebrow="Meeting Reviews"
      title="모임 후기"
      description="공개된 모임 후기와 최대 10장의 후기 사진을 탐색하는 공개 화면입니다."
      primaryHref="/reviews/new"
      primaryLabel="후기 작성"
    />
  );
}
