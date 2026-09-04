"use client";

import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import ChartCard from "../ChartCard";
import { ACCENT_COLOR, LINE_COLOR } from "@/lib/chartColors";
import { TimeByTypeDTO } from "@/lib/dashboardTypes";
import { useDashboardData } from "@/lib/useDashboardData";

// GET /api/dashboard/charts/time-by-type — total hours per task type, team-wide. Built as a
// bar chart rather than a pie: a pie needs one distinct color per slice, and the design
// tokens (tailwind.config.ts) only define a handful of named colors, not an N-color
// categorical palette — a bar chart needing just one series color (accent) stays within
// "no hardcoded hex colors" without inventing palette entries that aren't real tokens.
export default function TimeByTypeChart() {
  const { data, status } = useDashboardData<TimeByTypeDTO>("/api/dashboard/charts/time-by-type");

  return (
    <ChartCard title="Hours by task type" status={status} emptyTitle="No hours logged yet">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
          <CartesianGrid stroke={LINE_COLOR} vertical={false} />
          <XAxis dataKey="taskType" stroke={LINE_COLOR} tick={{ fontSize: 12 }} />
          <YAxis stroke={LINE_COLOR} tick={{ fontSize: 12 }} />
          <Tooltip />
          <Bar dataKey="totalHours" name="Hours" fill={ACCENT_COLOR} radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
