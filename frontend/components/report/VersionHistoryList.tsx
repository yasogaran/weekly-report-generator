import Link from "next/link";
import { ReportDTO } from "@/lib/reportTypes";

interface VersionHistoryListProps {
  versions: ReportDTO[];
  currentReportId: number;
}

// Version chain list — shared by the team member's view page and the manager review page,
// both of which link to /reports/{version.id}/view for every non-current row (that page
// already works for any role: it only gates the Edit button on ownership, not viewing).
// Renders nothing when there's only one version, since "history" of one entry isn't history.
export default function VersionHistoryList({ versions, currentReportId }: VersionHistoryListProps) {
  if (versions.length <= 1) return null;

  return (
    <section className="flex flex-col gap-3 border-t border-line pt-6">
      <h3 className="text-sm font-semibold text-ink">Version history</h3>
      <ul className="flex flex-col gap-2">
        {versions.map((version) => (
          <li
            key={version.id}
            className="flex items-center justify-between rounded border border-line px-3 py-2 text-sm"
          >
            <span className="text-ink">
              Version {version.versionNumber} — {version.status}
              {version.submittedAt
                ? ` — submitted ${new Date(version.submittedAt).toLocaleDateString()}`
                : " — not yet submitted"}
            </span>
            {version.id === currentReportId ? (
              <span className="text-xs font-medium text-muted">Viewing</span>
            ) : (
              <Link href={`/reports/${version.id}/view`} className="text-accent hover:underline">
                View
              </Link>
            )}
          </li>
        ))}
      </ul>
    </section>
  );
}
