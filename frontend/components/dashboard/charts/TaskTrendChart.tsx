"use client";

import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import ChartCard from "../ChartCard";
import { ACCENT_COLOR, LINE_COLOR } from "@/lib/chartColors";
import { TaskTrendPointDTO } from "@/lib/dashboardTypes";
import { useDashboardData } from "@/lib/useDashboardData";

// GET /api/dashboard/charts/task-trend — completed task count per week, team-wide.
export default function TaskTrendChart() {
  const { data, status } = useDashboardData<TaskTrendPointDTO>("/api/dashboard/charts/task-trend");

  return (
    <ChartCard title="Task completion trend" status={status} emptyTitle="No completed tasks yet">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
          <CartesianGrid stroke={LINE_COLOR} vertical={false} />
          <XAxis dataKey="weekStartDate" stroke={LINE_COLOR} tick={{ fontSize: 12 }} />
          <YAxis stroke={LINE_COLOR} tick={{ fontSize: 12 }} allowDecimals={false} />
          <Tooltip />
          <Line
            type="monotone"
            dataKey="completedTaskCount"
            name="Completed tasks"
            stroke={ACCENT_COLOR}
            strokeWidth={2}
            dot={{ r: 3 }}
          />
        </LineChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
