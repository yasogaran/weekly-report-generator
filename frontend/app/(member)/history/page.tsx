"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Inbox } from "lucide-react";
import FilterBar, { DashboardFilters, EMPTY_FILTERS } from "@/components/dashboard/FilterBar";
import ViewReportButton from "@/components/report/ViewReportButton";
import DataTable, { DataTableColumn } from "@/components/ui/DataTable";
import EmptyState from "@/components/ui/EmptyState";
import Select from "@/components/ui/Select";
import StatusBadge from "@/components/ui/StatusBadge";
import { useToast } from "@/components/ui/Toast";
import { PageResponse, apiRequest } from "@/lib/apiClient";
import { ProjectDTO } from "@/lib/projectTypes";
import { ReportSummaryDTO } from "@/lib/reportTypes";

const PAGE_SIZE = 10;

// Maps directly to Spring's Pageable sort param format ("property,direction") — verified
// against the live backend (GET /api/reports honors `sort` automatically, since
// ReportRepository's findAll(spec, pageable) goes through JpaSpecificationExecutor, which
// Spring Data wires up with full Pageable support including sort — this wasn't assumed).
const SORT_OPTIONS = [
  { value: "updatedAt,desc", label: "Last updated (newest first)" },
  { value: "updatedAt,asc", label: "Last updated (oldest first)" },
  { value: "weekStartDate,desc", label: "Week start (newest first)" },
  { value: "weekStartDate,asc", label: "Week start (oldest first)" },
];
const DEFAULT_SORT = "updatedAt,desc";

// "use client": needs apiClient (localStorage token), and owns filter/sort/pagination state
// that changes on click without a full navigation.
export default function ReportHistoryPage() {
  const router = useRouter();
  const toast = useToast();
  // DataTable's pagination footer is 1-indexed ("Page 1 of N"); Spring's `page` query param
  // is 0-indexed. This component only ever deals in the 1-indexed version and converts at
  // the one place it calls the API.
  const [page, setPage] = useState(1);
  const [filters, setFilters] = useState<DashboardFilters>(EMPTY_FILTERS);
  const [sort, setSort] = useState(DEFAULT_SORT);
  const [data, setData] = useState<PageResponse<ReportSummaryDTO> | null>(null);
  const [projects, setProjects] = useState<ProjectDTO[]>([]);

  // Project filter's dropdown options. Deliberately NOT also fetching GET /api/users here
  // the way the manager report queue does for its Member filter — that endpoint is
  // MANAGER-only server-side (UserController's class-level @PreAuthorize), and this page
  // never shows a Member filter in the first place (FilterBar's showMemberFilter={false}
  // below), so there's nothing that would ever use that data.
  useEffect(() => {
    apiRequest<ProjectDTO[]>("/api/projects")
      .then(setProjects)
      .catch(() => {
        // Non-critical — the project filter dropdown just stays empty.
      });
  }, []);

  // Same query param names as app/(manager)/reports/page.tsx, checked against the actual
  // controller/specification (ReportController.listReports, ReportSpecifications.build) —
  // not re-derived from docs/api/api-doc.md's vaguer "week" mention. No memberId here: a
  // team member's reports are already scoped server-side to themselves regardless of what's
  // passed (api-doc.md — "silently overridden to the caller's own id"), so there's nothing
  // for this page to send even if it wanted to.
  useEffect(() => {
    const params = new URLSearchParams();
    params.set("page", String(page - 1));
    params.set("size", String(PAGE_SIZE));
    params.set("sort", sort);
    if (filters.projectId) params.set("projectId", String(filters.projectId));
    if (filters.status) params.set("status", filters.status);
    if (filters.weekStart) params.set("week", filters.weekStart);

    apiRequest<PageResponse<ReportSummaryDTO>>(`/api/reports?${params.toString()}`)
      .then(setData)
      .catch(() => toast.error("Couldn't load your reports — check your connection and try again."));
    // `toast`'s identity changes every time a toast fires, which would re-trigger this
    // fetch in a loop on failure — deliberately excluded, refetch on page/filter/sort change only.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, filters, sort]);

  // Changing a filter or the sort starts back at page 1 — staying on e.g. page 3 of a now
  // much shorter (or differently ordered) result would silently show an empty/wrong page
  // instead of the actual first results.
  const handleFiltersChange = (next: DashboardFilters) => {
    setFilters(next);
    setPage(1);
  };

  const handleSortChange = (next: string) => {
    setSort(next);
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

  // Distinguishes "you have zero reports, period" from "zero reports match what you've
  // filtered for" — very different messages, and conflating them would tell a first-time
  // user to "try clearing filters" they never set, or tell a filtered-out user to go create
  // a report they may already have several of.
  const areFiltersActive =
    filters.projectId !== null || filters.status !== null || filters.weekStart !== "" || filters.weekEnd !== "";

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-[28px] font-semibold text-ink">Report history</h1>

      <div className="flex flex-wrap items-end justify-between gap-4">
        <FilterBar
          filters={filters}
          onChange={handleFiltersChange}
          members={[]}
          projects={projects}
          showMemberFilter={false}
        />
        <label className="flex flex-col gap-1 text-sm font-medium text-ink">
          Sort by
          <Select
            value={sort}
            onChange={(event) => handleSortChange(event.target.value)}
            options={SORT_OPTIONS}
            className="w-56"
          />
        </label>
      </div>

      {data && data.content.length === 0 ? (
        areFiltersActive ? (
          <EmptyState
            title="No reports match your filters"
            description="Try clearing them to see your full report history."
            actionLabel="Clear filters"
            onAction={() => handleFiltersChange(EMPTY_FILTERS)}
          />
        ) : (
          <EmptyState
            icon={<Inbox size={32} />}
            title="No reports yet"
            description="Create your first weekly report to get started."
            actionLabel="Create report"
            onAction={() => router.push("/reports/new")}
          />
        )
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
