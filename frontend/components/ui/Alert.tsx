"use client";

import { ReactNode, useState } from "react";

type AlertVariant = "warning" | "info" | "success" | "error";

export interface AlertProps {
  variant: AlertVariant;
  title?: string;
  children: ReactNode;
  /** Only pass true for one-time notices — never for state that reflects reality (e.g. a
   * correction comment), which per the design doc must stay visible for the whole session. */
  dismissible?: boolean;
}

const variantClasses: Record<AlertVariant, string> = {
  warning: "border-feedback-warning text-feedback-warning",
  info: "border-feedback-info text-feedback-info",
  success: "border-feedback-success text-feedback-success",
  error: "border-feedback-error text-feedback-error",
};

// Persistent inline banner for context that should stay visible while the person works
// (e.g. a manager's correction comment above the edit form) — distinct from Toast, which
// flashes and disappears. Manages its own dismissed state, so this is a client component;
// the dismiss button only renders at all when `dismissible` is true.
export default function Alert({ variant, title, children, dismissible = false }: AlertProps) {
  const [dismissed, setDismissed] = useState(false);
  if (dismissed) return null;

  return (
    <div
      role="alert"
      className={`flex items-start justify-between gap-3 rounded border bg-surface px-4 py-3 text-sm ${variantClasses[variant]}`}
    >
      <div>
        {title && <p className="font-medium">{title}</p>}
        <div className="text-ink">{children}</div>
      </div>
      {dismissible && (
        <button
          type="button"
          aria-label="Dismiss"
          onClick={() => setDismissed(true)}
          className="text-current"
        >
          ×
        </button>
      )}
    </div>
  );
}
