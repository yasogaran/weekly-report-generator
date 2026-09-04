"use client";

import { useEffect, useState } from "react";
import { useToast } from "@/components/ui/Toast";
import { ApiError, apiRequest } from "./apiClient";
import { ReportDTO } from "./reportTypes";

interface UseReportWithVersionsResult {
  report: ReportDTO | null;
  versions: ReportDTO[];
  notFound: boolean;
}

// Fetches a report by id plus its full version chain — the exact pair of requests both
// app/(member)/reports/[id]/view/page.tsx and app/(manager)/review/[id]/page.tsx need
// (GET /api/reports/{id} and GET /api/reports/{id}/versions, docs/api/api-doc.md). Extracted
// here once the manager review page needed the identical pattern the view page already had,
// rather than a second copy of the same fetch/loading/404 handling.
//
// Staleness on an id change (e.g. clicking a different version's link while already on this
// page) is handled by the caller comparing `report?.id === reportId` before rendering — this
// hook doesn't reset state synchronously when `reportId` changes, since a set-state call
// directly in an effect body (rather than inside its async callbacks) is exactly the
// derived-state-as-state anti-pattern React's rules flag.
export function useReportWithVersions(reportId: number): UseReportWithVersionsResult {
  const toast = useToast();
  const [report, setReport] = useState<ReportDTO | null>(null);
  const [versions, setVersions] = useState<ReportDTO[]>([]);
  const [notFoundId, setNotFoundId] = useState<number | null>(null);

  useEffect(() => {
    let cancelled = false;

    apiRequest<ReportDTO>(`/api/reports/${reportId}`)
      .then((data) => {
        if (!cancelled) setReport(data);
      })
      .catch((err) => {
        if (cancelled) return;
        // 404 deliberately covers both "doesn't exist" and "exists but isn't yours"
        // (api-doc.md) — nothing more specific to say to the user either way. A manager
        // hitting this at all would mean a report id that simply doesn't exist, since
        // managers aren't ownership-scoped.
        if (err instanceof ApiError && err.status === 404) {
          setNotFoundId(reportId);
        } else {
          toast.error("Couldn't load this report — check your connection and try again.");
        }
      });

    // Works from any id in the chain and returns the full chain, newest first
    // (api-doc.md) — so this always shows every version regardless of which one is open.
    apiRequest<ReportDTO[]>(`/api/reports/${reportId}/versions`)
      .then((data) => {
        if (!cancelled) setVersions(data);
      })
      .catch(() => {
        // Non-critical — the report itself still renders without the version list.
      });

    return () => {
      cancelled = true;
    };
    // `toast`'s identity changes every time a toast fires, which would re-trigger this
    // fetch in a loop on failure — deliberately excluded, refetch on id change only.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [reportId]);

  return { report, versions, notFound: notFoundId === reportId };
}
