package com.feedback.report.adapter.out.ses;

import com.feedback.report.application.port.out.EmailSender;
import com.feedback.report.domain.model.EmailMessage;
import java.nio.charset.StandardCharsets;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

/** Sends emails through SES from the verified sender identity. */
public final class SesEmailSender implements EmailSender {

    private static final String CHARSET = StandardCharsets.UTF_8.name();

    private final SesClient client;
    private final String sender;

    public SesEmailSender(SesClient client, String sender) {
        this.client = client;
        this.sender = sender;
    }

    @Override
    public void send(String recipient, EmailMessage message) {
        client.sendEmail(SendEmailRequest.builder()
                .source(sender)
                .destination(destination -> destination.toAddresses(recipient))
                .message(email -> email
                        .subject(content(message.subject()))
                        .body(body -> body.text(content(message.textBody())).html(content(message.htmlBody()))))
                .build());
    }

    private static Content content(String data) {
        return Content.builder().data(data).charset(CHARSET).build();
    }
}
