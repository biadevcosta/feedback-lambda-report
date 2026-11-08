package com.feedback.report.domain.model;

import java.time.LocalDate;
import java.util.List;

/** Inclusive range of days covered by a report. */
public record ReportPeriod(LocalDate start, LocalDate end) {

    public ReportPeriod {
        if (start == null || end == null || start.isAfter(end)) {
            throw new IllegalArgumentException("Report period must have a start on or before its end");
        }
    }

    /** The given number of complete days before {@code today}, e.g. Monday → previous Monday to Sunday. */
    public static ReportPeriod completeDaysBefore(LocalDate today, int days) {
        if (days < 1) {
            throw new IllegalArgumentException("Report period must have at least one day");
        }
        return new ReportPeriod(today.minusDays(days), today.minusDays(1));
    }

    public List<LocalDate> days() {
        return start.datesUntil(end.plusDays(1)).toList();
    }
}
