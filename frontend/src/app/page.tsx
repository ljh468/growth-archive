import { Button, Card, PageHeader, Section, Tag } from "@/components/ui/primitives";

export default function HomePage() {
  return (
    <main>
      <Section>
        <div className="grid min-h-[calc(100vh-10rem)] items-center gap-10 py-12 lg:grid-cols-[1.05fr_0.95fr]">
          <PageHeader
            eyebrow="부자습관 만들기"
            title="읽고, 실행하고, 성장한 기록을 남기는 사람들"
            description="Growth Archive는 멤버의 독서, 실행, 회고, 모임 기록을 차분하게 쌓는 프리미엄 성장 아카이브입니다."
          />

          <Card>
            <div className="flex flex-wrap gap-2">
              <Tag>Reading</Tag>
              <Tag>Action</Tag>
              <Tag>Meeting</Tag>
              <Tag>Reflection</Tag>
            </div>
            <h2 className="mt-6 text-2xl font-semibold">MVP App Shell</h2>
            <p className="mt-4 text-sm leading-6 text-[var(--color-charcoal)]">
              공개 탐색, 멤버 기록, 운영 관리 화면으로 이어지는 기본 내비게이션과 디자인 토큰이 적용되었습니다.
            </p>
            <div className="mt-6 flex flex-wrap gap-3">
              <Button href="/library">라이브러리 보기</Button>
              <Button href="/login" variant="secondary">
                로그인
              </Button>
            </div>
          </Card>
        </div>
      </Section>
    </main>
  );
}
