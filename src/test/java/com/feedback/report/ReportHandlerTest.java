package com.feedback.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.feedback.report.adapter.in.scheduler.WeeklyReportTrigger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportHandlerTest {

    @Mock
    private WeeklyReportTrigger trigger;

    @Mock
    private Context context;

    @Mock
    private LambdaLogger logger;

    @Test
    void should_delegateToTrigger() {
        // given
        var event = new ScheduledEvent();
        when(context.getLogger()).thenReturn(logger);

        // when
        Void result = new ReportHandler(trigger).handleRequest(event, context);

        // then
        verify(trigger).handle(event, logger);
        assertThat(result).isNull();
    }
}
