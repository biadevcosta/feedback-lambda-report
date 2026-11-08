package com.feedback.report.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReportPeriodTest {

    private static final LocalDate MONDAY = LocalDate.parse("2026-11-09");

    @Test
    void should_coverPreviousMondayToSunday_when_runOnMondayFor7Days() {
        ReportPeriod period = ReportPeriod.completeDaysBefore(MONDAY, 7);

        assertThat(period.start()).isEqualTo("2026-11-02");
        assertThat(period.end()).isEqualTo("2026-11-08");
    }

    @Test
    void should_listEveryDayInclusive() {
        ReportPeriod period = ReportPeriod.completeDaysBefore(MONDAY, 7);

        assertThat(period.days()).hasSize(7).startsWith(period.start()).endsWith(period.end());
    }

    @Test
    void should_coverOnlyYesterday_when_periodHasOneDay() {
        assertThat(ReportPeriod.completeDaysBefore(MONDAY, 1).days()).containsExactly(LocalDate.parse("2026-11-08"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void should_reject_when_daysIsNotPositive(int days) {
        assertThatThrownBy(() -> ReportPeriod.completeDaysBefore(MONDAY, days)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_reject_when_startIsAfterEnd() {
        assertThatThrownBy(() -> new ReportPeriod(MONDAY, MONDAY.minusDays(1))).isInstanceOf(IllegalArgumentException.class);
    }
}
