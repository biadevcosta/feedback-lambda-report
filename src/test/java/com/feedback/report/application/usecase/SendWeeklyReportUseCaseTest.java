package com.feedback.report.application.usecase;

import static com.feedback.report.fixtures.EvaluationFixtures.anEvaluation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.feedback.report.application.port.out.AdminDirectory;
import com.feedback.report.application.port.out.EmailSender;
import com.feedback.report.application.port.out.EvaluationQueryRepository;
import com.feedback.report.domain.model.EmailMessage;
import com.feedback.report.domain.model.ReportPeriod;
import com.feedback.report.domain.service.ReportEmailRenderer;
import com.feedback.report.domain.service.WeeklyReportCalculator;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendWeeklyReportUseCaseTest {

    private static final ReportPeriod PERIOD = ReportPeriod.completeDaysBefore(LocalDate.parse("2026-11-09"), 7);
    private static final SendWeeklyReportCommand COMMAND = new SendWeeklyReportCommand(PERIOD);

    @Mock
    private EvaluationQueryRepository repository;

    @Mock
    private AdminDirectory adminDirectory;

    @Mock
    private EmailSender emailSender;

    @Captor
    private ArgumentCaptor<LocalDate> queriedDay;

    @Captor
    private ArgumentCaptor<EmailMessage> message;

    private SendWeeklyReportUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SendWeeklyReportUseCase(repository, adminDirectory, emailSender,
                new WeeklyReportCalculator(), new ReportEmailRenderer());
    }

    @Test
    void should_queryEachDayAndEmailEachAdmin() {
        // given
        when(repository.findByDay(any(LocalDate.class))).thenReturn(List.of());
        when(repository.findByDay(LocalDate.parse("2026-11-03")))
                .thenReturn(List.of(anEvaluation().withScore(4).submittedAt("2026-11-03T15:00:00Z").build()));
        when(adminDirectory.verifiedAdminEmails()).thenReturn(List.of("a@example.com", "b@example.com"));

        // when
        SendWeeklyReportResult result = useCase.execute(COMMAND);

        // then
        assertThat(result).isEqualTo(new SendWeeklyReportResult(1, 2));
        verify(repository, org.mockito.Mockito.times(7)).findByDay(queriedDay.capture());
        assertThat(queriedDay.getAllValues()).containsExactlyElementsOf(PERIOD.days());
        verify(emailSender).send(eq("a@example.com"), message.capture());
        verify(emailSender).send(eq("b@example.com"), any(EmailMessage.class));
        assertThat(message.getValue().textBody()).contains("Total evaluations: 1");
    }

    @Test
    void should_stillSendReport_when_weekHasNoEvaluations() {
        when(repository.findByDay(any(LocalDate.class))).thenReturn(List.of());
        when(adminDirectory.verifiedAdminEmails()).thenReturn(List.of("a@example.com"));

        SendWeeklyReportResult result = useCase.execute(COMMAND);

        assertThat(result).isEqualTo(new SendWeeklyReportResult(0, 1));
        verify(emailSender).send(eq("a@example.com"), message.capture());
        assertThat(message.getValue().textBody()).contains("No evaluations");
    }

    @Test
    void should_sendNothing_when_thereAreNoAdmins() {
        when(repository.findByDay(any(LocalDate.class))).thenReturn(List.of());
        when(adminDirectory.verifiedAdminEmails()).thenReturn(List.of());

        assertThat(useCase.execute(COMMAND).recipients()).isZero();
        verifyNoInteractions(emailSender);
    }
}
