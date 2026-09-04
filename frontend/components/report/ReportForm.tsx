"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Plus } from "lucide-react";
import Alert from "@/components/ui/Alert";
import AchievementsList from "@/components/ui/AchievementsList";
import BlockersList from "@/components/ui/BlockersList";
import Button from "@/components/ui/Button";
import DatePicker from "@/components/ui/DatePicker";
import HoursByTypeSection from "@/components/ui/HoursByTypeSection";
import Select from "@/components/ui/Select";
import TaskEntryCard from "@/components/ui/TaskEntryCard";
import Textarea from "@/components/ui/Textarea";
import { useConfirm } from "@/components/ui/ConfirmDialog";
import { useToast } from "@/components/ui/Toast";
import { ApiError, apiRequest } from "@/lib/apiClient";
import { ProjectDTO } from "@/lib/projectTypes";
import { ReportDTO, ReportFormData, TaskEntryFormData, TaskEntryType, toFormData } from "@/lib/reportTypes";
import { useLatestReviewComment } from "@/lib/useLatestReviewComment";

interface ReportFormProps {
  mode: "create" | "edit";
  /** Only present in edit mode — the id currently in the URL. */
  reportId?: number;
  /** Only present in edit mode — already fetched by the page before this mounts. */
  initialReport?: ReportDTO;
}

// Fields the backend actually accepts on POST /api/reports and PATCH /api/reports/{id}
// (CreateReportRequest / UpdateReportRequest, docs/api/api-doc.md) — status/versionNumber/
// etc. are server-assigned and never sent.
function toRequestBody(data: ReportFormData) {
  return {
    projectId: data.projectId,
    weekStartDate: data.weekStartDate,
    weekEndDate: data.weekEndDate,
    notes: data.notes,
    taskEntries: data.taskEntries,
    blockers: data.blockers,
    achievements: data.achievements,
    hoursByType: data.hoursByType,
  };
}

// Required-field checks only — this is UX (fast feedback, avoid a round trip for an obvious
// miss), not the real gate. The backend re-validates everything with @Valid on
// CreateReportRequest/UpdateReportRequest and that's the check that actually matters.
function validate(data: ReportFormData): string | null {
  if (!data.projectId) return "Select a project.";
  if (!data.weekStartDate || !data.weekEndDate) return "Enter both a week start and end date.";
  if (data.weekEndDate < data.weekStartDate) return "Week end date can't be before the start date.";
  return null;
}

