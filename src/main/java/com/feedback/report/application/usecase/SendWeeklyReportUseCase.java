package com.feedback.report.application.usecase;

import com.feedback.report.application.port.out.AdminDirectory;
import com.feedback.report.application.port.out.EmailSender;
import com.feedback.report.application.port.out.EvaluationQueryRepository;
import com.feedback.report.domain.model.EmailMessage;
import com.feedback.report.domain.model.Evaluation;
import com.feedback.report.domain.model.WeeklyReport;
import com.feedback.report.domain.service.ReportEmailRenderer;
import com.feedback.report.domain.service.WeeklyReportCalculator;
import java.util.List;

/** Builds the report for the period and emails it to every verified admin. */
public final class SendWeeklyReportUseCase {

    private final EvaluationQueryRepository repository;
    private final AdminDirectory adminDirectory;
    private final EmailSender emailSender;
    private final WeeklyReportCalculator calculator;
    private final ReportEmailRenderer renderer;

    public SendWeeklyReportUseCase(EvaluationQueryRepository repository, AdminDirectory adminDirectory,
            EmailSender emailSender, WeeklyReportCalculator calculator, ReportEmailRenderer renderer) {
        this.repository = repository;
        this.adminDirectory = adminDirectory;
        this.emailSender = emailSender;
        this.calculator = calculator;
        this.renderer = renderer;
    }

    /** Also sends the email when the week had no evaluations, so admins know the report ran. */
    public SendWeeklyReportResult execute(SendWeeklyReportCommand command) {
        // One query per day of the period: the by-day index is keyed by day
        List<Evaluation> evaluations = command.period().days().stream()
                .flatMap(day -> repository.findByDay(day).stream())
                .toList();
        WeeklyReport report = calculator.calculate(command.period(), evaluations);

        List<String> recipients = adminDirectory.verifiedAdminEmails();
        if (!recipients.isEmpty()) {
            EmailMessage message = renderer.render(report);
            recipients.forEach(recipient -> emailSender.send(recipient, message));
        }
        return new SendWeeklyReportResult(report.total(), recipients.size());
    }
}
