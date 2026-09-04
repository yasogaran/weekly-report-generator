import tailwindConfig from "../tailwind.config";
import { ReportStatus } from "./reportTypes";

// Single source of truth for chart colors — pulled from tailwind.config.ts (the actual
// design tokens, docs/frontend-design-system.md §1) instead of repeating hex values in
// every chart component. If a token changes in tailwind.config.ts, every chart picks it up
// automatically instead of drifting.
const colors = tailwindConfig.theme?.extend?.colors as {
  accent: string;
  muted: string;
  line: string;
  status: Record<"draft" | "submitted" | "needsCorrection" | "approved", string>;
  feedback: Record<"success" | "error" | "warning" | "info", string>;
};

export const ACCENT_COLOR = colors.accent;
export const MUTED_COLOR = colors.muted;
export const LINE_COLOR = colors.line;

// StatusByMemberChart is the one place chart colors are semantically meaningful (they must
// match StatusBadge's colors for the same status), not just decorative — everywhere else a
// chart needs a color, it uses ACCENT_COLOR/MUTED_COLOR above instead.
export const STATUS_COLORS: Record<ReportStatus, string> = {
  DRAFT: colors.status.draft,
  SUBMITTED: colors.status.submitted,
  NEEDS_CORRECTION: colors.status.needsCorrection,
  APPROVED: colors.status.approved,
};

export const FEEDBACK_COLORS = {
  success: colors.feedback.success,
  error: colors.feedback.error,
  warning: colors.feedback.warning,
  info: colors.feedback.info,
};
