"use client";

import { Flag, Plus, Trash2 } from "lucide-react";
import Button from "./Button";
import Input from "./Input";
import type { BlockerFormData } from "@/lib/reportTypes";

interface BlockersListProps {
  rows: BlockerFormData[];
  onChange: (rows: BlockerFormData[]) => void;
}

// Add/remove list of blockers, with a "flag as key issue" toggle that's mutually exclusive
// across the whole list — enforced here in state, not just by how it looks, since a UI that
// merely *rendered* only one as flagged while letting multiple rows carry isKeyIssue: true
// underneath would send that inconsistent state straight to the API.
export default function BlockersList({ rows, onChange }: BlockersListProps) {
  const updateDescription = (index: number, description: string) => {
    onChange(rows.map((row, i) => (i === index ? { ...row, description } : row)));
  };

  // Flagging row `index` un-flags every other row in the same update — there's never a
  // moment where two rows both hold isKeyIssue: true. Clicking an already-flagged row's
  // toggle again un-flags it (no key issue selected), rather than being a no-op.
  const toggleFlag = (index: number) => {
    onChange(rows.map((row, i) => ({ ...row, isKeyIssue: i === index ? !row.isKeyIssue : false })));
  };

  const removeRow = (index: number) => {
    onChange(rows.filter((_, i) => i !== index));
  };

  const addRow = () => {
    onChange([...rows, { description: "", isKeyIssue: false }]);
  };

  return (
    <div className="flex flex-col gap-3">
      <h3 className="text-sm font-semibold text-ink">Blockers</h3>
      <div className="flex flex-col gap-2">
        {rows.map((row, index) => (
          <div key={index} className="flex items-center gap-2">
            <Input
              value={row.description}
              onChange={(e) => updateDescription(index, e.target.value)}
              placeholder="Describe the blocker"
              aria-label="Blocker description"
              className="flex-1"
            />
            <Button
              variant="ghost"
              ariaLabel={
                row.isKeyIssue
                  ? "Key blocker this week — click to unflag"
                  : "Mark this as the most important blocker this week"
              }
              icon={
                <Flag
                  size={16}
                  className={row.isKeyIssue ? "text-accent" : "text-muted"}
                  fill={row.isKeyIssue ? "currentColor" : "none"}
                />
              }
              onClick={() => toggleFlag(index)}
            />
            <Button
              variant="ghost"
              ariaLabel="Remove blocker"
              icon={<Trash2 size={16} />}
              onClick={() => removeRow(index)}
            />
          </div>
        ))}
      </div>
      <Button variant="secondary" onClick={addRow}>
        <Plus size={16} />
        Add blocker
      </Button>
    </div>
  );
}
