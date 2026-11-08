package com.feedback.report.application.usecase;

import com.feedback.report.domain.model.ReportPeriod;

/** Input of SendWeeklyReportUseCase: the days the report covers. */
public record SendWeeklyReportCommand(ReportPeriod period) {
}
