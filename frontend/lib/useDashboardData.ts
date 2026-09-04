"use client";

import { useEffect, useState } from "react";
import { apiRequest } from "./apiClient";

export type FetchStatus = "loading" | "error" | "empty" | "ready";

// Shared by the 4 dashboard chart components — each hits a different endpoint but needs the
// identical loading -> ready/empty/error state machine. Extracted here once a fourth chart
// would have made it a fourth copy of the same 10-line effect.
//
// Each chart calling this independently (rather than one Promise.all at the page level
// feeding all 4 as props) is what actually satisfies "fetch in parallel, not sequentially":
// every chart's own effect fires on mount with no dependency on any other chart's data, so
// the browser dispatches all 4 (plus the summary and activity-feed requests) concurrently.
// It also gets "each chart handles its own loading/error state independently" for free —
// a centralized Promise.all would need Promise.allSettled plus per-chart state threaded
// back down as props to get the same isolation, for no benefit over just letting each
// component own its own request.
export function useDashboardData<T>(path: string): { data: T[]; status: FetchStatus } {
  const [data, setData] = useState<T[]>([]);
  const [status, setStatus] = useState<FetchStatus>("loading");

  useEffect(() => {
    apiRequest<T[]>(path)
      .then((result) => {
        setData(result);
        setStatus(result.length === 0 ? "empty" : "ready");
      })
      .catch(() => setStatus("error"));
  }, [path]);

  return { data, status };
}
