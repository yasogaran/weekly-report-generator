"use client";

import { useEffect, useState } from "react";
import ActivityFeed from "@/components/dashboard/ActivityFeed";
import FilterBar, { DashboardFilters, EMPTY_FILTERS } from "@/components/dashboard/FilterBar";
import StatusByMemberChart from "@/components/dashboard/charts/StatusByMemberChart";
import TaskTrendChart from "@/components/dashboard/charts/TaskTrendChart";
import TimeByTypeChart from "@/components/dashboard/charts/TimeByTypeChart";
import WorkloadByProjectChart from "@/components/dashboard/charts/WorkloadByProjectChart";
import SummaryCard from "@/components/ui/SummaryCard";
import { PageResponse, apiRequest } from "@/lib/apiClient";
import { DashboardSummaryDTO } from "@/lib/dashboardTypes";
import { ProjectDTO } from "@/lib/projectTypes";
import { UserDTO } from "@/lib/userTypes";
import { FetchStatus } from "@/lib/useDashboardData";

// "use client": every fetch here needs apiClient (JWT from localStorage), and the FilterBar
// state below has to live somewhere that survives filter changes without a navigation.
export default function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummaryDTO | null>(null);
  const [summaryStatus, setSummaryStatus] = useState<FetchStatus>("loading");
  const [members, setMembers] = useState<UserDTO[]>([]);
  const [projects, setProjects] = useState<ProjectDTO[]>([]);
  const [filters, setFilters] = useState<DashboardFilters>(EMPTY_FILTERS);

  // Summary cards, the member/project dropdown options, and each of the 4 charts
  // (TaskTrendChart etc., which fetch inside themselves via useDashboardData) all fire
  // independently on mount — there's no data dependency between any of them, so this is
  // already "parallel, not sequential" without needing an explicit Promise.all: nothing
  // here awaits anything else before firing its own request.
  useEffect(() => {
    apiRequest<DashboardSummaryDTO>("/api/dashboard/summary")
      .then((data) => {
        setSummary(data);
        setSummaryStatus("ready");
      })
      .catch(() => setSummaryStatus("error"));

    apiRequest<PageResponse<UserDTO>>("/api/users?size=100")
      .then((page) => setMembers(page.content))
      .catch(() => {
        // Non-critical — the member filter dropdown just stays empty.
      });

    apiRequest<ProjectDTO[]>("/api/projects")
      .then(setProjects)
      .catch(() => {
        // Non-critical — the project filter dropdown just stays empty.
      });
  }, []);

  return (
    <div className="flex flex-col gap-8">
      <h1 className="text-[28px] font-semibold text-ink">Team dashboard</h1>

      <section className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {summaryStatus === "loading" && (
          <>
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="h-20 animate-pulse rounded border border-line bg-line/20" />
            ))}
          </>
        )}
        {summaryStatus === "error" && (
          <p className="col-span-full text-sm text-muted">
            Couldn&apos;t load summary metrics — check your connection and try again.
          </p>
        )}
        {summaryStatus === "ready" && summary && (
          <>
            <SummaryCard label="Submitted this week" value={String(summary.totalSubmittedThisWeek)} />
            <SummaryCard
              label="Compliance rate"
              value={`${Math.round(summary.complianceRate * 100)}%`}
            />
            <SummaryCard label="Needs correction" value={String(summary.needsCorrectionCount)} />
            <SummaryCard label="Open blockers" value={String(summary.openBlockersCount)} />
          </>
        )}
      </section>

      <FilterBar filters={filters} onChange={setFilters} members={members} projects={projects} />

      <section className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <TaskTrendChart />
        <StatusByMemberChart />
        <WorkloadByProjectChart />
        <TimeByTypeChart />
      </section>

      <section className="flex flex-col gap-3 rounded border border-line p-4">
        <h3 className="text-sm font-semibold text-ink">Activity feed</h3>
        <ActivityFeed filters={filters} members={members} />
      </section>
    </div>
  );
}
