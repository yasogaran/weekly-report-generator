"use client";

// TEMPORARY — this page exists only to visually verify the components/ui/ library in one
// place before real pages are built on top of it. Delete this whole app/(dev)/ route group
// before final submission.

import { useState } from "react";
import { Inbox, Plus, Trash2 } from "lucide-react";
import Button from "@/components/ui/Button";
import Input from "@/components/ui/Input";
import Textarea from "@/components/ui/Textarea";
import Select from "@/components/ui/Select";
import DatePicker from "@/components/ui/DatePicker";
import StatusBadge, { ReportStatus } from "@/components/ui/StatusBadge";
import Alert from "@/components/ui/Alert";
import EmptyState from "@/components/ui/EmptyState";
import DataTable, { DataTableColumn } from "@/components/ui/DataTable";
import { useToast } from "@/components/ui/Toast";
import { useConfirm } from "@/components/ui/ConfirmDialog";

const ALL_STATUSES: ReportStatus[] = ["DRAFT", "SUBMITTED", "NEEDS_CORRECTION", "APPROVED"];

interface TaskRow {
  id: number;
  task: string;
  plannedPct: number;
  actualPct: number;
}

const TASK_ROWS: TaskRow[] = [
  { id: 1, task: "Migrate auth service", plannedPct: 100, actualPct: 80 },
  { id: 2, task: "Write dashboard queries", plannedPct: 60, actualPct: 60 },
];

const taskColumns: DataTableColumn<TaskRow>[] = [
  { key: "task", header: "Task", render: (row) => row.task },
  { key: "planned", header: "Plan %", numeric: true, render: (row) => `${row.plannedPct}%` },
  { key: "actual", header: "Actual %", numeric: true, render: (row) => `${row.actualPct}%` },
];

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section className="flex flex-col gap-3 border-b border-line pb-8">
      <h2 className="text-lg font-semibold text-ink">{title}</h2>
      {children}
    </section>
  );
}

export default function ComponentPreviewPage() {
  const toast = useToast();
  const confirm = useConfirm();
  const [page, setPage] = useState(1);

  const handleApprove = async () => {
    const ok = await confirm({
      title: "Approve this report?",
      description: "The team member will be notified this report is finalized.",
      variant: "primary",
      confirmLabel: "Approve",
    });
    if (ok) toast.success("Report approved");
  };

  const handleDeleteProject = async () => {
    const ok = await confirm({
      title: "Delete this project?",
      description: "This can't be undone. Reports linked to it will keep their history.",
      variant: "destructive",
      confirmLabel: "Delete",
    });
    if (ok) toast.error("Project deleted");
  };

  return (
    <main className="mx-auto flex max-w-3xl flex-col gap-8 px-6 py-10">
      <h1 className="text-[28px] font-semibold text-ink">Component preview (temporary)</h1>

      <Section title="Button">
        <div className="flex flex-wrap items-center gap-3">
          <Button variant="primary" onClick={() => toast.success("Report submitted for review")}>
            Submit for review
          </Button>
          <Button variant="secondary" onClick={() => toast.info("Draft saved")}>
            Save draft
          </Button>
          <Button variant="destructive" onClick={handleDeleteProject}>
            Delete project
          </Button>
          <Button variant="ghost" ariaLabel="Delete task" icon={<Trash2 size={16} />} />
        </div>
      </Section>

      <Section title="Form primitives">
        <div className="flex flex-col gap-3">
          <Input placeholder="Task name" />
          <Textarea placeholder="Manager comment" />
          <Select
            placeholder="Select a project"
            options={[
              { value: "client-a", label: "Client A" },
              { value: "client-b", label: "Client B" },
            ]}
          />
          <DatePicker />
        </div>
      </Section>

      <Section title="StatusBadge">
        <div className="flex flex-wrap gap-3">
          {ALL_STATUSES.map((status) => (
            <StatusBadge key={status} status={status} />
          ))}
        </div>
      </Section>

      <Section title="Alert">
        <div className="flex flex-col gap-3">
          <Alert variant="warning" title="Needs correction">
            Please add more detail to the blocker section.
          </Alert>
          <Alert variant="info">This week&apos;s report is due Friday.</Alert>
          <Alert variant="success" dismissible>
            Your report was approved.
          </Alert>
          <Alert variant="error">Couldn&apos;t save — check your connection and try again.</Alert>
        </div>
      </Section>

      <Section title="Toast (click to trigger)">
        <div className="flex flex-wrap gap-3">
          <Button variant="secondary" onClick={() => toast.success("Report submitted")}>
            Trigger success toast
          </Button>
          <Button variant="secondary" onClick={() => toast.error("Couldn't save changes")}>
            Trigger error toast
          </Button>
          <Button variant="secondary" onClick={() => toast.info("New report assigned to you")}>
            Trigger info toast
          </Button>
        </div>
      </Section>

      <Section title="ConfirmDialog (promise-based)">
        <div className="flex flex-wrap gap-3">
          <Button variant="primary" onClick={handleApprove}>
            Approve
          </Button>
          <Button variant="destructive" onClick={handleDeleteProject}>
            Delete project
          </Button>
        </div>
      </Section>

      <Section title="EmptyState">
        <EmptyState
          icon={<Inbox size={32} />}
          title="No reports yet this week"
          description="Create your first weekly report to get started."
          actionLabel="Create report"
          onAction={() => toast.info("Would navigate to the new report form")}
        />
      </Section>

      <Section title="DataTable">
        <DataTable
          columns={taskColumns}
          rows={TASK_ROWS}
          rowKey={(row) => row.id}
          pagination={{ page, totalPages: 3, onPageChange: setPage }}
        />
      </Section>

      <Section title="EmptyState + Button (icon reference)">
        <div className="flex items-center gap-2 text-sm text-muted">
          <Plus size={16} />
          <span>lucide-react icons render fine inside Button/EmptyState.</span>
        </div>
      </Section>
    </main>
  );
}
