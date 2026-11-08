package com.feedback.report.application.usecase;

/** How many evaluations the report covered and how many admins received it. */
public record SendWeeklyReportResult(int evaluations, int recipients) {
}
