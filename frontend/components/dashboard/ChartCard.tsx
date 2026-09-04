import { ReactNode } from "react";
import EmptyState from "@/components/ui/EmptyState";

export type ChartStatus = "loading" | "error" | "empty" | "ready";

interface ChartCardProps {
  title: string;
  status: ChartStatus;
  emptyTitle?: string;
  children: ReactNode;
}

// Shared chrome for the 4 chart cards — title + a fixed-height body that shows a loading
// skeleton, an error message, an EmptyState, or the actual chart, depending on `status`.
// Each chart component owns its own fetch/loading/error state independently (see
// TaskTrendChart.tsx etc.) and just tells this what to render; pulling the identical
// border/padding/title markup out here once four charts needed it avoids four copies of it.
export default function ChartCard({ title, status, emptyTitle, children }: ChartCardProps) {
  return (
    <div className="flex flex-col gap-3 rounded border border-line p-4">
      <h3 className="text-sm font-semibold text-ink">{title}</h3>
      <div className="h-64">
        {status === "loading" && <div className="h-full animate-pulse rounded bg-line/40" />}
        {status === "error" && (
          <div className="flex h-full items-center justify-center text-center text-sm text-muted">
            Couldn&apos;t load this chart — check your connection and try again.
          </div>
        )}
        {status === "empty" && (
          <div className="flex h-full items-center justify-center">
            <EmptyState title={emptyTitle ?? "No data yet"} />
          </div>
        )}
        {status === "ready" && children}
      </div>
    </div>
  );
}
