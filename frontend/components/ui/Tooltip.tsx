"use client";

import { ReactNode, useRef, useState } from "react";

export interface TooltipProps {
  content: string;
  children: ReactNode;
  delayMs?: number;
}

const SHOW_DELAY_MS = 300;

// Short hover/focus-triggered label. Triggers on focus as well as mouse hover so keyboard
// users get the same information (per the design doc's accessibility rules) — this is a
// client component because it tracks show/hide state and a show delay.
export default function Tooltip({ content, children, delayMs = SHOW_DELAY_MS }: TooltipProps) {
  const [visible, setVisible] = useState(false);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const show = () => {
    timerRef.current = setTimeout(() => setVisible(true), delayMs);
  };

  const hide = () => {
    if (timerRef.current) clearTimeout(timerRef.current);
    setVisible(false);
  };

  return (
    <span
      className="relative inline-flex"
      onMouseEnter={show}
      onMouseLeave={hide}
      onFocus={show}
      onBlur={hide}
    >
      {children}
      {visible && (
        <span
          role="tooltip"
          className="pointer-events-none absolute bottom-full left-1/2 z-50 mb-1.5 w-max max-w-xs -translate-x-1/2 rounded bg-ink px-2 py-1 text-xs font-normal text-paper shadow-lg"
        >
          {content}
        </span>
      )}
    </span>
  );
}
