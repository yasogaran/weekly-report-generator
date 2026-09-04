import Tooltip from "./Tooltip";

export type ReportStatus = "DRAFT" | "SUBMITTED" | "NEEDS_CORRECTION" | "APPROVED";

interface StatusConfig {
  label: string;
  explanation: string;
  colorClasses: string;
}

const STATUS_CONFIG: Record<ReportStatus, StatusConfig> = {
  DRAFT: {
    label: "Draft",
    explanation: "Not yet submitted — you can still edit this report.",
    colorClasses: "border-status-draft text-status-draft bg-status-draft/10",
  },
  SUBMITTED: {
    label: "Submitted",
    explanation: "Waiting for your manager to review this report.",
    colorClasses: "border-status-submitted text-status-submitted bg-status-submitted/10",
  },
  NEEDS_CORRECTION: {
    label: "Needs correction",
    explanation: "Your manager requested changes — edit and resubmit this report.",
    colorClasses:
      "border-status-needsCorrection text-status-needsCorrection bg-status-needsCorrection/10",
  },
  APPROVED: {
    label: "Approved",
    explanation: "This report has been reviewed and approved — no further changes are needed.",
    colorClasses: "border-status-approved text-status-approved bg-status-approved/10",
  },
};

interface StatusBadgeProps {
  status: ReportStatus;
}

// Colored pill + text label for a report's status — color is never the only signal, the word
// itself is always rendered too. Wrapped in a Tooltip explaining what the status means, since
// this is meant to be understandable to anyone new to the tool.
export default function StatusBadge({ status }: StatusBadgeProps) {
  const config = STATUS_CONFIG[status];
  return (
    <Tooltip content={config.explanation}>
      <span
        className={`inline-flex items-center gap-1.5 rounded border px-2 py-0.5 text-xs font-medium ${config.colorClasses}`}
      >
        <span className="h-1.5 w-1.5 rounded-full bg-current" aria-hidden="true" />
        {config.label}
      </span>
    </Tooltip>
  );
}
