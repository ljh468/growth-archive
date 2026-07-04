import type { ReactNode } from "react";

const habits = [
  ["미라클 모닝", "매일", "정한 루틴으로 하루를 주도적으로 시작합니다."],
  ["독서 기록", "월 1회", "삶에 적용할 행동 변화를 기록하고 공유합니다."],
  ["목표설정 및 실행", "월 1회", "작은 목표를 실행하고 실패와 배움을 공유합니다."],
];

const guides: Array<[string, ReactNode]> = [
  ["일시/장소", "월 2회 일요일 오전 / 미사역 부근 카페, 2시간"],
  [
    "주제",
    <span className="grid gap-1" key="meeting-topics">
      <span>둘째 주 일요일 독서모임</span>
      <span>넷째 주 일요일 실행수다모임</span>
    </span>,
  ],
  ["회비", "월 3,000원"],
  ["가입 조건", "85~00년생, 성별 무관"],
  ["강퇴 사유", "가입 후 24시간 내 미인사, 2개월 미참석, 당일 취소 및 노쇼, 사전 연락 없는 지각이나 불참, 모임 분위기 저해"],
];

export default function AboutPage() {
  return (
    <main className="overflow-x-hidden bg-[var(--color-ivory)]">
      <section className="mx-auto w-full max-w-6xl px-5 py-8 sm:px-8 sm:py-18 lg:px-10 lg:py-20">
        <div className="mx-auto w-full min-w-0 max-w-full sm:max-w-4xl">
          <div className="relative sm:rounded-[12px] sm:border sm:border-[rgba(83,62,39,0.18)] sm:bg-[rgba(168,137,98,0.62)] sm:p-px sm:shadow-[0_22px_52px_rgba(63,47,34,0.09)]">
            <article className="relative w-full min-w-0 max-w-full sm:overflow-hidden sm:rounded-[11px] sm:border sm:border-[rgba(106,83,56,0.12)] sm:bg-[#fffdf6] sm:px-9 sm:py-10 sm:shadow-[inset_0_0_0_1px_rgba(255,255,255,0.72)] lg:px-12">
              <div className="pointer-events-none absolute inset-0 hidden bg-[linear-gradient(90deg,rgba(74,52,36,0.035),transparent_18%,transparent_82%,rgba(74,52,36,0.03)),repeating-linear-gradient(0deg,transparent_0,transparent_39px,rgba(106,83,56,0.042)_40px)] sm:block" />
              <div className="relative z-10 min-w-0">
                <header className="border-b border-[rgba(106,83,56,0.16)] pb-6 sm:pb-8">
                  <h1 className="max-w-full font-display text-[1.85rem] font-semibold leading-[1.22] text-[var(--color-deep-green)] [overflow-wrap:anywhere] sm:text-4xl sm:leading-[1.14]">부자습관 만들기 모임 소개</h1>
                  <p className="mt-5 max-w-3xl break-words text-sm leading-7 text-[var(--color-charcoal)] [overflow-wrap:anywhere] sm:text-base sm:leading-8">
                    <span className="block">환경에 휘둘리지 않는 주도적인 삶을 지향하며,</span>
                    <span className="block">책과 실행을 통해 한 달의 변화를</span>
                    <span className="block">함께 남기는 모임입니다.</span>
                  </p>
                </header>

                <section className="grid min-w-0 gap-7 py-7 sm:grid-cols-[1fr_1.1fr] sm:gap-10 sm:py-10">
                  <div className="min-w-0">
                    <blockquote className="border-l-2 border-[var(--color-deep-green)] pl-4 font-hand text-[1.35rem] leading-8 text-[var(--color-ink)] [overflow-wrap:anywhere] sm:text-2xl sm:leading-9">
                      주변 사람 5명의 평균이 곧 나의 수준이다.
                    </blockquote>
                    <div className="mt-6 flex flex-wrap gap-2 text-sm font-semibold text-[var(--color-deep-green)]">
                      <span className="note-chip">직장인 환영</span>
                      <span className="note-chip">프리랜서 환영</span>
                      <span className="note-chip">자영업자 환영</span>
                    </div>
                    <div className="mt-7 max-w-full rotate-[-1deg] border border-[rgba(169,120,69,0.22)] bg-[#f7efd9] px-4 py-3.5 shadow-[0_12px_24px_rgba(63,47,34,0.07)] sm:max-w-sm sm:px-5 sm:py-4">
                      <p className="font-hand text-[1.35rem] leading-8 text-[var(--color-wood-brown)] sm:text-2xl sm:leading-9">
                        읽고, 실행하고, 한 달의 변화를 남깁니다.
                      </p>
                    </div>
                  </div>

                  <NotebookSection title="우리가 만들어갈 3가지 습관">
                    <div className="grid gap-4">
                      {habits.map(([title, cadence, description], index) => (
                        <article className="grid min-w-0 grid-cols-[28px_minmax(0,1fr)] gap-2.5 border-b border-[rgba(106,83,56,0.14)] pb-4 last:border-b-0 sm:grid-cols-[34px_minmax(0,1fr)] sm:gap-3" key={title}>
                          <span className="flex size-7 items-center justify-center rounded-full bg-[rgba(47,90,67,0.09)] font-latin text-lg leading-none text-[var(--color-deep-green)] sm:size-8 sm:text-xl">{index + 1}</span>
                          <div className="min-w-0">
                            <div className="flex flex-wrap items-baseline gap-x-3 gap-y-1">
                              <h3 className="font-display text-base font-normal text-[var(--color-ink)] sm:text-lg">{title}</h3>
                              <span className="text-sm text-[var(--color-bronze)]">{cadence}</span>
                            </div>
                            <p className="mobile-hard-wrap mt-1 max-w-[calc(100vw-88px)] text-[13px] leading-6 text-[var(--color-charcoal)] sm:max-w-none sm:text-sm sm:leading-7">{description}</p>
                          </div>
                        </article>
                      ))}
                    </div>
                  </NotebookSection>
                </section>

                <NotebookSection title="정기 모임 안내">
                  <dl className="grid gap-3 border-t border-[rgba(106,83,56,0.16)] pt-5 sm:grid-cols-2 sm:gap-x-10">
                    {guides.map(([label, value]) => (
                      <div className="grid min-w-0 gap-1 border-b border-[rgba(106,83,56,0.12)] pb-3 sm:grid-cols-[82px_1fr] sm:gap-4" key={label}>
                        <dt className="text-sm leading-7 text-[var(--color-deep-green)]">{label}</dt>
                        <dd className="mobile-hard-wrap min-w-0 text-sm leading-7 text-[var(--color-charcoal)]">{value}</dd>
                      </div>
                    ))}
                  </dl>
                </NotebookSection>
              </div>
            </article>
          </div>
        </div>
      </section>
    </main>
  );
}

function NotebookSection({ children, title }: { children: ReactNode; title: string }) {
  return (
    <section className="min-w-0">
      <h2 className="mb-4 font-display text-lg font-normal leading-tight text-[var(--color-wood-brown)] [overflow-wrap:anywhere] sm:text-xl">{title}</h2>
      {children}
    </section>
  );
}
