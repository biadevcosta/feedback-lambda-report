package com.feedback.report.adapter.out.dynamodb;

import com.feedback.report.application.port.out.EvaluationQueryRepository;
import com.feedback.report.domain.model.Evaluation;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/** Queries the by-day index, one day at a time, following pagination. Never scans. */
public final class DynamoDbEvaluationQueryRepository implements EvaluationQueryRepository {

    private final DynamoDbClient client;
    private final String tableName;
    private final String indexName;

    public DynamoDbEvaluationQueryRepository(DynamoDbClient client, String tableName, String indexName) {
        this.client = client;
        this.tableName = tableName;
        this.indexName = indexName;
    }

    @Override
    public List<Evaluation> findByDay(LocalDate day) {
        List<Evaluation> evaluations = new ArrayList<>();
        Map<String, AttributeValue> startKey = null;
        do {
            QueryResponse page = client.query(QueryRequest.builder()
                    .tableName(tableName)
                    .indexName(indexName)
                    // "day" is a DynamoDB reserved word, hence the #day placeholder
                    .keyConditionExpression("#day = :day")
                    .expressionAttributeNames(Map.of("#day", "day"))
                    .expressionAttributeValues(Map.of(":day", AttributeValue.fromS(day.toString())))
                    .exclusiveStartKey(startKey)
                    .build());
            page.items().forEach(item -> evaluations.add(EvaluationItemMapper.fromItem(item)));
            startKey = page.hasLastEvaluatedKey() && !page.lastEvaluatedKey().isEmpty() ? page.lastEvaluatedKey() : null;
        } while (startKey != null);
        return evaluations;
    }
}
