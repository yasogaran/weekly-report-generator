"use client";

import Button from "@/components/ui/Button";
import DatePicker from "@/components/ui/DatePicker";
import Select from "@/components/ui/Select";
import { ProjectDTO } from "@/lib/projectTypes";
import { ReportStatus } from "@/lib/reportTypes";
import { UserDTO } from "@/lib/userTypes";

export interface DashboardFilters {
  memberId: number | null;
  projectId: number | null;
  status: ReportStatus | null;
  weekStart: string;
  weekEnd: string;
}

export const EMPTY_FILTERS: DashboardFilters = {
  memberId: null,
  projectId: null,
  status: null,
  weekStart: "",
  weekEnd: "",
};

const STATUS_OPTIONS: { value: ReportStatus; label: string }[] = [
  { value: "DRAFT", label: "Draft" },
  { value: "SUBMITTED", label: "Submitted" },
  { value: "NEEDS_CORRECTION", label: "Needs correction" },
  { value: "APPROVED", label: "Approved" },
];

interface FilterBarProps {
  filters: DashboardFilters;
  onChange: (filters: DashboardFilters) => void;
  // Fetched once by the page (app/(manager)/dashboard/page.tsx) and passed down — ActivityFeed
  // needs this same member list too (to resolve the member filter's id to a name), so the
  // page owns the single fetch rather than this component and ActivityFeed each fetching
  // their own copy of GET /api/users.
  members: UserDTO[];
  projects: ProjectDTO[];
}

// SCOPE NOTE (read before wiring this up to anything new): the summary and chart endpoints
// this dashboard uses (GET /api/dashboard/summary and the 4 charts/* endpoints,
// docs/api/api-doc.md) don't accept any filter query params today — only `page`/`size` and
// a few filters exist on GET /api/reports, nothing on the dashboard aggregation endpoints.
// So these filters are real (backed by real data fetched here, genuinely wired to
// onChange), but they can only affect what this page can filter client-side: the Activity
// Feed (see ActivityFeed.tsx), which already has the full list in hand. The summary cards
// and all 4 charts intentionally ignore this state entirely — making them "respect" the
// filters would mean either faking it (silently wrong) or adding backend query params
// first, which is out of scope for this task.
//
// One filter can't even do that much: ActivityFeedItemDTO has no project field at all
// (docs/api/api-doc.md), so the project filter below has no effect on anything yet — it's
// built because the task asked for it to exist, not because it's functional today.
export default function FilterBar({ filters, onChange, members, projects }: FilterBarProps) {
  const setField = <K extends keyof DashboardFilters>(field: K, value: DashboardFilters[K]) => {
    onChange({ ...filters, [field]: value });
  };

  return (
    <div className="flex flex-wrap items-end gap-4">
      <label className="flex flex-col gap-1 text-sm font-medium text-ink">
        Member
        <Select
          value={filters.memberId ?? ""}
          onChange={(event) =>
            setField("memberId", event.target.value ? Number(event.target.value) : null)
          }
          options={[
            { value: "", label: "All members" },
            ...members.map((member) => ({ value: String(member.id), label: member.name })),
          ]}
          className="w-44"
        />
      </label>

      <label className="flex flex-col gap-1 text-sm font-medium text-ink">
        Project
        <Select
          value={filters.projectId ?? ""}
          onChange={(event) =>
            setField("projectId", event.target.value ? Number(event.target.value) : null)
          }
          options={[
            { value: "", label: "All projects" },
            ...projects.map((project) => ({ value: String(project.id), label: project.name })),
          ]}
          className="w-44"
        />
      </label>

      <label className="flex flex-col gap-1 text-sm font-medium text-ink">
        Status
        <Select
          value={filters.status ?? ""}
          onChange={(event) =>
            setField("status", event.target.value ? (event.target.value as ReportStatus) : null)
          }
          options={[{ value: "", label: "All statuses" }, ...STATUS_OPTIONS]}
          className="w-44"
        />
      </label>

      <label className="flex flex-col gap-1 text-sm font-medium text-ink">
        From
        <DatePicker
          value={filters.weekStart}
          onChange={(event) => setField("weekStart", event.target.value)}
        />
      </label>

      <label className="flex flex-col gap-1 text-sm font-medium text-ink">
        To
        <DatePicker
          value={filters.weekEnd}
          onChange={(event) => setField("weekEnd", event.target.value)}
        />
      </label>

      <Button variant="secondary" onClick={() => onChange(EMPTY_FILTERS)}>
        Clear filters
      </Button>
    </div>
  );
}