// Shared by app/(member)/reports/new/page.tsx and app/(member)/reports/[id]/edit/page.tsx.
// Owns all form state as one object (a single setField updater, not a useState per field)
// so save/submit always serialize one consistent snapshot instead of racing field updates.
export default function ReportForm({ mode, reportId, initialReport }: ReportFormProps) {
  const router = useRouter();
  const toast = useToast();
  const confirm = useConfirm();

  const [formData, setFormData] = useState<ReportFormData>(() => toFormData(initialReport));
  const [projects, setProjects] = useState<ProjectDTO[]>([]);
  // Shared with ReportDetail.tsx (the read-only view) — extracted into this hook once both
  // needed the exact same "fetch review history, find the latest REQUESTED_CHANGES comment"
  // logic, rather than each keeping its own copy of the effect.
  const correctionComment = useLatestReviewComment(
    reportId,
    mode === "edit" && initialReport?.status === "NEEDS_CORRECTION"
  );
  // Tracks which action is in flight (not just a boolean) so the button that wasn't
  // clicked doesn't also relabel itself while the other one is saving.
  const [savingAction, setSavingAction] = useState<"draft" | "submit" | null>(null);

  const setField = <K extends keyof ReportFormData>(field: K, value: ReportFormData[K]) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  // The project dropdown's options — every authenticated user can read this list
  // (GET /api/projects, api-doc.md), it's just scoped to active projects only.
  useEffect(() => {
    apiRequest<ProjectDTO[]>("/api/projects")
      .then(setProjects)
      .catch(() => toast.error("Couldn't load projects — check your connection and try again."));
    // eslint-disable-next-line react-hooks/exhaustive-deps -- fetch once on mount; toast identity isn't relevant here.
  }, []);

  // Posts/patches the current form state and returns whatever the backend actually stored.
  const saveReport = (): Promise<ReportDTO> => {
    const body = JSON.stringify(toRequestBody(formData));
    return mode === "create"
      ? apiRequest<ReportDTO>("/api/reports", { method: "POST", body })
      : apiRequest<ReportDTO>(`/api/reports/${reportId}`, { method: "PATCH", body });
  };

  // CRITICAL — versioning fork: editing a report that's currently NEEDS_CORRECTION doesn't
  // patch that row in place. Per PATCH /api/reports/{id} (docs/api/api-doc.md), the backend
  // creates a brand-new Report row (new id, versionNumber + 1, parentReportId pointing at
  // the original) and freezes the original from that moment on — it's now a read-only past
  // version, not the report being edited anymore. The response's `id` is the NEW row's id,
  // which will differ from `reportId` (what's in the URL right now) exactly when this
  // happens. If this component kept treating `reportId` as current after that — saving
  // again, submitting, or just leaving the address bar on the old id — every next action
  // would target a row the backend has already stopped accepting edits on. So: whenever the
  // saved id doesn't match the id we saved against, navigate to the new id's edit URL. This
  // is the single point where that gets handled; nothing downstream should ever need to
  // guess whether a fork happened.
  const goToSavedReport = (saved: ReportDTO) => {
    if (mode === "edit" && reportId !== undefined && saved.id !== reportId) {
      router.replace(`/reports/${saved.id}/edit`);
    }
  };

  const handleSaveDraft = async () => {
    const validationError = validate(formData);
    if (validationError) {
      toast.error(validationError);
      return;
    }

    setSavingAction("draft");
    try {
      const saved = await saveReport();
      toast.success("Report saved as draft");
      goToSavedReport(saved);
    } catch (err) {
      toast.error(
        err instanceof ApiError ? err.message : "Couldn't save — check your connection and try again."
      );
    } finally {
      setSavingAction(null);
    }
  };

  const handleSubmitForReview = async () => {
    const validationError = validate(formData);
    if (validationError) {
      toast.error(validationError);
      return;
    }

    // Submit/Approve actions go through ConfirmDialog first (frontend-design-system.md §2).
    const confirmed = await confirm({
      title: "Submit this report for review?",
      description: "You won't be able to edit it until your manager responds.",
      variant: "primary",
      confirmLabel: "Submit for review",
    });
    if (!confirmed) return;

    setSavingAction("submit");
    try {
      const saved = await saveReport();
      // Submit against `saved.id`, not `reportId` — see the versioning-fork comment on
      // goToSavedReport above. The exact same "id may have just changed" fact applies here,
      // so this must never fall back to the (possibly now-frozen) id from the URL.
      await apiRequest<ReportDTO>(`/api/reports/${saved.id}/submit`, { method: "POST" });
      toast.success("Report submitted for review");
      router.push("/history");
    } catch (err) {
      toast.error(
        err instanceof ApiError ? err.message : "Couldn't submit — check your connection and try again."
      );
    } finally {
      setSavingAction(null);
    }
  };

  const completedTasks = formData.taskEntries.filter((task) => task.entryType === "COMPLETED");
  const plannedTasks = formData.taskEntries.filter((task) => task.entryType === "PLANNED_NEXT_WEEK");

  const emptyTaskEntry = (entryType: TaskEntryType): TaskEntryFormData => ({
    taskName: "",
    priority: "",
    plannedPercent: 0,
    actualPercent: 0,
    status: "",
    timePlanned: 0,
    timeSpent: 0,
    deliverable: "",
    entryType,
  });

  // One generic updater for both task sections (COMPLETED / PLANNED_NEXT_WEEK) — filters out
  // the relevant subset, applies whatever the caller wants done to it, and reassembles the
  // full flat array. This replaces the update/remove/add logic TaskTable used to own
  // internally per section; the actual behavior (filter → map/filter/append → merge back) is
  // unchanged, just centralized here now that two TaskEntryCard lists share it instead of
  // two separate TaskTable instances each doing their own version of the same thing.
  const updateTaskList = (
    entryType: TaskEntryType,
    updater: (list: TaskEntryFormData[]) => TaskEntryFormData[]
  ) => {
    const current = entryType === "COMPLETED" ? completedTasks : plannedTasks;
    const others = entryType === "COMPLETED" ? plannedTasks : completedTasks;
    const updated = updater(current);
    setField("taskEntries", entryType === "COMPLETED" ? [...updated, ...others] : [...others, ...updated]);
  };

  const handleTaskChange = (entryType: TaskEntryType, index: number, value: TaskEntryFormData) =>
    updateTaskList(entryType, (list) => list.map((task, i) => (i === index ? value : task)));

  const handleTaskRemove = (entryType: TaskEntryType, index: number) =>
    updateTaskList(entryType, (list) => list.filter((_, i) => i !== index));

  const handleTaskAdd = (entryType: TaskEntryType) =>
    updateTaskList(entryType, (list) => [...list, emptyTaskEntry(entryType)]);

  return (
    <div className="mx-auto flex max-w-4xl flex-col gap-8 pb-12">
      <h1 className="text-[28px] font-semibold text-ink">
        {mode === "create" ? "New weekly report" : "Edit weekly report"}
      </h1>

      {correctionComment && (
        <Alert variant="warning" title="Needs correction">
          {correctionComment}
        </Alert>
      )}

      <section className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <label className="flex flex-col gap-1 text-sm font-medium text-ink">
          Project
          <Select
            placeholder="Select a project"
            value={formData.projectId ?? ""}
            onChange={(event) =>
              setField("projectId", event.target.value ? Number(event.target.value) : null)
            }
            options={projects.map((project) => ({ value: String(project.id), label: project.name }))}
          />
        </label>
        <label className="flex flex-col gap-1 text-sm font-medium text-ink">
          Week start
          <DatePicker
            value={formData.weekStartDate}
            onChange={(event) => setField("weekStartDate", event.target.value)}
          />
        </label>
        <label className="flex flex-col gap-1 text-sm font-medium text-ink">
          Week end
          <DatePicker
            value={formData.weekEndDate}
            onChange={(event) => setField("weekEndDate", event.target.value)}
          />
        </label>
      </section>

      <section className="flex flex-col gap-3">
        <h3 className="text-sm font-semibold text-ink">Tasks completed</h3>
        <div className="flex flex-col gap-3">
          {completedTasks.map((task, index) => (
            <TaskEntryCard
              key={index}
              value={task}
              onChange={(value) => handleTaskChange("COMPLETED", index, value)}
              onRemove={() => handleTaskRemove("COMPLETED", index)}
            />
          ))}
        </div>
        <Button variant="secondary" onClick={() => handleTaskAdd("COMPLETED")} className="w-fit">
          <Plus size={16} />
          Add task
        </Button>
      </section>

      <section className="flex flex-col gap-3">
        <h3 className="text-sm font-semibold text-ink">Planned next week</h3>
        <div className="flex flex-col gap-3">
          {plannedTasks.map((task, index) => (
            <TaskEntryCard
              key={index}
              value={task}
              onChange={(value) => handleTaskChange("PLANNED_NEXT_WEEK", index, value)}
              onRemove={() => handleTaskRemove("PLANNED_NEXT_WEEK", index)}
            />
          ))}
        </div>
        <Button variant="secondary" onClick={() => handleTaskAdd("PLANNED_NEXT_WEEK")} className="w-fit">
          <Plus size={16} />
          Add task
        </Button>
      </section>

      <BlockersList rows={formData.blockers} onChange={(rows) => setField("blockers", rows)} />
      <AchievementsList rows={formData.achievements} onChange={(rows) => setField("achievements", rows)} />
      <HoursByTypeSection rows={formData.hoursByType} onChange={(rows) => setField("hoursByType", rows)} />

      <label className="flex flex-col gap-1 text-sm font-medium text-ink">
        Notes
        <Textarea
          value={formData.notes}
          onChange={(event) => setField("notes", event.target.value)}
          placeholder="Anything else worth flagging this week (optional)"
        />
      </label>

      <div className="flex justify-end gap-3 border-t border-line pt-6">
        <Button variant="secondary" onClick={handleSaveDraft} disabled={savingAction !== null}>
          {savingAction === "draft" ? "Saving…" : "Save draft"}
        </Button>
        <Button variant="primary" onClick={handleSubmitForReview} disabled={savingAction !== null}>
          {savingAction === "submit" ? "Submitting…" : "Submit for review"}
        </Button>
      </div>
    </div>
  );
}
