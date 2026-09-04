"use client";

import { useMemo } from "react";
import Link from "next/link";
import EmptyState from "@/components/ui/EmptyState";
import { DashboardFilters } from "./FilterBar";
import { ActivityFeedItemDTO, ActivityType } from "@/lib/dashboardTypes";
import { FEEDBACK_COLORS } from "@/lib/chartColors";
import { formatRelativeTime } from "@/lib/formatRelativeTime";
import { activityItemHref } from "@/lib/reportNavigation";
import { ReportStatus } from "@/lib/reportTypes";
import { UserDTO } from "@/lib/userTypes";
import { useDashboardData } from "@/lib/useDashboardData";

// ActivityFeedItemDTO's `type` only has 3 values, one short of the 4 ReportStatus values —
// there's no "went to draft" event, since drafts aren't reviewed. Selecting the "Draft"
// status filter is therefore expected to show zero items (nothing maps to it), not a bug.
const STATUS_TO_ACTIVITY_TYPE: Partial<Record<ReportStatus, ActivityType>> = {
  SUBMITTED: "SUBMITTED",
  APPROVED: "APPROVED",
  NEEDS_CORRECTION: "REQUESTED_CHANGES",
};

// Reuses feedback colors (not status colors) since these map more naturally to "good news /
// needs attention" than to the 4 report-status colors — SUBMITTED reads as informational,
// same hue as the Submitted status badge; APPROVED/REQUESTED_CHANGES reuse
// success/warning respectively, matching how Alert/Toast already use those hues.
const ACTIVITY_DOT_COLOR: Record<ActivityType, string> = {
  SUBMITTED: FEEDBACK_COLORS.info,
  APPROVED: FEEDBACK_COLORS.success,
  REQUESTED_CHANGES: FEEDBACK_COLORS.warning,
};

interface ActivityFeedProps {
  filters: DashboardFilters;
  members: UserDTO[];
}

// GET /api/dashboard/activity-feed. This is the one dashboard section the FilterBar's
// member/status/date filters actually apply to — see the scope note in FilterBar.tsx for
// why the summary cards and charts don't. Filtering happens client-side here since the feed
// endpoint has no filter query params and this component already has the full list loaded.
export default function ActivityFeed({ filters, members }: ActivityFeedProps) {
  const { data, status } = useDashboardData<ActivityFeedItemDTO>("/api/dashboard/activity-feed");

  const filtered = useMemo(() => {
    // The feed only carries `userName`, not a member id (api-doc.md), so the member filter
    // (chosen by id, to survive two members sharing a name in the dropdown) has to be
    // resolved to a name here to compare against. If the name lookup fails for any reason,
    // the filter fails safe (matches nothing) rather than silently ignoring the filter.
    const selectedMemberName = filters.memberId
      ? members.find((m) => m.id === filters.memberId)?.name
      : null;

    return data.filter((item) => {
      if (selectedMemberName && item.userName !== selectedMemberName) return false;
      if (filters.status && item.type !== STATUS_TO_ACTIVITY_TYPE[filters.status]) return false;
      if (filters.weekStart && item.timestamp < filters.weekStart) return false;
      if (filters.weekEnd && item.timestamp > `${filters.weekEnd}T23:59:59`) return false;
      // Project filter intentionally not applied here — ActivityFeedItemDTO carries no
      // project field to filter on (see FilterBar.tsx's scope note).
      return true;
    });
  }, [data, filters, members]);

  if (status === "loading") {
    return <div className="h-48 animate-pulse rounded bg-line/40" />;
  }

  if (status === "error") {
    return (
      <p className="text-sm text-muted">
        Couldn&apos;t load the activity feed — check your connection and try again.
      </p>
    );
  }

  if (status === "empty") {
    return <EmptyState title="No activity yet" description="Reviews and submissions will show up here." />;
  }

  if (filtered.length === 0) {
    return <p className="text-sm text-muted">No activity matches the current filters.</p>;
  }

  return (
    <ul className="flex flex-col gap-3">
      {filtered.map((item, index) => (
        <li key={index}>
          <Link
            href={activityItemHref(item.reportId, item.type)}
            className="flex items-start gap-3 rounded text-sm hover:bg-line/40"
          >
            <span
              className="mt-1.5 h-2 w-2 shrink-0 rounded-full"
              style={{ backgroundColor: ACTIVITY_DOT_COLOR[item.type] }}
              aria-hidden="true"
            />
            <div>
              <p className="text-ink">
                <span className="font-medium">{item.userName}</span> {item.detail}
              </p>
              <p className="text-xs text-muted">{formatRelativeTime(item.timestamp)}</p>
            </div>
          </Link>
        </li>
      ))}
    </ul>
  );
}
