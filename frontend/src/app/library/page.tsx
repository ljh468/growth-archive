import { PlaceholderPage } from "@/components/ui/PlaceholderPage";

export default function LibraryPage() {
  return (
    <PlaceholderPage
      eyebrow="Reading Library"
      title="독서기록 라이브러리"
      description="멤버들이 남긴 공개 독서기록과 책별 성장 기록을 탐색하는 공개 화면입니다."
      primaryHref="/reading-records/new"
      primaryLabel="독서기록 작성"
    />
  );
}
