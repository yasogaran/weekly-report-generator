package com.company.weeklyreports.service;

/**
 * The actions that can drive a Report from one status to another. Kept
 * separate from ReportStatus itself since an action (e.g. SUBMIT) is a
 * verb performed by an actor, not a state the report sits in.
 */
public enum ReportTransitionAction {
    SUBMIT,
    APPROVE,
    REQUEST_CHANGES
}
