"use client";

import { ReactNode, useState } from "react";
import { Trash2 } from "lucide-react";
import Button from "./Button";
import Input from "./Input";
import Select from "./Select";
import { fieldClasses } from "./shared";
import type { TaskEntryFormData } from "@/lib/reportTypes";

interface TaskEntryCardProps {
  value: TaskEntryFormData;
  onChange: (value: TaskEntryFormData) => void;
  onRemove: () => void;
}

// Verified against real saved data (task_entries.priority in the DB), not guessed — every
// existing row uses these exact uppercase values, not "High"/"Medium"/"Low". The backend
// field itself is a plain unconstrained String (docs/api/api-doc.md just says "priority":
// "string"), but since 100% of real data already agrees on one casing, a plain fixed Select
// is safe here — unlike Status below, there's no observed inconsistency to design around.
const PRIORITY_OPTIONS = [
  { value: "HIGH", label: "High" },
  { value: "MEDIUM", label: "Medium" },
  { value: "LOW", label: "Low" },
];

// Status is genuinely free text on the backend (same "string" typing as priority) — real
// data has "Done" and "In Progress" today, but nothing stops a future value the presets
// below don't cover. A plain fixed Select would silently be unable to represent that; an
// "Other..." option that reveals a text Input is what keeps this a UX convenience layered
// on top of a deliberately-flexible field, rather than a UI-imposed constraint the backend
// doesn't actually have.
const STATUS_PRESETS = ["Not Started", "In Progress", "Done", "Blocked"];
const STATUS_OTHER_VALUE = "__other__";

function isCustomStatus(status: string): boolean {
  return status !== "" && !STATUS_PRESETS.includes(status);
}

// One task entry as a bordered card — replaces the editable row-in-a-table layout
// (components/ui/TaskTable.tsx, now deleted — it had no reliable way to give every numeric
// column enough width on a narrow viewport, and turned out to be unused for read-only
// display too: ReportDetail.tsx never imported it, it has always had its own separate
// ReadOnlyTaskTable). This component only replaces the editing path.
export default function TaskEntryCard({ value, onChange, onRemove }: TaskEntryCardProps) {
  // Derived once at mount from the incoming value (not re-derived every render) — after
  // that, this is the source of truth for which UI is showing, so typing a custom status
  // down to "" (clearing the field) doesn't cause it to unexpectedly snap back to the Select.
  const [statusIsCustom, setStatusIsCustom] = useState(() => isCustomStatus(value.status));

  const setField = <K extends keyof TaskEntryFormData>(field: K, fieldValue: TaskEntryFormData[K]) => {
    onChange({ ...value, [field]: fieldValue });
  };

  return (
    <div className="flex flex-col gap-3 rounded border border-line bg-surface p-4">
      <div className="flex items-start justify-between gap-3">
        <label className="flex flex-1 flex-col gap-1 text-sm font-medium text-ink">
          Task name
          <Input
            value={value.taskName}
            onChange={(event) => setField("taskName", event.target.value)}
            aria-label="Task name"
          />
        </label>
        <Button variant="ghost" ariaLabel="Remove task" icon={<Trash2 size={16} />} onClick={onRemove} />
      </div>

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <label className="flex flex-col gap-1 text-sm font-medium text-ink">
          Priority
          <Select
            value={value.priority}
            onChange={(event) => setField("priority", event.target.value)}
            options={PRIORITY_OPTIONS}
            placeholder="Select priority"
            aria-label="Priority"
          />
        </label>

        <label className="flex flex-col gap-1 text-sm font-medium text-ink">
          Status
          {statusIsCustom ? (
            <div className="flex flex-col gap-1">
              <Input
                value={value.status}
                onChange={(event) => setField("status", event.target.value)}
                placeholder="Enter a status"
                aria-label="Status"
                autoFocus
              />
              <button
                type="button"
                className="w-fit text-left text-xs text-accent hover:underline"
                onClick={() => {
                  setStatusIsCustom(false);
                  setField("status", "");
                }}
              >
                Choose from list instead
              </button>
            </div>
          ) : (
            <Select
              value={value.status}
              onChange={(event) => {
                if (event.target.value === STATUS_OTHER_VALUE) {
                  setStatusIsCustom(true);
                  setField("status", "");
                } else {
                  setField("status", event.target.value);
                }
              }}
              options={[
                ...STATUS_PRESETS.map((status) => ({ value: status, label: status })),
                { value: STATUS_OTHER_VALUE, label: "Other…" },
              ]}
              placeholder="Select status"
              aria-label="Status"
            />
          )}
        </label>
      </div>

      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <NumericField
          label="Plan %"
          unit="%"
          value={value.plannedPercent}
          onChange={(n) => setField("plannedPercent", n)}
          min={0}
          max={100}
          ariaLabel="Planned percent"
        />
        <NumericField
          label="Actual %"
          unit="%"
          value={value.actualPercent}
          onChange={(n) => setField("actualPercent", n)}
          min={0}
          max={100}
          ariaLabel="Actual percent"
        />
        <NumericField
          label="Hrs Planned"
          unit="hrs"
          value={value.timePlanned}
          onChange={(n) => setField("timePlanned", n)}
          min={0}
          ariaLabel="Hours planned"
        />
        <NumericField
          label="Hrs Spent"
          unit="hrs"
          value={value.timeSpent}
          onChange={(n) => setField("timeSpent", n)}
          min={0}
          ariaLabel="Hours spent"
        />
      </div>

      <label className="flex flex-col gap-1 text-sm font-medium text-ink">
        Deliverable
        <Input
          value={value.deliverable}
          onChange={(event) => setField("deliverable", event.target.value)}
          aria-label="Deliverable"
        />
      </label>
    </div>
  );
}

interface NumericFieldProps {
  label: string;
  unit: ReactNode;
  value: number;
  onChange: (value: number) => void;
  min?: number;
  max?: number;
  ariaLabel: string;
}

// Unit suffix rendered INSIDE the field (an input-group pattern), not just a column header —
// a header can scroll out of view or get separated from its value at a glance; a suffix that
// travels with the input itself never can. This is what replaces TaskTable's old "Plan %"
// header-only labeling, which is also the layout that let the column get squeezed narrow
// enough to visually clip the digits (see this task's write-up on the old bug).
function NumericField({ label, unit, value, onChange, min, max, ariaLabel }: NumericFieldProps) {
  return (
    <label className="flex flex-col gap-1 text-sm font-medium text-ink">
      {label}
      <div className="relative">
        <input
          type="number"
          min={min}
          max={max}
          value={value}
          onChange={(event) => onChange(Number(event.target.value))}
          aria-label={ariaLabel}
          className={`${fieldClasses} pr-10 font-mono`}
        />
        <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-xs text-muted">
          {unit}
        </span>
      </div>
    </label>
  );
}
