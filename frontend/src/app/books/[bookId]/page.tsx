import { BookDetailClient } from "./BookDetailClient";

type BookDetailPageProps = {
  params: Promise<{ bookId: string }>;
};

export default async function BookDetailPage({ params }: BookDetailPageProps) {
  const { bookId } = await params;

  return <BookDetailClient bookId={bookId} />;
}
