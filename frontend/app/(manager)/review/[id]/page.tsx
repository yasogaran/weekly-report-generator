"use client";

import { useState } from "react";
import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { useParams, useRouter } from "next/navigation";
import Alert from "@/components/ui/Alert";
import Button from "@/components/ui/Button";
import ReportDetail from "@/components/report/ReportDetail";
import Textarea from "@/components/ui/Textarea";
import VersionHistoryList from "@/components/report/VersionHistoryList";
import { useConfirm } from "@/components/ui/ConfirmDialog";
import { useToast } from "@/components/ui/Toast";
import { ApiError, apiRequest } from "@/lib/apiClient";
import { ReportDTO, ReportStatus } from "@/lib/reportTypes";
import { useReportWithVersions } from "@/lib/useReportWithVersions";

type ReviewAction = "APPROVED" | "REQUESTED_CHANGES";

// Turns a failed POST /api/reports/{id}/reviews into copy a manager can act on. 409
// specifically means the state machine rejected this because the report isn't SUBMITTED
// anymore (api-doc.md) — the most likely real-world cause is another manager reviewed it
// moments ago, so say that rather than a generic "something went wrong."
function reviewErrorMessage(err: unknown): string {
  if (err instanceof ApiError) {
    if (err.status === 409) {
      return "This report isn't awaiting review anymore — someone may have already reviewed it. Refresh to see its current status.";
    }
    return err.message;
  }
  return "Couldn't submit this review — check your connection and try again.";
}

// The manager report queue (app/(manager)/reports/page.tsx) — after acting on one report,
// back to the list of others that may still need attention.
const AFTER_REVIEW_REDIRECT = "/reports";

// Shown instead of the action bar when the report isn't SUBMITTED — explains why, specific
// to the actual status, rather than just leaving that area of the page blank.
const NON_REVIEWABLE_MESSAGE: Record<Exclude<ReportStatus, "SUBMITTED">, string> = {
  DRAFT: "This report is still a draft — nothing to review yet.",
  NEEDS_CORRECTION: "This report is waiting for the team member to make corrections and resubmit.",
  APPROVED: "This report has already been approved. No further action is needed.",
};

export default function ManagerReviewPage() {
  const params = useParams<{ id: string }>();
  const reportId = Number(params.id);
  const router = useRouter();
  const toast = useToast();
  const confirm = useConfirm();

  const { report, versions, notFound } = useReportWithVersions(reportId);

  const [requestChangesOpen, setRequestChangesOpen] = useState(false);
  const [comment, setComment] = useState("");
  // Tracks which action is in flight so both buttons/the panel can disable together without
  // needing three separate booleans.
  const [submittingAction, setSubmittingAction] = useState<ReviewAction | null>(null);

  const submitReview = async (action: ReviewAction, reviewComment?: string) => {
    setSubmittingAction(action);
    try {
      await apiRequest<ReportDTO>(`/api/reports/${reportId}/reviews`, {
        method: "POST",
        body: JSON.stringify({ action, comment: reviewComment }),
      });
      toast.success(action === "APPROVED" ? "Report approved" : "Sent back for correction");
      router.push(AFTER_REVIEW_REDIRECT);
    } catch (err) {
      toast.error(reviewErrorMessage(err));
    } finally {
      setSubmittingAction(null);
    }
  };

  const handleApprove = async () => {
    // Approve goes through ConfirmDialog — unlike Request Changes, there's no other
    // built-in pause here (no required comment to type), so this is the deliberate-pause
    // step for this action (frontend-design-system.md §2).
    const confirmed = await confirm({
      title: "Approve this report?",
      description: "The team member will be notified and no further edits will be expected.",
      variant: "primary",
      confirmLabel: "Approve",
    });
    if (!confirmed) return;
    // Comment is optional for APPROVED (api-doc.md — only required "if REQUESTED_CHANGES"),
    // so it's simply omitted rather than sent as an empty string.
    await submitReview("APPROVED");
  };

  const handleSendRequestedChanges = async () => {
    if (!comment.trim()) return;
    await submitReview("REQUESTED_CHANGES", comment);
  };

  if (notFound) {
    return <p className="text-sm text-muted">This report doesn&apos;t exist.</p>;
  }

  if (!report || report.id !== reportId) {
    return <p className="text-sm text-muted">Loading…</p>;
  }

  // Only SUBMITTED reports can actually be reviewed — the state machine 409s on anything
  // else (api-doc.md). A manager can still land here for a DRAFT/NEEDS_CORRECTION/APPROVED
  // report (e.g. via a version history link), so rather than offering Approve/Request
  // Changes buttons that are already known to fail, this area explains why instead — see
  // NON_REVIEWABLE_MESSAGE below.
  const isReviewable = report.status === "SUBMITTED";

  return (
    <div className="mx-auto flex max-w-4xl flex-col gap-8 pb-12">
      <Link href="/reports" className="inline-flex w-fit">
        <Button variant="secondary">
          <ArrowLeft size={16} />
          Back to reports
        </Button>
      </Link>

      <ReportDetail report={report} />

      <VersionHistoryList versions={versions} currentReportId={report.id} />

      {!isReviewable && (
        <div className="border-t border-line pt-6">
          <Alert variant="info">
            {NON_REVIEWABLE_MESSAGE[report.status as Exclude<ReportStatus, "SUBMITTED">]}
          </Alert>
        </div>
      )}

      {isReviewable && (
        <div className="flex flex-col gap-4 border-t border-line pt-6">
          {requestChangesOpen ? (
            <div className="flex flex-col gap-3 rounded border border-line bg-surface p-4">
              <label className="flex flex-col gap-1 text-sm font-medium text-ink">
                Comment
                <Textarea
                  value={comment}
                  onChange={(event) => setComment(event.target.value)}
                  placeholder="Explain what needs to change before this can be approved"
                  autoFocus
                />
              </label>
              <div className="flex justify-end gap-3">
                <Button
                  variant="secondary"
                  onClick={() => {
                    setRequestChangesOpen(false);
                    setComment("");
                  }}
                  disabled={submittingAction !== null}
                >
                  Cancel
                </Button>
                <Button
                  variant="primary"
                  onClick={handleSendRequestedChanges}
                  // Mirrors the backend's whitespace-only rejection (api-doc.md) — no point
                  // letting someone submit a request that's guaranteed to 400.
                  disabled={comment.trim().length === 0 || submittingAction !== null}
                >
                  {submittingAction === "REQUESTED_CHANGES" ? "Sending…" : "Send back for correction"}
                </Button>
              </div>
            </div>
          ) : (
            <div className="flex justify-end gap-3">
              <Button
                variant="secondary"
                onClick={() => setRequestChangesOpen(true)}
                disabled={submittingAction !== null}
              >
                Request changes
              </Button>
              <Button variant="primary" onClick={handleApprove} disabled={submittingAction !== null}>
                {submittingAction === "APPROVED" ? "Approving…" : "Approve"}
              </Button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
