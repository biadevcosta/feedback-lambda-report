package com.feedback.report.application.port.out;

import com.feedback.report.domain.model.EmailMessage;

/** Delivers an email to one recipient. */
public interface EmailSender {

    void send(String recipient, EmailMessage message);
}
