"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Inbox } from "lucide-react";
import ViewReportButton from "@/components/report/ViewReportButton";
import DataTable, { DataTableColumn } from "@/components/ui/DataTable";
import EmptyState from "@/components/ui/EmptyState";
import StatusBadge from "@/components/ui/StatusBadge";
import { useToast } from "@/components/ui/Toast";
import { PageResponse, apiRequest } from "@/lib/apiClient";
import { ReportSummaryDTO } from "@/lib/reportTypes";

const PAGE_SIZE = 10;

// "use client": needs apiClient (localStorage token), and owns pagination state that
// changes on click without a full navigation.
export default function ReportHistoryPage() {
  const router = useRouter();
  const toast = useToast();
  // DataTable's pagination footer is 1-indexed ("Page 1 of N"); Spring's `page` query param
  // is 0-indexed. This component only ever deals in the 1-indexed version and converts at
  // the one place it calls the API.
  const [page, setPage] = useState(1);
  const [data, setData] = useState<PageResponse<ReportSummaryDTO> | null>(null);

  useEffect(() => {
    // For a team member this is already scoped server-side to their own reports
    // (GET /api/reports, api-doc.md — `memberId` is silently overridden to the caller's own
    // id) — no client-side filtering by user needed here.
    apiRequest<PageResponse<ReportSummaryDTO>>(`/api/reports?page=${page - 1}&size=${PAGE_SIZE}`)
      .then(setData)
      .catch(() => toast.error("Couldn't load your reports — check your connection and try again."));
    // `toast`'s identity changes every time a toast fires, which would re-trigger this
    // fetch in a loop on failure — deliberately excluded, refetch on page change only.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  const columns: DataTableColumn<ReportSummaryDTO>[] = [
    {
      key: "week",
      header: "Week",
      // No longer a link — the explicit View action below is the only way to navigate from
      // this table now, so nothing else here should look or behave like it's clickable.
      render: (row) => `${row.weekStartDate} – ${row.weekEndDate}`,
    },
    { key: "project", header: "Project", render: (row) => row.projectName },
    { key: "status", header: "Status", render: (row) => <StatusBadge status={row.status} /> },
    {
      key: "updated",
      header: "Last updated",
      render: (row) => new Date(row.updatedAt).toLocaleDateString(),
    },
    {
      key: "actions",
      // Blank header, matching the existing convention for action columns (see
      // app/(manager)/projects/page.tsx and app/(manager)/users/page.tsx).
      header: "",
      render: (row) => (
        <div className="flex justify-end">
          <ViewReportButton reportId={row.id} status={row.status} viewerRole="TEAM_MEMBER" />
        </div>
      ),
    },
  ];

  if (data && data.totalElements === 0) {
    return (
      <EmptyState
        icon={<Inbox size={32} />}
        title="No reports yet"
        description="Create your first weekly report to get started."
        actionLabel="Create report"
        onAction={() => router.push("/reports/new")}
      />
    );
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-[28px] font-semibold text-ink">Report history</h1>
      <DataTable
        columns={columns}
        rows={data?.content ?? []}
        rowKey={(row) => row.id}
        pagination={
          data
            ? { page, totalPages: Math.max(data.totalPages, 1), onPageChange: setPage }
            : undefined
        }
      />
    </div>
  );
}
