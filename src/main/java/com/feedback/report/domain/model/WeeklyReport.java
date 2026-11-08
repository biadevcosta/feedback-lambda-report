package com.feedback.report.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Report numbers. Every day of the period and every urgency level appears in the counts,
 * with 0 when there was no evaluation, so the email always shows the full picture.
 */
public record WeeklyReport(ReportPeriod period, int total, Optional<BigDecimal> averageScore,
        Map<LocalDate, Long> countByDay, Map<Urgency, Long> countByUrgency, List<Evaluation> evaluations) {

    public WeeklyReport {
        countByDay = Map.copyOf(countByDay);
        countByUrgency = Map.copyOf(countByUrgency);
        evaluations = List.copyOf(evaluations);
    }

    public boolean isEmpty() {
        return total == 0;
    }
}
