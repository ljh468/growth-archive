"use client";

import { useRouter } from "next/navigation";

export function MobileBackButton({ fallbackHref, label = "이전" }: { fallbackHref: string; label?: string }) {
  const router = useRouter();

  function goBack() {
    const referrer = document.referrer;
    const fromSameOrigin = referrer ? new URL(referrer).origin === window.location.origin : false;
    if (fromSameOrigin && window.history.length > 1) {
      router.back();
      return;
    }
    router.push(fallbackHref);
  }

  return (
    <button
      aria-label={`${label} 화면으로 이동`}
      className="inline-flex w-fit items-center gap-1 text-xs font-normal leading-none text-[var(--color-deep-green)] transition active:translate-y-px sm:hidden"
      onClick={goBack}
      type="button"
    >
      <span aria-hidden="true" className="text-sm leading-none">←</span>
      {label}
    </button>
  );
}
