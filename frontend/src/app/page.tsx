const navigationItems = ["Home", "Library", "Meetings", "People", "My"];

export default function HomePage() {
  return (
    <main className="min-h-screen bg-[var(--color-ivory)] text-[var(--color-ink)]">
      <section className="mx-auto flex min-h-screen w-full max-w-6xl flex-col px-5 py-6 sm:px-8 lg:px-10">
        <header className="flex items-center justify-between border-b border-[var(--color-line)] pb-4">
          <div>
            <p className="text-sm font-medium text-[var(--color-bronze)]">부자습관 만들기</p>
            <h1 className="text-xl font-semibold">Growth Archive</h1>
          </div>
          <nav className="hidden gap-6 text-sm text-[var(--color-charcoal)] md:flex">
            {navigationItems.slice(0, 4).map((item) => (
              <a href="#" key={item}>
                {item}
              </a>
            ))}
          </nav>
        </header>

        <div className="grid flex-1 items-center gap-10 py-12 lg:grid-cols-[1.05fr_0.95fr]">
          <div className="max-w-2xl">
            <p className="text-sm font-medium uppercase tracking-[0.08em] text-[var(--color-deep-green)]">
              Premium Growth Archive
            </p>
            <h2 className="mt-4 text-4xl font-semibold leading-tight sm:text-5xl">
              읽고, 실행하고,
              <br />
              성장한 기록을 남기는 사람들
            </h2>
            <p className="mt-6 max-w-xl text-base leading-7 text-[var(--color-charcoal)]">
              Growth Archive는 부자습관 만들기 멤버의 독서, 실행, 회고, 모임 기록을 차분하게 쌓는 MVP
              부트스트랩 화면입니다.
            </p>
          </div>

          <div className="border border-[var(--color-line)] bg-[var(--color-warm-white)] p-6 shadow-sm">
            <p className="text-sm font-medium text-[var(--color-bronze)]">Phase 0</p>
            <h3 className="mt-3 text-2xl font-semibold">Repository Bootstrap</h3>
            <dl className="mt-6 grid gap-4 text-sm">
              <div className="flex justify-between border-b border-[var(--color-line)] pb-3">
                <dt>Frontend</dt>
                <dd>Next.js + TypeScript + Tailwind</dd>
              </div>
              <div className="flex justify-between border-b border-[var(--color-line)] pb-3">
                <dt>Backend</dt>
                <dd>Java 25 + Spring Boot 4.1.0</dd>
              </div>
              <div className="flex justify-between">
                <dt>API</dt>
                <dd>/api/v1/health</dd>
              </div>
            </dl>
          </div>
        </div>

        <nav className="fixed inset-x-0 bottom-0 grid grid-cols-5 border-t border-[var(--color-line)] bg-[var(--color-warm-white)] px-2 py-2 text-center text-xs text-[var(--color-charcoal)] md:hidden">
          {navigationItems.map((item) => (
            <a className="py-2" href="#" key={item}>
              {item}
            </a>
          ))}
        </nav>
      </section>
    </main>
  );
}
