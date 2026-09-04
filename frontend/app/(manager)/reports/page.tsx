"use client";

import { useEffect, useState } from "react";
import FilterBar, { DashboardFilters, EMPTY_FILTERS } from "@/components/dashboard/FilterBar";
import ViewReportButton from "@/components/report/ViewReportButton";
import DataTable, { DataTableColumn } from "@/components/ui/DataTable";
import EmptyState from "@/components/ui/EmptyState";
import StatusBadge from "@/components/ui/StatusBadge";
import { useToast } from "@/components/ui/Toast";
import { PageResponse, apiRequest } from "@/lib/apiClient";
import { ProjectDTO } from "@/lib/projectTypes";
import { ReportSummaryDTO } from "@/lib/reportTypes";
import { UserDTO } from "@/lib/userTypes";

const PAGE_SIZE = 10;

// Manager's report queue — the missing page that makes every submitted report reachable
// without typing a URL. Defaults the status filter to SUBMITTED ("what needs my attention"
// is the most common reason to open this), but that's just the initial value: the Select
// still offers "All statuses" like everywhere else FilterBar is used, nothing is locked.
const DEFAULT_FILTERS: DashboardFilters = { ...EMPTY_FILTERS, status: "SUBMITTED" };

export default function ManagerReportsPage() {
  const toast = useToast();
  const [filters, setFilters] = useState<DashboardFilters>(DEFAULT_FILTERS);
  const [page, setPage] = useState(1);
  const [data, setData] = useState<PageResponse<ReportSummaryDTO> | null>(null);
  const [members, setMembers] = useState<UserDTO[]>([]);
  const [projects, setProjects] = useState<ProjectDTO[]>([]);

  // Options for FilterBar's dropdowns — same pattern as the dashboard page, fetched once.
  useEffect(() => {
    apiRequest<PageResponse<UserDTO>>("/api/users?size=100")
      .then((result) => setMembers(result.content))
      .catch(() => {
        // Non-critical — the member filter dropdown just stays empty.
      });
    apiRequest<ProjectDTO[]>("/api/projects")
      .then(setProjects)
      .catch(() => {
        // Non-critical — the project filter dropdown just stays empty.
      });
  }, []);

  // Unlike the dashboard's FilterBar usage, these filters are real server-side query params
  // on GET /api/reports (docs/api/api-doc.md: "page, size ... plus filters: week, projectId,
  // status, memberId") — checked against the actual controller, not assumed. One real gap:
  // the endpoint only accepts a single `week` (exact match on weekStartDate), not a range,
  // so FilterBar's "To" date has nothing to bind to here — only "From" is sent, as `week`.
  // Extending the backend to accept a real weekStart/weekEnd range would be a clean
  // follow-up, but is out of scope for this page.
  useEffect(() => {
    const params = new URLSearchParams();
    params.set("page", String(page - 1));
    params.set("size", String(PAGE_SIZE));
    if (filters.projectId) params.set("projectId", String(filters.projectId));
    if (filters.status) params.set("status", filters.status);
    if (filters.weekStart) params.set("week", filters.weekStart);
    if (filters.memberId) params.set("memberId", String(filters.memberId));

    apiRequest<PageResponse<ReportSummaryDTO>>(`/api/reports?${params.toString()}`)
      .then(setData)
      .catch(() => toast.error("Couldn't load reports — check your connection and try again."));
    // `toast`'s identity changes every time a toast fires, which would re-trigger this
    // fetch in a loop on failure — deliberately excluded, refetch on page/filter change only.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, filters]);

  // Changing filters starts back at page 1 — staying on e.g. page 3 of a now much shorter
  // filtered result would silently show an empty page instead of the actual first results.
  const handleFiltersChange = (next: DashboardFilters) => {
    setFilters(next);
    setPage(1);
  };

  const columns: DataTableColumn<ReportSummaryDTO>[] = [
    {
      key: "week",
      header: "Week",
      // No longer a link — the explicit View action below is the only way to navigate from
      // this table now, so nothing else here should look or behave like it's clickable.
      render: (row) => `${row.weekStartDate} – ${row.weekEndDate}`,
    },
    { key: "member", header: "Member", render: (row) => row.userName },
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
          <ViewReportButton reportId={row.id} status={row.status} viewerRole="MANAGER" />
        </div>
      ),
    },
  ];

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-[28px] font-semibold text-ink">Reports</h1>

      <FilterBar filters={filters} onChange={handleFiltersChange} members={members} projects={projects} />

      {data && data.content.length === 0 ? (
        <EmptyState
          title="No reports match these filters"
          description="Try a different member, project, status, or week."
        />
      ) : (
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
      )}
    </div>
  );
}
