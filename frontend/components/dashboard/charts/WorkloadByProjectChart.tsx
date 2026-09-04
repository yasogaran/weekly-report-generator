"use client";

import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import ChartCard from "../ChartCard";
import { ACCENT_COLOR, LINE_COLOR } from "@/lib/chartColors";
import { WorkloadByProjectDTO } from "@/lib/dashboardTypes";
import { useDashboardData } from "@/lib/useDashboardData";

// GET /api/dashboard/charts/workload-by-project — task count per project, team-wide.
export default function WorkloadByProjectChart() {
  const { data, status } = useDashboardData<WorkloadByProjectDTO>(
    "/api/dashboard/charts/workload-by-project"
  );

  return (
    <ChartCard title="Workload by project" status={status} emptyTitle="No tasks logged yet">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
          <CartesianGrid stroke={LINE_COLOR} vertical={false} />
          <XAxis dataKey="projectName" stroke={LINE_COLOR} tick={{ fontSize: 12 }} />
          <YAxis stroke={LINE_COLOR} tick={{ fontSize: 12 }} allowDecimals={false} />
          <Tooltip />
          <Bar dataKey="taskCount" name="Tasks" fill={ACCENT_COLOR} radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
