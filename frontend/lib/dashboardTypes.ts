// Mirrors the Dashboard endpoints in docs/api/api-doc.md exactly — all MANAGER-only, no
// exceptions. Field names match the backend one-for-one, same convention as reportTypes.ts.

export interface DashboardSummaryDTO {
  totalSubmittedThisWeek: number;
  complianceRate: number;
  needsCorrectionCount: number;
  openBlockersCount: number;
}

export interface TaskTrendPointDTO {
  weekStartDate: string;
  completedTaskCount: number;
}

export interface StatusByMemberDTO {
  userId: number;
  userName: string;
  status: "DRAFT" | "SUBMITTED" | "NEEDS_CORRECTION" | "APPROVED";
  count: number;
}

export interface WorkloadByProjectDTO {
  projectId: number;
  projectName: string;
  taskCount: number;
}

export interface TimeByTypeDTO {
  taskType: string;
  totalHours: number;
}

export type ActivityType = "SUBMITTED" | "APPROVED" | "REQUESTED_CHANGES";

export interface ActivityFeedItemDTO {
  type: ActivityType;
  reportId: number;
  userName: string;
  timestamp: string;
  detail: string;
}
