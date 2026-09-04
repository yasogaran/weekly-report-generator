"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import ReportForm from "@/components/report/ReportForm";
import { useToast } from "@/components/ui/Toast";
import { ApiError, apiRequest } from "@/lib/apiClient";
import { ReportDTO } from "@/lib/reportTypes";

// "use client" (not the usual server-component default) because fetching the report needs
// apiClient, which reads the JWT from localStorage — browser-only, unavailable during any
// server-side render. This page's only job is: resolve the id from the URL, fetch that
// report once, and hand it to ReportForm as initial state; ReportForm owns everything after
// that, including the versioning-fork redirect if a save forks this report onto a new id.
export default function EditReportPage() {
  const params = useParams<{ id: string }>();
  const reportId = Number(params.id);
  const toast = useToast();

  const [report, setReport] = useState<ReportDTO | null>(null);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    let cancelled = false;

    apiRequest<ReportDTO>(`/api/reports/${reportId}`)
      .then((data) => {
        if (!cancelled) setReport(data);
      })
      .catch((err) => {
        if (cancelled) return;
        // 404 deliberately covers both "doesn't exist" and "exists but isn't yours"
        // (api-doc.md) — there's nothing more specific to say to the user either way.
        if (err instanceof ApiError && err.status === 404) {
          setNotFound(true);
        } else {
          toast.error("Couldn't load this report — check your connection and try again.");
        }
      });

    return () => {
      cancelled = true;
    };
    // `toast`'s identity changes every time a toast fires, which would re-trigger this
    // fetch in a loop on failure — deliberately excluded, fetch once per id only.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [reportId]);

  if (notFound) {
    return <p className="text-sm text-muted">This report doesn&apos;t exist, or isn&apos;t yours to edit.</p>;
  }

  if (!report) {
    return <p className="text-sm text-muted">Loading…</p>;
  }

  return <ReportForm mode="edit" reportId={reportId} initialReport={report} />;
}
