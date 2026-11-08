package com.feedback.report.adapter.out.ses;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.feedback.report.domain.model.EmailMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@ExtendWith(MockitoExtension.class)
class SesEmailSenderTest {

    @Mock
    private SesClient client;

    @Captor
    private ArgumentCaptor<SendEmailRequest> request;

    @Test
    void should_sendFromSenderToOneRecipientWithTextAndHtml() {
        // given
        var sender = new SesEmailSender(client, "sender@example.com");

        // when
        sender.send("admin@example.com", new EmailMessage("Subject", "Text body", "<p>Html body</p>"));

        // then
        verify(client).sendEmail(request.capture());
        SendEmailRequest sent = request.getValue();
        assertThat(sent.source()).isEqualTo("sender@example.com");
        assertThat(sent.destination().toAddresses()).containsExactly("admin@example.com");
        assertThat(sent.destination().hasCcAddresses()).isFalse();
        assertThat(sent.message().subject().data()).isEqualTo("Subject");
        assertThat(sent.message().body().text().data()).isEqualTo("Text body");
        assertThat(sent.message().body().html().data()).isEqualTo("<p>Html body</p>");
        assertThat(sent.message().body().html().charset()).isEqualTo("UTF-8");
    }
}
