"use client";

import { Plus, Trash2 } from "lucide-react";
import Button from "./Button";
import Input from "./Input";
import { fieldClasses } from "./shared";
import type { HoursByTypeFormData } from "@/lib/reportTypes";

interface HoursByTypeSectionProps {
  rows: HoursByTypeFormData[];
  onChange: (rows: HoursByTypeFormData[]) => void;
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
      <div className="flex flex-col gap-2">
        {rows.map((row, index) => (
          <div key={index} className="flex items-center gap-2">
            <Input
              value={row.taskType}
              onChange={(e) => updateRow(index, { taskType: e.target.value })}
              placeholder="e.g. Development"
              aria-label="Task type"
              className="flex-1"
            />
            <input
              type="number"
              min={0}
              step={0.5}
              value={row.hours}
              onChange={(e) => updateRow(index, { hours: Number(e.target.value) })}
              aria-label="Hours"
              className={`${fieldClasses} w-24 font-mono text-right`}
            />
            <Button
              variant="ghost"
              ariaLabel="Remove hours entry"
              icon={<Trash2 size={16} />}
              onClick={() => removeRow(index)}
            />
          </div>
        ))}
      </div>
      <Button variant="secondary" onClick={addRow}>
        <Plus size={16} />
        Add hours entry
      </Button>
    </div>
  );
}
