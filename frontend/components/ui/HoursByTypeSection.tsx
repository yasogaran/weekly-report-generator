"use client";

import { useState } from "react";
import { Plus, Trash2 } from "lucide-react";
import Button from "./Button";
import Input from "./Input";
import Select from "./Select";
import { fieldClasses } from "./shared";
import type { HoursByTypeFormData } from "@/lib/reportTypes";

interface HoursByTypeSectionProps {
  rows: HoursByTypeFormData[];
  onChange: (rows: HoursByTypeFormData[]) => void;
}

// Same free-text-with-presets pattern as TaskEntryCard's Status field, for the same reason:
// taskType is an unconstrained backend String (docs/api/api-doc.md's hoursByType example is
// just `"taskType": "Development"`, no enum), but the spec's own examples name a small
// recurring set — Development, Testing, Meetings, Documentation — so a Select with those
// plus "Other..." covers the common case without the UI pretending the backend enforces a
// fixed list it doesn't.
const TASK_TYPE_PRESETS = ["Development", "Testing", "Meetings", "Documentation"];
const OTHER_VALUE = "__other__";

function isCustomTaskType(taskType: string): boolean {
  return taskType !== "" && !TASK_TYPE_PRESETS.includes(taskType);
}

// Optional breakdown of hours by task type (e.g. "Development: 6"). Not required by the
// backend (an empty array is a valid CreateReportRequest.hoursByType), so the heading says
// so explicitly rather than leaving it to be inferred from the absence of a red asterisk.
export default function HoursByTypeSection({ rows, onChange }: HoursByTypeSectionProps) {
  const updateRow = (index: number, patch: Partial<HoursByTypeFormData>) => {
    onChange(rows.map((row, i) => (i === index ? { ...row, ...patch } : row)));
  };

  const removeRow = (index: number) => {
    onChange(rows.filter((_, i) => i !== index));
  };

  const addRow = () => {
    onChange([...rows, { taskType: "", hours: 0 }]);
  };

  return (
    <div className="flex flex-col gap-3">
      <h3 className="text-sm font-semibold text-ink">Hours by type (optional)</h3>
      <div className="flex flex-col gap-3">
        {rows.map((row, index) => (
          <HoursRow
            key={index}
            row={row}
            onChange={(patch) => updateRow(index, patch)}
            onRemove={() => removeRow(index)}
          />
        ))}
      </div>
      <Button variant="secondary" onClick={addRow} className="w-fit">
        <Plus size={16} />
        Add hours entry
      </Button>
    </div>
  );
}

interface HoursRowProps {
  row: HoursByTypeFormData;
  onChange: (patch: Partial<HoursByTypeFormData>) => void;
  onRemove: () => void;
}

function HoursRow({ row, onChange, onRemove }: HoursRowProps) {
  const [taskTypeIsCustom, setTaskTypeIsCustom] = useState(() => isCustomTaskType(row.taskType));

  return (
    <div className="flex items-start gap-3">
      {/* Root cause of the old near-zero-width bug (see this task's write-up): Input's
          shared fieldClasses always includes w-full, and — verified against the actual
          compiled Tailwind CSS, not assumed — .w-full is emitted AFTER narrower width
          utilities like .w-24 in the generated stylesheet, so it wins the cascade
          regardless of className order. Appending a fixed width directly onto an Input
          could therefore never actually shrink it. The fix is structural: any fixed-width
          constraint goes on a WRAPPING element instead, and the Input/Select inside is left
          to fill 100% of that wrapper via its own untouched w-full — no two width utilities
          ever compete for the same element again. min-w-0 on the flexible wrapper is the
          other half of this: without it, a flex item's default min-width:auto can refuse to
          shrink below its content's intrinsic width, which a <select>'s longest option text
          can trigger just as easily as a fixed-width clash can. */}
      <div className="min-w-0 flex-1">
        <label className="flex flex-col gap-1 text-xs text-muted">
          Task type
          {taskTypeIsCustom ? (
            <div className="flex flex-col gap-1">
              <Input
                value={row.taskType}
                onChange={(event) => onChange({ taskType: event.target.value })}
                placeholder="Enter a task type"
                aria-label="Task type"
                autoFocus
              />
              <button
                type="button"
                className="w-fit text-left text-xs text-accent hover:underline"
                onClick={() => {
                  setTaskTypeIsCustom(false);
                  onChange({ taskType: "" });
                }}
              >
                Choose from list instead
              </button>
            </div>
          ) : (
            <Select
              value={row.taskType}
              onChange={(event) => {
                if (event.target.value === OTHER_VALUE) {
                  setTaskTypeIsCustom(true);
                  onChange({ taskType: "" });
                } else {
                  onChange({ taskType: event.target.value });
                }
              }}
              options={[
                ...TASK_TYPE_PRESETS.map((type) => ({ value: type, label: type })),
                { value: OTHER_VALUE, label: "Other…" },
              ]}
              placeholder="Select task type"
              aria-label="Task type"
            />
          )}
        </label>
      </div>

      {/* Fixed width lives on this wrapper, not on the <input> itself — see the comment
          above for why that distinction is the actual fix, not a cosmetic preference. */}
      <div className="w-28 shrink-0">
        <label className="flex flex-col gap-1 text-xs text-muted">
          Hours
          <div className="relative">
            <input
              type="number"
              min={0}
              step={0.5}
              value={row.hours}
              onChange={(event) => onChange({ hours: Number(event.target.value) })}
              aria-label="Hours"
              className={`${fieldClasses} pr-9 font-mono text-right`}
            />
            <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-xs text-muted">
              hrs
            </span>
          </div>
        </label>
      </div>

      {/* Invisible spacer matching the fields' label row height, so the delete button lines
          up with the input row itself rather than sitting a line too high. */}
      <div className="flex flex-col gap-1">
        <span aria-hidden="true" className="select-none text-xs text-transparent">
          .
        </span>
        <Button variant="ghost" ariaLabel="Remove hours entry" icon={<Trash2 size={16} />} onClick={onRemove} />
      </div>
    </div>
  );
}
