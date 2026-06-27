import type { ReactNode } from "react";

type ButtonProps = {
  children: ReactNode;
  href?: string;
  type?: "button" | "submit";
  variant?: "primary" | "secondary" | "ghost";
};

export function Button({ children, href, type = "button", variant = "primary" }: ButtonProps) {
  const className = [
    "inline-flex min-h-11 items-center justify-center rounded-[var(--radius-card)] px-5 py-2.5 text-sm font-normal transition focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--color-bronze)] active:translate-y-px",
    variant === "primary" && "bg-[var(--color-deep-green)] !text-[var(--color-warm-white)] shadow-[var(--shadow-soft)] hover:bg-[var(--color-wood-brown)]",
    variant === "secondary" && "bg-[var(--color-deep-green)] !text-[var(--color-warm-white)] shadow-[var(--shadow-soft)] hover:bg-[var(--color-wood-brown)]",
    variant === "ghost" && "bg-[var(--color-deep-green)] !text-[var(--color-warm-white)] hover:bg-[var(--color-wood-brown)]",
  ]
    .filter(Boolean)
    .join(" ");

  if (href) {
    return (
      <a className={className} href={href} style={{ color: "var(--color-warm-white)" }}>
        {children}
      </a>
    );
  }

  return (
    <button className={className} style={{ color: "var(--color-warm-white)" }} type={type}>
      {children}
    </button>
  );
}

export function Card({ children }: { children: ReactNode }) {
  return (
    <article className="rounded-[var(--radius-card)] border border-[rgba(233,225,214,0.8)] bg-[rgba(255,254,250,0.94)] p-6 shadow-[var(--shadow-soft)]">
      {children}
    </article>
  );
}

export function Section({ children }: { children: ReactNode }) {
  return <section className="mx-auto w-full max-w-6xl px-5 py-14 sm:px-8 sm:py-18 lg:px-10 lg:py-20">{children}</section>;
}

export function PageHeader({ eyebrow, title, description }: { eyebrow: string; title: string; description?: string }) {
  return (
    <div className="max-w-3xl">
      <p className="font-latin text-3xl text-[var(--color-bronze)] sm:text-4xl">{eyebrow}</p>
      <h1 className="mt-4 font-display text-3xl font-normal leading-[1.2] sm:text-4xl">{title}</h1>
      {description && <p className="mt-5 max-w-2xl text-base leading-8 text-[var(--color-muted)]">{description}</p>}
    </div>
  );
}

export function EmptyState({ title, description }: { title: string; description: string }) {
  return (
    <div className="rounded-[var(--radius-card)] border border-dashed border-[var(--color-line)] bg-[rgba(255,254,250,0.78)] p-7">
      <h2 className="text-lg font-normal">{title}</h2>
      <p className="mt-2 text-sm leading-6 text-[var(--color-muted)]">{description}</p>
    </div>
  );
}

export function ErrorState({ title, description }: { title: string; description: string }) {
  return (
    <div className="rounded-[var(--radius-card)] border border-[var(--color-bronze)] bg-[var(--color-warm-white)] p-6">
      <h2 className="text-lg font-normal">{title}</h2>
      <p className="mt-2 text-sm leading-6 text-[var(--color-charcoal)]">{description}</p>
    </div>
  );
}

export function LoadingState() {
  return (
    <div className="grid gap-3" aria-label="Loading">
      <div className="h-4 w-32 bg-[var(--color-line)]" />
      <div className="h-20 w-full bg-[var(--color-line)]" />
    </div>
  );
}

export function Avatar({ name }: { name: string }) {
  const initial = name.trim().slice(0, 1) || "G";
  return (
    <div className="flex size-10 items-center justify-center rounded-full bg-[var(--color-deep-green)] text-sm font-normal text-[var(--color-warm-white)]">
      {initial}
    </div>
  );
}

export function Tag({ children }: { children: ReactNode }) {
  return (
    <span className="inline-flex min-h-7 items-center rounded-full border border-[rgba(63,95,74,0.24)] bg-[rgba(255,254,250,0.82)] px-3 py-1 text-xs font-normal leading-none text-[var(--color-charcoal)]">
      {children}
    </span>
  );
}
