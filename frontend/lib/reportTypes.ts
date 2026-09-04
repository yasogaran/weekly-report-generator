// Mirrors the request/response DTOs documented in docs/api/api-doc.md. Field names are kept
// identical to the backend on purpose — the *FormData types below get JSON.stringify'd
// straight into a CreateReportRequest/UpdateReportRequest body with no translation layer, so
// a renamed field here would silently break the request instead of failing loudly.

export type TaskEntryType = "COMPLETED" | "PLANNED_NEXT_WEEK";

export type ReportStatus = "DRAFT" | "SUBMITTED" | "NEEDS_CORRECTION" | "APPROVED";

// --- Request-shape types (what the form edits, what gets sent to the API) ---

export interface TaskEntryFormData {
  taskName: string;
  priority: string;
  plannedPercent: number;
  actualPercent: number;
  status: string;
  timePlanned: number;
  timeSpent: number;
  deliverable: string;
  entryType: TaskEntryType;
}

export interface BlockerFormData {
  description: string;
  isKeyIssue: boolean;
}

export interface AchievementFormData {
  description: string;
  isKeyAchievement: boolean;
}

export interface HoursByTypeFormData {
  taskType: string;
  hours: number;
}

// Mirrors CreateReportRequest / UpdateReportRequest exactly. `projectId` is nullable here
// (not on the backend) because the form starts with nothing selected — validation blocks
// submission until it's set, so by the time this is serialized for a request it's a number.
export interface ReportFormData {
  projectId: number | null;
  weekStartDate: string;
  weekEndDate: string;
  notes: string;
  taskEntries: TaskEntryFormData[];
  blockers: BlockerFormData[];
  achievements: AchievementFormData[];
  hoursByType: HoursByTypeFormData[];
}

// --- Response-shape types (what GET /api/reports/{id} returns, used to prefill the form) ---

export interface ReportDTO {
  id: number;
  userId: number;
  userName: string;
  projectId: number;
  projectName: string;
  weekStartDate: string;
  weekEndDate: string;
  status: ReportStatus;
  versionNumber: number;
  parentReportId: number | null;
  notes: string;
  taskEntries: TaskEntryFormData[];
  blockers: BlockerFormData[];
  achievements: AchievementFormData[];
  hoursByType: HoursByTypeFormData[];
  createdAt: string;
  updatedAt: string;
  submittedAt: string | null;
}

// Lighter shape returned by the paginated list endpoint (GET /api/reports) — no nested
// task/blocker/achievement/hours lists, just enough for a history row.
export interface ReportSummaryDTO {
  id: number;
  userId: number;
  userName: string;
  projectName: string;
  weekStartDate: string;
  weekEndDate: string;
  status: ReportStatus;
  updatedAt: string;
}

export interface ReviewActionDTO {
  id: number;
  reviewerName: string;
  action: "APPROVED" | "REQUESTED_CHANGES";
  comment: string;
  createdAt: string;
}

// Builds a blank form from scratch (new report) or from an already-fetched ReportDTO
// (editing an existing one) — the one place that knows how to go from either starting
// point to the shape the form actually edits.
export function toFormData(report?: ReportDTO): ReportFormData {
  if (!report) {
    return {
      projectId: null,
      weekStartDate: "",
      weekEndDate: "",
      notes: "",
      taskEntries: [],
      blockers: [],
      achievements: [],
      hoursByType: [],
    };
  }
  return {
    projectId: report.projectId,
    weekStartDate: report.weekStartDate,
    weekEndDate: report.weekEndDate,
    notes: report.notes ?? "",
    taskEntries: report.taskEntries,
    blockers: report.blockers,
    achievements: report.achievements,
    hoursByType: report.hoursByType,
  };
}
