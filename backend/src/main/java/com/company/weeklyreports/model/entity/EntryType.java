package com.company.weeklyreports.model.entity;

/**
 * Distinguishes a task entry that was already worked on this week from one
 * planned for next week - both live in the same TaskEntry table/shape.
 */
public enum EntryType {
    COMPLETED,
    PLANNED_NEXT_WEEK
}
