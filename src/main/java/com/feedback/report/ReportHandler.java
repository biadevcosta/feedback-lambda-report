package com.feedback.report;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.feedback.report.adapter.in.scheduler.WeeklyReportTrigger;
import com.feedback.report.config.DependencyFactory;

/**
 * Lambda entry point (referenced by Terraform). Bootstrap only: the runtime creates one instance
 * per execution environment, so the dependencies built here are reused across invocations.
 */
public class ReportHandler implements RequestHandler<ScheduledEvent, Void> {

    private final WeeklyReportTrigger trigger;

    public ReportHandler() {
        this(DependencyFactory.fromEnvironment().weeklyReportTrigger());
    }

    ReportHandler(WeeklyReportTrigger trigger) {
        this.trigger = trigger;
    }

    @Override
    public Void handleRequest(ScheduledEvent event, Context context) {
        trigger.handle(event, context.getLogger());
        return null;
    }
}
