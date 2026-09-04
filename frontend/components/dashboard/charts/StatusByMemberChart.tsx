"use client";

import { useMemo } from "react";
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import ChartCard from "../ChartCard";
import { LINE_COLOR, STATUS_COLORS } from "@/lib/chartColors";
import { StatusByMemberDTO } from "@/lib/dashboardTypes";
import { ReportStatus } from "@/lib/reportTypes";
import { useDashboardData } from "@/lib/useDashboardData";

const STATUSES: ReportStatus[] = ["DRAFT", "SUBMITTED", "NEEDS_CORRECTION", "APPROVED"];
const STATUS_LABELS: Record<ReportStatus, string> = {
  DRAFT: "Draft",
  SUBMITTED: "Submitted",
  NEEDS_CORRECTION: "Needs correction",
  APPROVED: "Approved",
};

interface MemberStatusRow {
  userName: string;
  DRAFT: number;
  SUBMITTED: number;
  NEEDS_CORRECTION: number;
  APPROVED: number;
}

// The API returns one flat row per (member, status) pair with a count — Recharts needs one
// object per bar-group (member) with a numeric field per stacked series (status) instead.
// This is the one place that reshapes the API response; everything downstream just reads
// the pivoted rows.
function pivotByMember(rows: StatusByMemberDTO[]): MemberStatusRow[] {
  const byMember = new Map<string, MemberStatusRow>();
  for (const row of rows) {
    if (!byMember.has(row.userName)) {
      byMember.set(row.userName, {
        userName: row.userName,
        DRAFT: 0,
        SUBMITTED: 0,
        NEEDS_CORRECTION: 0,
        APPROVED: 0,
      });
    }
    byMember.get(row.userName)![row.status] = row.count;
  }
  return Array.from(byMember.values());
}

// GET /api/dashboard/charts/status-by-member — how many reports each member has in each
// status, stacked. This is the one chart where color is semantically meaningful (must match
// StatusBadge's colors for the same status), not decorative — each <Bar> below uses its
// matching STATUS_COLORS entry instead of Recharts' default palette.
export default function StatusByMemberChart() {
  const { data, status } = useDashboardData<StatusByMemberDTO>(
    "/api/dashboard/charts/status-by-member"
  );
  const rows = useMemo(() => pivotByMember(data), [data]);

  return (
    <ChartCard title="Report status by member" status={status} emptyTitle="No reports yet">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={rows} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
          <CartesianGrid stroke={LINE_COLOR} vertical={false} />
          <XAxis dataKey="userName" stroke={LINE_COLOR} tick={{ fontSize: 12 }} />
          <YAxis stroke={LINE_COLOR} tick={{ fontSize: 12 }} allowDecimals={false} />
          <Tooltip />
          <Legend wrapperStyle={{ fontSize: 12 }} />
          {STATUSES.map((s) => (
            <Bar key={s} dataKey={s} name={STATUS_LABELS[s]} stackId="status" fill={STATUS_COLORS[s]} />
          ))}
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
