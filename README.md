# feedback-lambda-report

**Weekly report** Lambda for the Class Feedback Platform (Tech Challenge phase 4).

Scheduled by **EventBridge** every Monday at 8am Brasília time (`cron(0 11 ? * MON *)`, in UTC). Queries the last 7 days in DynamoDB and emails the summary to admins via SES.

- **AWS function:** `feedback-report` · **Runtime:** Java 21 (Maven) · **Region:** `sa-east-1`
- **Permissions:** `dynamodb:Query` on the `by-day` index and `ses:SendEmail`

## Report content

- Average score over the last 7 days
- Count of evaluations per day and per urgency level
- List with description, urgency and submission date

## Structure (in progress)

```
pom.xml
src/main/java/com/feedback/report/ReportHandler.java
src/test/java/...              # JUnit5 + Mockito, average/count tests
.github/workflows/ci-cd.yml
```

## Related repositories

`feedback-infra` · `feedback-lambda-ingestion` · `feedback-lambda-notification`

## Status

Track progress in [PLANO.md](PLANO.md).
