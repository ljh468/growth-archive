import { PlaceholderPage } from "@/components/ui/PlaceholderPage";

type BookDetailPageProps = {
  params: Promise<{ bookId: string }>;
};

export default async function BookDetailPage({ params }: BookDetailPageProps) {
  const { bookId } = await params;

  return (
    <PlaceholderPage
      eyebrow="Book Detail"
      title={`책 상세 #${bookId}`}
      description="평균 평점, 기록 수, 읽은 사람, 작성자별 독서기록을 연결할 공개 상세 화면입니다."
    />
  );
}
