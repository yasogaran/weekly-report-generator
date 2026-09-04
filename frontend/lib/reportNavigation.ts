import { UserRole } from "./authContext";
import { ActivityType } from "./dashboardTypes";
import { ReportStatus } from "./reportTypes";

/**
 * Where the "View" action on a report row should navigate — the single place this logic
 * lives, shared by every report list table (the manager queue, a team member's own history,
 * and the Team Member Profile page's history table once it exists) instead of each
 * reimplementing the same status/role check.
 *
 * - A team member never gets the review page for their own report, regardless of status —
 *   they aren't the reviewer, so there's nothing to review-act on even when it's SUBMITTED.
 * - A manager sees the same split the report queue already used: a SUBMITTED report goes to
 *   the review page (where they can act on it); anything else goes to the plain read-only
 *   detail page, which already renders an explanatory Alert instead of action buttons for a
 *   non-reviewable status (see review/[id]/page.tsx's NON_REVIEWABLE_MESSAGE).
 */
export function getReportViewDestination(
  report: { id: number; status: ReportStatus },
  viewerRole: UserRole
): string {
  if (viewerRole === "MANAGER" && report.status === "SUBMITTED") {
    return `/review/${report.id}`;
  }
  return `/reports/${report.id}/view`;
}

/**
 * Where an Activity Feed entry should link. Unlike reportRowHref, this doesn't know the
 * report's CURRENT status — only which event happened, which may be stale by the time
 * someone clicks it. A SUBMITTED event means the report was awaiting review at that moment,
 * so it routes to the review page — which is safe even if it's since been reviewed by
 * someone else, since that page's action bar is already gated on live status (fetched
 * fresh), not on this event. APPROVED and REQUESTED_CHANGES describe a decision that
 * already happened, so those go to the plain read-only detail page instead of the review
 * page, which would render the same content but under a misleading "awaiting your review"
 * framing for something already resolved.
 */
export function activityItemHref(reportId: number, activityType: ActivityType): string {
  return activityType === "SUBMITTED" ? `/review/${reportId}` : `/reports/${reportId}/view`;
}
