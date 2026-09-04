"use client";

import {
  ReactNode,
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from "react";
import Button from "./Button";

// Selector for elements a focus trap should cycle between — covers the Cancel/Confirm
// buttons and a possible close "X", which is all this dialog ever contains.
const FOCUSABLE_SELECTOR =
  'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

export interface ConfirmOptions {
  title: string;
  description: string;
  /** Confirm button variant — destructive for delete/deactivate, primary for workflow actions. */
  variant?: "primary" | "destructive";
  confirmLabel?: string;
  cancelLabel?: string;
}

type ConfirmFn = (options: ConfirmOptions) => Promise<boolean>;

const ConfirmContext = createContext<ConfirmFn | null>(null);

interface PendingConfirm extends ConfirmOptions {
  resolve: (result: boolean) => void;
}

// Mounts once at the app root, same pattern as ToastProvider. Holds at most one pending
// confirmation at a time and resolves its promise when the person picks Cancel or Confirm.
export function ConfirmProvider({ children }: { children: ReactNode }) {
  const [pending, setPending] = useState<PendingConfirm | null>(null);

  const confirm = useCallback<ConfirmFn>((options) => {
    return new Promise<boolean>((resolve) => {
      setPending({ ...options, resolve });
    });
  }, []);

  // Memoized against `pending` (not recreated on every render) so ConfirmDialogView's focus
  // effect — which depends on this callback's identity — doesn't re-run and steal focus back
  // to Cancel whenever something unrelated causes ConfirmProvider to re-render while open.
  const close = useCallback(
    (result: boolean) => {
      pending?.resolve(result);
      setPending(null);
    },
    [pending]
  );

  return (
    <ConfirmContext.Provider value={confirm}>
      {children}
      {pending && <ConfirmDialogView pending={pending} onClose={close} />}
    </ConfirmContext.Provider>
  );
}

interface ConfirmDialogViewProps {
  pending: PendingConfirm;
  onClose: (result: boolean) => void;
}

// Pure presentation + focus management for one open dialog. Split out from ConfirmProvider
// so it mounts/unmounts with `pending`, giving each open dialog a fresh effect run (and a
// clean previously-focused-element capture) rather than trying to detect "just opened"
// inside a component that's always mounted.
function ConfirmDialogView({ pending, onClose }: ConfirmDialogViewProps) {
  const onCancel = useCallback(() => onClose(false), [onClose]);
  const onConfirm = useCallback(() => onClose(true), [onClose]);
  const dialogRef = useRef<HTMLDivElement>(null);
  const cancelButtonRef = useRef<HTMLButtonElement>(null);
  const previouslyFocusedRef = useRef<HTMLElement | null>(null);

  useEffect(() => {
    // Remember what had focus before the dialog opened so it can be restored on close,
    // regardless of which of the four close paths (Confirm/Cancel/Escape/outside click) fires.
    previouslyFocusedRef.current = document.activeElement as HTMLElement | null;

    // Focus Cancel, not Confirm — Confirm is often the destructive/state-changing action,
    // and defaulting focus there means a stray Enter press (e.g. from whatever keystroke
    // opened the dialog) could confirm before the person has read the description.
    cancelButtonRef.current?.focus();

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        event.preventDefault();
        onCancel();
        return;
      }

      if (event.key !== "Tab" || !dialogRef.current) return;

      const focusable = Array.from(
        dialogRef.current.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR)
      );
      if (focusable.length === 0) return;

      const first = focusable[0];
      const last = focusable[focusable.length - 1];
      const active = document.activeElement;

      // Wrap Tab/Shift+Tab at the edges so focus stays trapped inside the dialog instead
      // of escaping to the page behind it.
      if (event.shiftKey && active === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && active === last) {
        event.preventDefault();
        first.focus();
      }
    };

    // Clicking the backdrop counts as Cancel — anywhere outside the dialog panel itself.
    const handleMouseDown = (event: MouseEvent) => {
      if (dialogRef.current && !dialogRef.current.contains(event.target as Node)) {
        onCancel();
      }
    };

    document.addEventListener("keydown", handleKeyDown);
    document.addEventListener("mousedown", handleMouseDown);

    return () => {
      document.removeEventListener("keydown", handleKeyDown);
      document.removeEventListener("mousedown", handleMouseDown);
      previouslyFocusedRef.current?.focus();
    };
  }, [onCancel]);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink/40 p-4">
      <div
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby="confirm-dialog-title"
        className="w-full max-w-sm rounded bg-surface p-6 shadow-lg"
      >
        <h2 id="confirm-dialog-title" className="text-base font-semibold text-ink">
          {pending.title}
        </h2>
        <p className="mt-2 text-sm text-muted">{pending.description}</p>
        <div className="mt-6 flex justify-end gap-3">
          <Button ref={cancelButtonRef} variant="secondary" onClick={onCancel}>
            {pending.cancelLabel ?? "Cancel"}
          </Button>
          <Button
            variant={pending.variant === "destructive" ? "destructive" : "primary"}
            onClick={onConfirm}
          >
            {pending.confirmLabel ?? "Confirm"}
          </Button>
        </div>
      </div>
    </div>
  );
}

// Usage: `const ok = await confirm({ title, description }); if (ok) { doAction() }`.
// Throws if called outside ConfirmProvider so a missing provider fails loudly.
export function useConfirm(): ConfirmFn {
  const ctx = useContext(ConfirmContext);
  if (!ctx) {
    throw new Error("useConfirm must be used within a ConfirmProvider");
  }
  return ctx;
}
