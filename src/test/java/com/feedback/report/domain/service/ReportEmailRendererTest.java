package com.feedback.report.domain.service;

import static com.feedback.report.fixtures.EvaluationFixtures.anEvaluation;
import static org.assertj.core.api.Assertions.assertThat;

import com.feedback.report.domain.model.EmailMessage;
import com.feedback.report.domain.model.ReportPeriod;
import com.feedback.report.domain.model.WeeklyReport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReportEmailRendererTest {

    private static final ReportPeriod PERIOD = ReportPeriod.completeDaysBefore(LocalDate.parse("2026-11-09"), 7);

    private final WeeklyReportCalculator calculator = new WeeklyReportCalculator();
    private final ReportEmailRenderer renderer = new ReportEmailRenderer();

    @Test
    void should_namePeriodInSubject() {
        EmailMessage email = renderer.render(calculator.calculate(PERIOD, List.of()));

        assertThat(email.subject()).isEqualTo("[Weekly report] Evaluations from 02/11/2026 to 08/11/2026");
    }

    @Test
    void should_showTotalsCountsAndList_when_weekHasEvaluations() {
        // given
        WeeklyReport report = calculator.calculate(PERIOD, List.of(
                anEvaluation().withScore(2).withDescription("Too fast").submittedAt("2026-11-04T01:30:00Z").build(),
                anEvaluation().withScore(9).withDescription("Loved it").submittedAt("2026-11-06T15:00:00Z").build()));

        // when
        EmailMessage email = renderer.render(report);

        // then
        assertThat(email.textBody())
                .contains("Total evaluations: 2")
                .contains("Average score: 5.5")
                .contains("03/11/2026: 1") // 01:30 UTC on Nov 4 is still Nov 3 in Brasília
                .contains("02/11/2026: 0")
                .contains("high: 1")
                .contains("medium: 0")
                .contains("low: 1")
                .contains("03/11/2026 22:30 | high | Too fast")
                .contains("06/11/2026 12:00 | low | Loved it");
        assertThat(email.htmlBody())
                .contains("<strong>Total evaluations:</strong> 2")
                .contains("<strong>Average score:</strong> 5.5")
                .contains("<td>Too fast</td>");
    }

    @Test
    void should_sayThereWereNoEvaluations_when_weekIsEmpty() {
        EmailMessage email = renderer.render(calculator.calculate(PERIOD, List.of()));

        assertThat(email.textBody()).contains("No evaluations were submitted in this period.");
        assertThat(email.htmlBody()).contains("No evaluations were submitted in this period.");
        assertThat(email.textBody()).doesNotContain("Average score");
    }

    @Test
    void should_escapeDescriptionInHtml() {
        WeeklyReport report = calculator.calculate(PERIOD,
                List.of(anEvaluation().withDescription("<img src=x onerror=alert(1)> & co").build()));

        EmailMessage email = renderer.render(report);

        assertThat(email.htmlBody())
                .doesNotContain("<img")
                .contains("&lt;img src=x onerror=alert(1)&gt; &amp; co");
    }
}
