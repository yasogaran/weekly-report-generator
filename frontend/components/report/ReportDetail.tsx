"use client";

import { Flag } from "lucide-react";
import Alert from "@/components/ui/Alert";
import StatusBadge from "@/components/ui/StatusBadge";
import {
  AchievementFormData,
  BlockerFormData,
  HoursByTypeFormData,
  ReportDTO,
  TaskEntryFormData,
} from "@/lib/reportTypes";
import { useLatestReviewComment } from "@/lib/useLatestReviewComment";

interface ReportDetailProps {
  report: ReportDTO;
}

// Read-only render of everything in a ReportDTO. Used by both the team member's own detail
// page (app/(member)/reports/[id]/view) and the manager review page — deliberately has no
// "your report" wording, no onChange props, and no assumption about who's looking at it or
// what they're allowed to do next (an Edit button, an Approve action, etc. are the calling
// page's job to add around this, not this component's).
export default function ReportDetail({ report }: ReportDetailProps) {
  // Same shared hook ReportForm uses — see lib/useLatestReviewComment.ts. Only fetches when
  // the report is actually NEEDS_CORRECTION; harmless (and skipped) otherwise.
  const correctionComment = useLatestReviewComment(
    report.id,
    report.status === "NEEDS_CORRECTION"
  );

  const completedTasks = report.taskEntries.filter((task) => task.entryType === "COMPLETED");
  const plannedTasks = report.taskEntries.filter((task) => task.entryType === "PLANNED_NEXT_WEEK");

  return (
    <div className="flex flex-col gap-8">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-[28px] font-semibold text-ink">
            {report.weekStartDate} – {report.weekEndDate}
          </h1>
          <p className="text-sm text-muted">{report.projectName}</p>
        </div>
        <StatusBadge status={report.status} />
      </header>

      {correctionComment && (
        <Alert variant="warning" title="Needs correction">
          {correctionComment}
        </Alert>
      )}

      <ReadOnlyTaskTable heading="Tasks completed" rows={completedTasks} />
      <ReadOnlyTaskTable heading="Planned next week" rows={plannedTasks} />

      <ReadOnlyBlockers rows={report.blockers} />
      <ReadOnlyAchievements rows={report.achievements} />
      <ReadOnlyHoursByType rows={report.hoursByType} />

      {report.notes && (
        <section className="flex flex-col gap-2">
          <h3 className="text-sm font-semibold text-ink">Notes</h3>
          <p className="whitespace-pre-wrap text-sm text-ink">{report.notes}</p>
        </section>
      )}
    </div>
  );
}

// Read-only counterpart to components/ui/TaskTable.tsx — same columns and the same
// COMPLETED/PLANNED_NEXT_WEEK split, but plain cells instead of inputs. Kept local to this
// file (not a shared components/ui/ export) since nothing else needs an uneditable task
// table yet — extract it if a third caller shows up.
function ReadOnlyTaskTable({ heading, rows }: { heading: string; rows: TaskEntryFormData[] }) {
  if (rows.length === 0) {
    return (
      <div className="flex flex-col gap-3">
        <h3 className="text-sm font-semibold text-ink">{heading}</h3>
        <p className="text-sm text-muted">No entries.</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-3">
      <h3 className="text-sm font-semibold text-ink">{heading}</h3>
      <div className="overflow-x-auto rounded border border-line">
        <table className="w-full min-w-[900px] border-collapse text-sm">
          <thead>
            <tr className="border-b border-line bg-paper text-left text-xs font-medium text-muted">
              <th className="px-3 py-2">Task</th>
              <th className="px-3 py-2">Priority</th>
              <th className="px-3 py-2">Status</th>
              <th className="px-3 py-2 text-right">Plan %</th>
              <th className="px-3 py-2 text-right">Actual %</th>
              <th className="px-3 py-2 text-right">Hrs planned</th>
              <th className="px-3 py-2 text-right">Hrs spent</th>
              <th className="px-3 py-2">Deliverable</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row, index) => (
              <tr key={index} className="border-b border-line last:border-b-0">
                <td className="px-3 py-2 text-ink">{row.taskName}</td>
                <td className="px-3 py-2 text-ink">{row.priority}</td>
                <td className="px-3 py-2 text-ink">{row.status}</td>
                <td className="px-3 py-2 text-right font-mono text-ink">{row.plannedPercent}%</td>
                <td className="px-3 py-2 text-right font-mono text-ink">{row.actualPercent}%</td>
                <td className="px-3 py-2 text-right font-mono text-ink">{row.timePlanned}</td>
                <td className="px-3 py-2 text-right font-mono text-ink">{row.timeSpent}</td>
                <td className="px-3 py-2 text-ink">{row.deliverable}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function ReadOnlyBlockers({ rows }: { rows: BlockerFormData[] }) {
  return (
    <section className="flex flex-col gap-3">
      <h3 className="text-sm font-semibold text-ink">Blockers</h3>
      {rows.length === 0 ? (
        <p className="text-sm text-muted">No blockers reported.</p>
      ) : (
        <ul className="flex flex-col gap-2">
          {rows.map((row, index) => (
            <li
              key={index}
              className={`flex items-start gap-2 rounded border px-3 py-2 text-sm ${
                row.isKeyIssue ? "border-accent bg-accent/5" : "border-line"
              }`}
            >
              <Flag
                size={16}
                className={`mt-0.5 shrink-0 ${row.isKeyIssue ? "text-accent" : "text-muted"}`}
                fill={row.isKeyIssue ? "currentColor" : "none"}
              />
              <div>
                {row.isKeyIssue && (
                  <p className="text-xs font-medium text-accent">Key blocker</p>
                )}
                <p className="text-ink">{row.description}</p>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

function ReadOnlyAchievements({ rows }: { rows: AchievementFormData[] }) {
  return (
    <section className="flex flex-col gap-3">
      <h3 className="text-sm font-semibold text-ink">Achievements</h3>
      {rows.length === 0 ? (
        <p className="text-sm text-muted">No achievements reported.</p>
      ) : (
        <ul className="flex flex-col gap-2">
          {rows.map((row, index) => (
            <li
              key={index}
              className={`flex items-start gap-2 rounded border px-3 py-2 text-sm ${
                row.isKeyAchievement ? "border-accent bg-accent/5" : "border-line"
              }`}
            >
              <Flag
                size={16}
                className={`mt-0.5 shrink-0 ${row.isKeyAchievement ? "text-accent" : "text-muted"}`}
                fill={row.isKeyAchievement ? "currentColor" : "none"}
              />
              <div>
                {row.isKeyAchievement && (
                  <p className="text-xs font-medium text-accent">Key achievement</p>
                )}
                <p className="text-ink">{row.description}</p>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

function ReadOnlyHoursByType({ rows }: { rows: HoursByTypeFormData[] }) {
  if (rows.length === 0) return null;

  return (
    <section className="flex flex-col gap-3">
      <h3 className="text-sm font-semibold text-ink">Hours by type</h3>
      <table className="w-full max-w-sm border-collapse text-sm">
        <tbody>
          {rows.map((row, index) => (
            <tr key={index} className="border-b border-line last:border-b-0">
              <td className="py-1.5 text-ink">{row.taskType}</td>
              <td className="py-1.5 text-right font-mono text-ink">{row.hours}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
