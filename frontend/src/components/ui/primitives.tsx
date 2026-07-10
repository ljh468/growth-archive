import type { CSSProperties, MouseEvent, ReactNode } from "react";

type ButtonProps = {
  children: ReactNode;
  className?: string;
  href?: string;
  onClick?: () => void;
  type?: "button" | "submit";
  variant?: "primary" | "secondary" | "ghost";
};

type RecordButtonProps = {
  children: ReactNode;
  className?: string;
  disabled?: boolean;
  href?: string;
  onClick?: (event: MouseEvent<HTMLButtonElement>) => void;
  style?: CSSProperties;
  type?: "button" | "submit";
  variant?: "default" | "primary" | "danger";
};

type MoreButtonProps = {
  "aria-label"?: string;
  children?: ReactNode;
  className?: string;
  disabled?: boolean;
  onClick?: () => void;
  type?: "button" | "submit";
};

type ConfirmDialogProps = {
  cancelLabel?: string;
  confirmLabel?: string;
  description: string;
  onCancel: () => void;
  onConfirm: () => void;
  open: boolean;
  title: string;
};

export function Button({ children, className, href, onClick, type = "button", variant = "primary" }: ButtonProps) {
  const classes = [
    "inline-flex min-h-11 max-w-full appearance-none self-start touch-manipulation items-center justify-center rounded-[var(--radius-card)] border border-transparent px-5 py-2.5 text-sm font-normal leading-none [font-family:var(--font-body)] transition focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--color-bronze)] active:translate-y-px",
    variant === "primary" && "bg-[var(--color-deep-green)] !text-[var(--color-warm-white)] shadow-[var(--shadow-soft)] hover:bg-[var(--color-wood-brown)]",
    variant === "secondary" && "bg-[var(--color-deep-green)] !text-[var(--color-warm-white)] shadow-[var(--shadow-soft)] hover:bg-[var(--color-wood-brown)]",
    variant === "ghost" && "bg-[var(--color-deep-green)] !text-[var(--color-warm-white)] hover:bg-[var(--color-wood-brown)]",
    className,
  ]
    .filter(Boolean)
    .join(" ");
  const style: CSSProperties = {
    appearance: "none",
    borderColor: "transparent",
    color: "var(--color-warm-white)",
    fontFamily: "var(--font-body)",
    fontSize: "0.875rem",
    fontWeight: 400,
    lineHeight: 1,
  };

  if (href) {
    return (
      <a className={classes} href={href} style={style}>
        {children}
      </a>
    );
  }

  return (
    <button className={classes} onClick={onClick} style={style} type={type}>
      {children}
    </button>
  );
}

export function RecordButton({ children, className, disabled, href, onClick, style, type = "button", variant = "default" }: RecordButtonProps) {
  const classes = [
    "archive-record-button",
    variant === "primary" && "archive-record-button--primary",
    variant === "danger" && "archive-record-button--danger",
    className,
  ]
    .filter(Boolean)
    .join(" ");

  if (href) {
    return <a className={classes} href={href}>{children}</a>;
  }

  return (
    <button className={classes} disabled={disabled} onClick={onClick} style={style} type={type}>
      {children}
    </button>
  );
}

export function MoreButton({ "aria-label": ariaLabel, children = "More", className, disabled, onClick, type = "button" }: MoreButtonProps) {
  return (
    <button aria-label={ariaLabel} className={["archive-more-button", className].filter(Boolean).join(" ")} disabled={disabled} onClick={onClick} type={type}>
      {children}
    </button>
  );
}

export function Card({ children }: { children: ReactNode }) {
  return (
    <article className="rounded-[var(--radius-card)] border border-[rgba(233,225,214,0.8)] bg-[rgba(255,254,250,0.94)] p-4 shadow-[var(--shadow-soft)] sm:p-6">
      {children}
    </article>
  );
}

export function ConfirmDialog({
  cancelLabel = "취소",
  confirmLabel = "삭제",
  description,
  onCancel,
  onConfirm,
  open,
  title,
}: ConfirmDialogProps) {
  if (!open) {
    return null;
  }

  return (
    <div aria-modal="true" className="fixed inset-0 z-50 grid place-items-center bg-[rgba(34,34,28,0.32)] px-5 backdrop-blur-[2px]" role="dialog">
      <div className="w-full max-w-sm rounded-[var(--radius-card)] border border-[rgba(229,222,209,0.9)] bg-[var(--color-warm-white)] p-5 shadow-[0_24px_70px_rgba(63,47,34,0.22)]">
        <p className="font-display text-base font-normal leading-6 text-[var(--color-ink)]">{title}</p>
        <p className="mt-2 text-sm leading-6 text-[var(--color-muted)]">{description}</p>
        <div className="mt-5 flex justify-end gap-2">
          <button
            className="inline-flex min-h-10 items-center justify-center rounded-[var(--radius-card)] border border-[var(--color-line)] bg-[var(--color-warm-white)] px-4 text-[10px] font-normal text-[var(--color-charcoal)] transition hover:border-[var(--color-deep-green)] hover:bg-[rgba(47,90,67,0.045)]"
            onClick={onCancel}
            type="button"
          >
            {cancelLabel}
          </button>
          <button
            className="inline-flex min-h-10 items-center justify-center rounded-[var(--radius-card)] border border-[rgba(74,52,36,0.18)] bg-[var(--color-wood-brown)] px-4 text-[10px] font-normal text-[var(--color-warm-white)] transition hover:bg-[var(--color-deep-green)]"
            onClick={onConfirm}
            type="button"
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}

export function Section({ children }: { children: ReactNode }) {
  return <section className="mx-auto w-full max-w-6xl px-5 py-9 sm:px-8 sm:py-18 lg:px-10 lg:py-20">{children}</section>;
}

export function PageHeader({ eyebrow, title, description }: { eyebrow: string; title: string; description?: string }) {
  return (
    <div className="max-w-3xl">
      <p className="font-latin text-2xl leading-none text-[var(--color-bronze)] sm:text-4xl">{eyebrow}</p>
      <h1 className="mt-2 font-display text-2xl font-normal leading-[1.2] sm:mt-4 sm:text-4xl">{title}</h1>
      {description && <p className="mt-5 hidden max-w-2xl text-base leading-8 text-[var(--color-muted)] sm:block">{description}</p>}
    </div>
  );
}

export function EmptyState({ title, description }: { title: string; description: string }) {
  return (
    <div className="rounded-[var(--radius-card)] border border-dashed border-[var(--color-line)] bg-[rgba(255,254,250,0.78)] p-4 sm:p-7">
      <h2 className="text-base font-normal sm:text-lg">{title}</h2>
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

export function SkeletonBlock({ className = "" }: { className?: string }) {
  return (
    <div
      aria-hidden="true"
      className={[
        "animate-pulse rounded-[var(--radius-card)] bg-[linear-gradient(90deg,rgba(229,220,207,0.58),rgba(255,254,250,0.86),rgba(229,220,207,0.58))] bg-[length:220%_100%]",
        className,
      ].join(" ")}
    />
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
