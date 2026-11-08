package com.feedback.report.adapter.in.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.feedback.report.application.usecase.SendWeeklyReportCommand;
import com.feedback.report.application.usecase.SendWeeklyReportResult;
import com.feedback.report.application.usecase.SendWeeklyReportUseCase;
import com.feedback.report.fixtures.TestEvents;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeeklyReportTriggerTest {

    @Mock
    private SendWeeklyReportUseCase useCase;

    @Mock
    private LambdaLogger logger;

    @Captor
    private ArgumentCaptor<SendWeeklyReportCommand> command;

    @Captor
    private ArgumentCaptor<String> logged;

    @ParameterizedTest(name = "run at {0} UTC → {1} to {2}")
    @CsvSource({
        // scheduled run: Monday 8am in Brasília
        "2026-11-09T11:00:00Z, 2026-11-02, 2026-11-08",
        // 01:00 UTC on Monday is still Sunday in Brasília, so Sunday is not complete yet
        "2026-11-09T01:00:00Z, 2026-11-01, 2026-11-07"
    })
    void should_reportCompleteDaysBeforeTodayInBrasilia(String now, String start, String end) {
        // given
        when(useCase.execute(any(SendWeeklyReportCommand.class))).thenReturn(new SendWeeklyReportResult(0, 1));

        // when
        trigger(now).handle(TestEvents.scheduled("scheduled.json"), logger);

        // then
        verify(useCase).execute(command.capture());
        assertThat(command.getValue().period().start()).isEqualTo(LocalDate.parse(start));
        assertThat(command.getValue().period().end()).isEqualTo(LocalDate.parse(end));
    }

    @Test
    void should_workWithEmptyEvent_when_invokedManually() {
        when(useCase.execute(any(SendWeeklyReportCommand.class))).thenReturn(new SendWeeklyReportResult(3, 2));

        trigger("2026-11-09T11:00:00Z").handle(new ScheduledEvent(), logger);

        verify(logger).log(logged.capture());
        assertThat(logged.getValue())
                .isEqualTo("Weekly report 2026-11-02 to 2026-11-08: 3 evaluations, sent to 2 admins");
    }

    @Test
    void should_propagateError_when_reportFails() {
        when(useCase.execute(any(SendWeeklyReportCommand.class))).thenThrow(new IllegalStateException("SES unavailable"));

        assertThatThrownBy(() -> trigger("2026-11-09T11:00:00Z").handle(new ScheduledEvent(), logger))
                .isInstanceOf(IllegalStateException.class);
    }

    private WeeklyReportTrigger trigger(String now) {
        return new WeeklyReportTrigger(useCase, Clock.fixed(Instant.parse(now), ZoneOffset.UTC), 7);
    }
}
