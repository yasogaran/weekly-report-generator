"use client";

import { Flag, Plus, Trash2 } from "lucide-react";
import Button from "./Button";
import Input from "./Input";
import type { AchievementFormData } from "@/lib/reportTypes";

interface AchievementsListProps {
  rows: AchievementFormData[];
  onChange: (rows: AchievementFormData[]) => void;
}

// Mirrors BlockersList.tsx's structure exactly, but isn't merged with it into one generic
// component — the two lists edit different DTO shapes (isKeyIssue vs. isKeyAchievement),
// and forcing a shared generic over two four-line handlers would cost more in indirection
// than it'd save in duplication.
export default function AchievementsList({ rows, onChange }: AchievementsListProps) {
  const updateDescription = (index: number, description: string) => {
    onChange(rows.map((row, i) => (i === index ? { ...row, description } : row)));
  };

  // Same mutual-exclusion rule as BlockersList: flagging one row un-flags every other row
  // in the same state update, enforced here rather than left to the UI to merely display.
  const toggleFlag = (index: number) => {
    onChange(
      rows.map((row, i) => ({ ...row, isKeyAchievement: i === index ? !row.isKeyAchievement : false }))
    );
  };

  const removeRow = (index: number) => {
    onChange(rows.filter((_, i) => i !== index));
  };

  const addRow = () => {
    onChange([...rows, { description: "", isKeyAchievement: false }]);
  };

  return (
    <div className="flex flex-col gap-3">
      <h3 className="text-sm font-semibold text-ink">Achievements</h3>
      <div className="flex flex-col gap-2">
        {rows.map((row, index) => (
          <div key={index} className="flex items-center gap-2">
            <Input
              value={row.description}
              onChange={(e) => updateDescription(index, e.target.value)}
              placeholder="Describe the achievement"
              aria-label="Achievement description"
              className="flex-1"
            />
            <Button
              variant="ghost"
              ariaLabel={
                row.isKeyAchievement
                  ? "Key achievement this week — click to unflag"
                  : "Mark this as the most important achievement this week"
              }
              icon={
                <Flag
                  size={16}
                  className={row.isKeyAchievement ? "text-accent" : "text-muted"}
                  fill={row.isKeyAchievement ? "currentColor" : "none"}
                />
              }
              onClick={() => toggleFlag(index)}
            />
            <Button
              variant="ghost"
              ariaLabel="Remove achievement"
              icon={<Trash2 size={16} />}
              onClick={() => removeRow(index)}
            />
          </div>
        ))}
      </div>
      <Button variant="secondary" onClick={addRow}>
        <Plus size={16} />
        Add achievement
      </Button>
    </div>
  );
}
