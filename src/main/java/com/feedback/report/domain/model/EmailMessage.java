package com.feedback.report.domain.model;

/** Email content with a plain-text and an HTML version of the same body. */
public record EmailMessage(String subject, String textBody, String htmlBody) {
}
