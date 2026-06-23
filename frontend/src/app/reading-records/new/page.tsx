import { ProtectedPlaceholderPage } from "@/components/ui/ProtectedPlaceholderPage";

export default function NewReadingRecordPage() {
  return (
    <ProtectedPlaceholderPage
      required="MEMBER"
      eyebrow="Reading Record"
      title="독서기록 작성"
      description="책 선택, 한 줄 리뷰, 선택 평점, 블로그 URL, 대표 이미지를 입력할 멤버 전용 작성 화면입니다."
    />
  );
}
