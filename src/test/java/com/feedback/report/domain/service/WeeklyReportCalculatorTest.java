package com.feedback.report.domain.service;

import static com.feedback.report.fixtures.EvaluationFixtures.anEvaluation;
import static org.assertj.core.api.Assertions.assertThat;

import com.feedback.report.domain.model.Evaluation;
import com.feedback.report.domain.model.ReportPeriod;
import com.feedback.report.domain.model.Urgency;
import com.feedback.report.domain.model.WeeklyReport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WeeklyReportCalculatorTest {

    // Monday Nov 2 to Sunday Nov 8, 2026
    private static final ReportPeriod PERIOD = ReportPeriod.completeDaysBefore(LocalDate.parse("2026-11-09"), 7);

    private final WeeklyReportCalculator calculator = new WeeklyReportCalculator();

    @Test
    void should_computeTotalsCountsAndAverage_when_weekHasEvaluations() {
        // given
        List<Evaluation> evaluations = List.of(
                anEvaluation().withScore(2).submittedAt("2026-11-03T12:00:00Z").build(),
                anEvaluation().withScore(5).submittedAt("2026-11-03T13:00:00Z").build(),
                anEvaluation().withScore(9).submittedAt("2026-11-06T12:00:00Z").build());

        // when
        WeeklyReport report = calculator.calculate(PERIOD, evaluations);

        // then
        assertThat(report.total()).isEqualTo(3);
        assertThat(report.averageScore()).contains(new BigDecimal("5.3"));
        assertThat(report.countByUrgency())
                .containsEntry(Urgency.HIGH, 1L)
                .containsEntry(Urgency.MEDIUM, 1L)
                .containsEntry(Urgency.LOW, 1L);
        assertThat(report.countByDay())
                .hasSize(7)
                .containsEntry(LocalDate.parse("2026-11-03"), 2L)
                .containsEntry(LocalDate.parse("2026-11-06"), 1L)
                .containsEntry(LocalDate.parse("2026-11-02"), 0L)
                .containsEntry(LocalDate.parse("2026-11-08"), 0L);
    }

    @ParameterizedTest(name = "scores {0} → average {1}")
    @CsvSource(delimiter = '|', value = {
        "7,8       | 7.5",
        "7,7,8,8   | 7.5",
        "1,2,2     | 1.7",
        "10        | 10.0",
        "0,0       | 0.0",
        "6,7,7,9   | 7.3"
    })
    void should_roundAverageToOneDecimalHalfUp(String scores, String expected) {
        List<Evaluation> evaluations = java.util.Arrays.stream(scores.split(","))
                .map(score -> anEvaluation().withScore(Integer.parseInt(score.trim())).build())
                .toList();

        assertThat(calculator.calculate(PERIOD, evaluations).averageScore()).contains(new BigDecimal(expected));
    }

    @Test
    void should_reportZeros_when_weekHasNoEvaluations() {
        WeeklyReport report = calculator.calculate(PERIOD, List.of());

        assertThat(report.isEmpty()).isTrue();
        assertThat(report.averageScore()).isEmpty();
        assertThat(report.countByDay()).hasSize(7).allSatisfy((day, count) -> assertThat(count).isZero());
        assertThat(report.countByUrgency()).hasSize(3).allSatisfy((urgency, count) -> assertThat(count).isZero());
    }

    @Test
    void should_ignoreEvaluations_when_theyAreOutsideThePeriod() {
        List<Evaluation> evaluations = List.of(
                anEvaluation().submittedAt("2026-11-02T02:59:00Z").build(), // Nov 1 in Brasília
                anEvaluation().submittedAt("2026-11-02T03:00:00Z").build(), // Nov 2 in Brasília
                anEvaluation().submittedAt("2026-11-09T03:00:00Z").build()); // Nov 9 in Brasília

        assertThat(calculator.calculate(PERIOD, evaluations).total()).isEqualTo(1);
    }

    @Test
    void should_listEvaluationsBySubmissionTime() {
        Evaluation later = anEvaluation().submittedAt("2026-11-05T12:00:00Z").build();
        Evaluation earlier = anEvaluation().submittedAt("2026-11-04T12:00:00Z").build();

        assertThat(calculator.calculate(PERIOD, List.of(later, earlier)).evaluations()).containsExactly(earlier, later);
    }
}
