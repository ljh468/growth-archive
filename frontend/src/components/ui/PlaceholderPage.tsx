import { Button, Card, EmptyState, PageHeader, Section, Tag } from "@/components/ui/primitives";

type PlaceholderPageProps = {
  eyebrow: string;
  title: string;
  description: string;
  status?: string;
  primaryHref?: string;
  primaryLabel?: string;
};

export function PlaceholderPage({
  eyebrow,
  title,
  description,
  status = "Phase 3 placeholder",
  primaryHref,
  primaryLabel,
}: PlaceholderPageProps) {
  return (
    <main>
      <Section>
        <div className="grid gap-8">
          <PageHeader eyebrow={eyebrow} title={title} description={description} />
          <Card>
            <div className="flex flex-wrap items-center justify-between gap-4">
              <Tag>{status}</Tag>
              {primaryHref && primaryLabel && <Button href={primaryHref}>{primaryLabel}</Button>}
            </div>
            <div className="mt-6">
              <EmptyState title="데이터 연결 전입니다." description="다음 기능 단계에서 API와 실제 목록, 상세 데이터를 연결합니다." />
            </div>
          </Card>
        </div>
      </Section>
    </main>
  );
}
