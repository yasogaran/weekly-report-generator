"use client";

import { useEffect, useState } from "react";
import { apiRequest } from "./apiClient";
import { ReviewActionDTO } from "./reportTypes";

// Fetches a report's review history and returns the comment from its most recent
// REQUESTED_CHANGES action (or null if there isn't one / it hasn't loaded yet). Both
// ReportForm (editing) and ReportDetail (read-only view) need this exact "needs correction"
// banner text, so it's extracted here rather than duplicated — this is the shared version;
// ReportForm previously had its own copy of this effect and now uses this hook instead.
export function useLatestReviewComment(
  reportId: number | undefined,
  enabled: boolean
): string | null {
  const [comment, setComment] = useState<string | null>(null);

  useEffect(() => {
    if (!enabled || !reportId) return;
    let cancelled = false;

    apiRequest<ReviewActionDTO[]>(`/api/reports/${reportId}/reviews`)
      .then((reviews) => {
        if (cancelled) return;
        const latest = reviews.find((review) => review.action === "REQUESTED_CHANGES");
        setComment(latest?.comment ?? null);
      })
      .catch(() => {
        // Non-critical — the page/form is still fully usable without the banner text.
      });

    return () => {
      cancelled = true;
    };
  }, [reportId, enabled]);

  return comment;
}
