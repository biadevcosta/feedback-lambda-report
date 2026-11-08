package com.feedback.report.domain.service;

import com.feedback.report.domain.model.Evaluation;
import com.feedback.report.domain.model.ReportPeriod;
import com.feedback.report.domain.model.Urgency;
import com.feedback.report.domain.model.WeeklyReport;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Computes the report numbers. Pure: no I/O, same input, same report. */
public final class WeeklyReportCalculator {

    public WeeklyReport calculate(ReportPeriod period, List<Evaluation> evaluations) {
        // Defensive: keep only evaluations inside the period
        List<Evaluation> inPeriod = evaluations.stream()
                .filter(evaluation -> !evaluation.day().isBefore(period.start()) && !evaluation.day().isAfter(period.end()))
                .sorted(Comparator.comparing(Evaluation::submittedAt))
                .toList();

        Map<LocalDate, Long> countByDay = new LinkedHashMap<>();
        period.days().forEach(day -> countByDay.put(day, 0L));
        inPeriod.forEach(evaluation -> countByDay.merge(evaluation.day(), 1L, Long::sum));

        Map<Urgency, Long> countByUrgency = new EnumMap<>(Urgency.class);
        for (Urgency urgency : Urgency.values()) {
            countByUrgency.put(urgency, 0L);
        }
        inPeriod.forEach(evaluation -> countByUrgency.merge(evaluation.urgency(), 1L, Long::sum));

        return new WeeklyReport(period, inPeriod.size(), average(inPeriod), countByDay, countByUrgency, inPeriod);
    }

    // One decimal place, half up (7.25 → 7.3); empty when there is nothing to average
    private static Optional<BigDecimal> average(List<Evaluation> evaluations) {
        if (evaluations.isEmpty()) {
            return Optional.empty();
        }
        int sum = evaluations.stream().mapToInt(Evaluation::score).sum();
        return Optional.of(BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(evaluations.size()), 1, RoundingMode.HALF_UP));
    }
}
