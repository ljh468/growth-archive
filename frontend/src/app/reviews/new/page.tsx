import { ProtectedPlaceholderPage } from "@/components/ui/ProtectedPlaceholderPage";

export default function NewReviewPage() {
  return (
    <ProtectedPlaceholderPage
      required="MEMBER"
      eyebrow="Meeting Review"
      title="모임 후기 작성"
      description="참석 버튼 여부와 무관하게 활성 멤버가 후기를 작성하고 사진을 최대 10장 업로드하는 화면입니다."
    />
  );
}
