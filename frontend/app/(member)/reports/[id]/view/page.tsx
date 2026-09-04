"use client";

import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { useParams } from "next/navigation";
import Button from "@/components/ui/Button";
import ReportDetail from "@/components/report/ReportDetail";
import VersionHistoryList from "@/components/report/VersionHistoryList";
import { useAuth } from "@/lib/authContext";
import { useReportWithVersions } from "@/lib/useReportWithVersions";

// "use client" for the same reason as the edit page: apiClient needs the JWT from
// localStorage, which doesn't exist during any server render. This page also reuses itself
// for viewing an old version — GET /api/reports/{id} works "from any id in the chain"
// (api-doc.md), so /reports/{oldVersionId}/view is not a different page, just a different
// id resolving to that frozen row's own content.
export default function ViewReportPage() {
  const params = useParams<{ id: string }>();
  const reportId = Number(params.id);
  const { currentUser } = useAuth();

  const { report, versions, notFound } = useReportWithVersions(reportId);

  if (notFound) {
    return <p className="text-sm text-muted">This report doesn&apos;t exist, or isn&apos;t yours to view.</p>;
  }

  // Covers both "hasn't loaded yet" and "navigated to a different id and the new fetch
  // hasn't resolved yet" — either way, don't render the previous id's (now-stale) content.
  if (!report || report.id !== reportId) {
    return <p className="text-sm text-muted">Loading…</p>;
  }

  const isEditable = report.status === "DRAFT" || report.status === "NEEDS_CORRECTION";
  const isOwnReport = currentUser?.id === report.userId;

  // This page is reachable by both roles (a manager lands here from /reports or a review
  // page's version history), so "back" means different things depending on who's looking:
  // a manager came from the report queue, a team member came from their own history.
  const backHref = currentUser?.role === "MANAGER" ? "/reports" : "/history";
  const backLabel = currentUser?.role === "MANAGER" ? "Back to reports" : "Back to history";

  return (
    <div className="mx-auto flex max-w-4xl flex-col gap-8 pb-12">
      <Link href={backHref} className="inline-flex w-fit">
        <Button variant="secondary">
          <ArrowLeft size={16} />
          {backLabel}
        </Button>
      </Link>

      <ReportDetail report={report} />

      <VersionHistoryList versions={versions} currentReportId={report.id} />

      {isEditable && isOwnReport && (
        <div className="flex justify-end border-t border-line pt-6">
          <Link href={`/reports/${report.id}/edit`}>
            <Button variant="primary">Edit</Button>
          </Link>
        </div>
      )}
    </div>
  );
}
