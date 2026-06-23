import type { ReactNode } from "react";

type ButtonProps = {
  children: ReactNode;
  href?: string;
  type?: "button" | "submit";
  variant?: "primary" | "secondary" | "ghost";
};

export function Button({ children, href, type = "button", variant = "primary" }: ButtonProps) {
  const className = [
    "inline-flex min-h-11 items-center justify-center border px-4 py-2 text-sm font-semibold transition",
    variant === "primary" && "bg-[var(--color-ink)] text-[var(--color-warm-white)]",
    variant === "secondary" && "border border-[var(--color-line)] bg-[var(--color-warm-white)] text-[var(--color-ink)]",
    variant === "ghost" && "border-transparent text-[var(--color-charcoal)]",
  ]
    .filter(Boolean)
    .join(" ");

  if (href) {
    return (
      <a className={className} href={href}>
        {children}
      </a>
    );
  }

  return (
    <button className={className} type={type}>
      {children}
    </button>
  );
}

export function Card({ children }: { children: ReactNode }) {
  return (
    <article className="rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] p-5 shadow-sm">
      {children}
    </article>
  );
}

export function Section({ children }: { children: ReactNode }) {
  return <section className="mx-auto w-full max-w-6xl px-5 py-8 sm:px-8 lg:px-10">{children}</section>;
}

export function PageHeader({ eyebrow, title, description }: { eyebrow: string; title: string; description?: string }) {
  return (
    <div className="max-w-3xl">
      <p className="text-sm font-medium text-[var(--color-bronze)]">{eyebrow}</p>
      <h1 className="mt-3 text-3xl font-semibold leading-tight sm:text-4xl">{title}</h1>
      {description && <p className="mt-4 text-sm leading-6 text-[var(--color-charcoal)] sm:text-base">{description}</p>}
    </div>
  );
}

export function EmptyState({ title, description }: { title: string; description: string }) {
  return (
    <div className="rounded-[var(--radius-card)] border border-dashed border-[var(--color-line)] bg-[var(--color-warm-white)] p-6">
      <h2 className="text-lg font-semibold">{title}</h2>
      <p className="mt-2 text-sm leading-6 text-[var(--color-charcoal)]">{description}</p>
    </div>
  );
}

export function ErrorState({ title, description }: { title: string; description: string }) {
  return (
    <div className="rounded-[var(--radius-card)] border border-[var(--color-bronze)] bg-[var(--color-warm-white)] p-6">
      <h2 className="text-lg font-semibold">{title}</h2>
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
    <div className="flex size-10 items-center justify-center rounded-full bg-[var(--color-deep-green)] text-sm font-semibold text-[var(--color-warm-white)]">
      {initial}
    </div>
  );
}

export function Tag({ children }: { children: ReactNode }) {
  return (
    <span className="inline-flex rounded-[var(--radius-card)] border border-[var(--color-line)] px-2.5 py-1 text-xs text-[var(--color-charcoal)]">
      {children}
    </span>
  );
}
