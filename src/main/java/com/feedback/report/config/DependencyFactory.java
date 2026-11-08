package com.feedback.report.config;

import com.feedback.report.adapter.in.scheduler.WeeklyReportTrigger;
import com.feedback.report.adapter.out.cognito.CognitoAdminDirectory;
import com.feedback.report.adapter.out.dynamodb.DynamoDbEvaluationQueryRepository;
import com.feedback.report.adapter.out.ses.SesEmailSender;
import com.feedback.report.application.usecase.SendWeeklyReportUseCase;
import com.feedback.report.domain.service.ReportEmailRenderer;
import com.feedback.report.domain.service.WeeklyReportCalculator;
import java.time.Clock;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ses.SesClient;

/** Composition root: builds the object graph once per execution environment. */
public final class DependencyFactory {

    private final EnvironmentConfig config;
    private final DynamoDbClient dynamoDb;
    private final CognitoIdentityProviderClient cognito;
    private final SesClient ses;

    private DependencyFactory(EnvironmentConfig config) {
        this.config = config;
        // Region comes from AWS_REGION, set by Lambda
        this.dynamoDb = DynamoDbClient.builder().httpClient(UrlConnectionHttpClient.create()).build();
        this.cognito = CognitoIdentityProviderClient.builder().httpClient(UrlConnectionHttpClient.create()).build();
        this.ses = SesClient.builder().httpClient(UrlConnectionHttpClient.create()).build();
    }

    public static DependencyFactory fromEnvironment() {
        return new DependencyFactory(EnvironmentConfig.fromEnvironment());
    }

    public WeeklyReportTrigger weeklyReportTrigger() {
        var useCase = new SendWeeklyReportUseCase(
                new DynamoDbEvaluationQueryRepository(dynamoDb, config.tableName(), config.indexName()),
                new CognitoAdminDirectory(cognito, config.userPoolId(), config.adminGroup()),
                new SesEmailSender(ses, config.sesSender()),
                new WeeklyReportCalculator(),
                new ReportEmailRenderer());
        return new WeeklyReportTrigger(useCase, Clock.systemUTC(), config.days());
    }
}
