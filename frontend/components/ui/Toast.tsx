"use client";

import { ReactNode, createContext, useCallback, useContext, useState } from "react";

type ToastVariant = "success" | "error" | "info";

interface ToastMessage {
  id: number;
  variant: ToastVariant;
  message: string;
}

interface ToastContextValue {
  success: (message: string) => void;
  error: (message: string) => void;
  info: (message: string) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

const AUTO_DISMISS_MS = 4000;

const variantClasses: Record<ToastVariant, string> = {
  success: "bg-feedback-success",
  error: "bg-feedback-error",
  info: "bg-feedback-info",
};

// Mounts once at the app root (see app/layout.tsx). Owns the live list of toasts and renders
// the top-right stack; any descendant component can push a toast via useToast() without
// prop-drilling a callback down through the tree.
export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  const dismiss = useCallback((id: number) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id));
  }, []);

  const push = useCallback(
    (variant: ToastVariant, message: string) => {
      const id = Date.now() + Math.random();
      setToasts((prev) => [...prev, { id, variant, message }]);
      setTimeout(() => dismiss(id), AUTO_DISMISS_MS);
    },
    [dismiss]
  );

  const value: ToastContextValue = {
    success: (message) => push("success", message),
    error: (message) => push("error", message),
    info: (message) => push("info", message),
  };

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="fixed top-4 right-4 z-50 flex w-full max-w-sm flex-col gap-2">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            role="status"
            className={`flex items-center justify-between gap-3 rounded px-4 py-2 text-sm font-medium text-paper shadow-lg ${variantClasses[toast.variant]}`}
          >
            <span>{toast.message}</span>
            <button
              type="button"
              aria-label="Dismiss notification"
              onClick={() => dismiss(toast.id)}
              className="text-paper/80 hover:text-paper"
            >
              ×
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

// Access to the toast API from any client component. Throws if called outside
// ToastProvider so a missing provider fails loudly during development, not silently.
export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext);
  if (!ctx) {
    throw new Error("useToast must be used within a ToastProvider");
  }
  return ctx;
}
