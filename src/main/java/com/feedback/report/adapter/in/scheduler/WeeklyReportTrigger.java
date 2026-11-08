package com.feedback.report.adapter.in.scheduler;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.feedback.report.application.usecase.SendWeeklyReportCommand;
import com.feedback.report.application.usecase.SendWeeklyReportResult;
import com.feedback.report.application.usecase.SendWeeklyReportUseCase;
import com.feedback.report.domain.model.ReportPeriod;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Runs the report for the complete days before the execution date in Brasília. The event content
 * is ignored, so a manual invocation with an empty event behaves like the schedule. Exceptions are
 * not caught: EventBridge retries the invocation.
 */
public final class WeeklyReportTrigger {

    // Brasília has no daylight saving time, so a fixed UTC-3 offset defines "today"
    private static final ZoneOffset BRASILIA = ZoneOffset.ofHours(-3);

    private final SendWeeklyReportUseCase useCase;
    private final Clock clock;
    private final int days;

    public WeeklyReportTrigger(SendWeeklyReportUseCase useCase, Clock clock, int days) {
        this.useCase = useCase;
        this.clock = clock;
        this.days = days;
    }

    public void handle(ScheduledEvent event, LambdaLogger logger) {
        LocalDate today = clock.instant().atOffset(BRASILIA).toLocalDate();
        ReportPeriod period = ReportPeriod.completeDaysBefore(today, days);

        SendWeeklyReportResult result = useCase.execute(new SendWeeklyReportCommand(period));

        // LGPD: counts only, never emails or descriptions
        logger.log("Weekly report " + period.start() + " to " + period.end() + ": "
                + result.evaluations() + " evaluations, sent to " + result.recipients() + " admins");
    }
}
