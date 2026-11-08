package com.feedback.report.domain.service;

import com.feedback.report.domain.model.EmailMessage;
import com.feedback.report.domain.model.Evaluation;
import com.feedback.report.domain.model.Urgency;
import com.feedback.report.domain.model.WeeklyReport;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/** Builds the weekly report email (plain text and simple HTML). Pure. */
public final class ReportEmailRenderer {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Brasília has no daylight saving time, so a fixed UTC-3 offset is enough
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneOffset.ofHours(-3));

    public EmailMessage render(WeeklyReport report) {
        String subject = "[Weekly report] Evaluations from " + DATE.format(report.period().start())
                + " to " + DATE.format(report.period().end());
        return new EmailMessage(subject, text(report), html(report));
    }

    private static String text(WeeklyReport report) {
        StringBuilder text = new StringBuilder("Weekly evaluation report\n\n");
        if (report.isEmpty()) {
            return text.append("No evaluations were submitted in this period.\n").toString();
        }

        text.append("Total evaluations: ").append(report.total()).append('\n')
                .append("Average score: ").append(average(report)).append("\n\n")
                .append("Evaluations per day:\n");
        report.period().days().forEach(day ->
                text.append("  ").append(DATE.format(day)).append(": ").append(report.countByDay().get(day)).append('\n'));

        text.append("\nEvaluations per urgency:\n");
        for (Urgency urgency : Urgency.values()) {
            text.append("  ").append(urgency.value()).append(": ").append(report.countByUrgency().get(urgency)).append('\n');
        }

        text.append("\nEvaluations:\n");
        for (Evaluation evaluation : report.evaluations()) {
            text.append("  - ").append(DATE_TIME.format(evaluation.submittedAt()))
                    .append(" | ").append(evaluation.urgency().value())
                    .append(" | ").append(evaluation.description()).append('\n');
        }
        return text.toString();
    }

    private static String html(WeeklyReport report) {
        StringBuilder html = new StringBuilder("<html><body style=\"font-family: Arial, sans-serif\">")
                .append("<h2>Weekly evaluation report</h2>")
                .append("<p>").append(DATE.format(report.period().start())).append(" to ")
                .append(DATE.format(report.period().end())).append(" (Brasília)</p>");
        if (report.isEmpty()) {
            return html.append("<p>No evaluations were submitted in this period.</p></body></html>").toString();
        }

        html.append("<p><strong>Total evaluations:</strong> ").append(report.total())
                .append("<br><strong>Average score:</strong> ").append(average(report)).append("</p>");

        html.append("<h3>Evaluations per day</h3><table border=\"1\" cellpadding=\"4\" cellspacing=\"0\">")
                .append("<tr><th>Day</th><th>Evaluations</th></tr>");
        report.period().days().forEach(day -> row(html, DATE.format(day), report.countByDay().get(day)));
        html.append("</table>");

        html.append("<h3>Evaluations per urgency</h3><table border=\"1\" cellpadding=\"4\" cellspacing=\"0\">")
                .append("<tr><th>Urgency</th><th>Evaluations</th></tr>");
        for (Urgency urgency : Urgency.values()) {
            row(html, urgency.value(), report.countByUrgency().get(urgency));
        }
        html.append("</table>");

        html.append("<h3>Evaluations</h3><table border=\"1\" cellpadding=\"4\" cellspacing=\"0\">")
                .append("<tr><th>Submitted at</th><th>Urgency</th><th>Description</th></tr>");
        // evaluations are already sorted by submission time by the calculator
        report.evaluations().forEach(evaluation ->
                html.append("<tr><td>").append(DATE_TIME.format(evaluation.submittedAt()))
                        .append("</td><td>").append(evaluation.urgency().value())
                        // Student-written text: escaped so it can't inject HTML into the email
                        .append("</td><td>").append(escapeHtml(evaluation.description()))
                        .append("</td></tr>"));
        return html.append("</table></body></html>").toString();
    }

    private static void row(StringBuilder html, String label, Long count) {
        html.append("<tr><td>").append(label).append("</td><td>").append(count).append("</td></tr>");
    }

    private static String average(WeeklyReport report) {
        return report.averageScore().map(Object::toString).orElse("-");
    }

    static String escapeHtml(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            switch (c) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&#39;");
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }
}
