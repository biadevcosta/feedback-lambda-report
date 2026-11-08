package com.feedback.report.adapter.out.dynamodb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.feedback.report.domain.model.Evaluation;
import com.feedback.report.domain.model.Urgency;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

@ExtendWith(MockitoExtension.class)
class DynamoDbEvaluationQueryRepositoryTest {

    private static final LocalDate DAY = LocalDate.parse("2026-11-03");

    @Mock
    private DynamoDbClient client;

    @Captor
    private ArgumentCaptor<QueryRequest> request;

    private DynamoDbEvaluationQueryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new DynamoDbEvaluationQueryRepository(client, "Evaluations", "by-day");
    }

    @Test
    void should_queryTheIndexByDay() {
        // given
        when(client.query(any(QueryRequest.class))).thenReturn(QueryResponse.builder().items(List.of()).build());

        // when
        repository.findByDay(DAY);

        // then
        verify(client).query(request.capture());
        QueryRequest sent = request.getValue();
        assertThat(sent.tableName()).isEqualTo("Evaluations");
        assertThat(sent.indexName()).isEqualTo("by-day");
        assertThat(sent.keyConditionExpression()).isEqualTo("#day = :day");
        assertThat(sent.expressionAttributeNames()).containsEntry("#day", "day");
        assertThat(sent.expressionAttributeValues()).containsEntry(":day", AttributeValue.fromS("2026-11-03"));
    }

    @Test
    void should_readAllPages_when_resultIsPaginated() {
        Map<String, AttributeValue> lastKey = Map.of("evaluationId", AttributeValue.fromS("eval-1"));
        when(client.query(any(QueryRequest.class))).thenReturn(
                QueryResponse.builder().items(item("eval-1", "2")).lastEvaluatedKey(lastKey).build(),
                QueryResponse.builder().items(item("eval-2", "9")).build());

        List<Evaluation> evaluations = repository.findByDay(DAY);

        assertThat(evaluations).extracting(Evaluation::evaluationId).containsExactly("eval-1", "eval-2");
        verify(client, times(2)).query(request.capture());
        assertThat(request.getAllValues().get(0).exclusiveStartKey()).isEmpty();
        assertThat(request.getAllValues().get(1).exclusiveStartKey()).isEqualTo(lastKey);
    }

    @Test
    void should_mapEveryAttribute() {
        when(client.query(any(QueryRequest.class)))
                .thenReturn(QueryResponse.builder().items(item("eval-1", "2")).build());

        Evaluation evaluation = repository.findByDay(DAY).get(0);

        assertThat(evaluation).isEqualTo(new Evaluation("eval-1", "Too fast", 2, Urgency.HIGH,
                Instant.parse("2026-11-03T15:00:00.000Z"), DAY));
    }

    @ParameterizedTest(name = "without {0}")
    @ValueSource(strings = {"evaluationId", "description", "score", "urgency", "submittedAt", "day"})
    void should_fail_when_itemMissesAnAttribute(String attribute) {
        Map<String, AttributeValue> item = new HashMap<>(item("eval-1", "2"));
        item.remove(attribute);
        when(client.query(any(QueryRequest.class))).thenReturn(QueryResponse.builder().items(item).build());

        assertThatThrownBy(() -> repository.findByDay(DAY))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Item without " + attribute);
    }

    @Test
    void should_fail_when_urgencyIsUnknown() {
        Map<String, AttributeValue> item = new HashMap<>(item("eval-1", "2"));
        item.put("urgency", AttributeValue.fromS("critical"));
        when(client.query(any(QueryRequest.class))).thenReturn(QueryResponse.builder().items(item).build());

        assertThatThrownBy(() -> repository.findByDay(DAY)).isInstanceOf(IllegalStateException.class);
    }

    private static Map<String, AttributeValue> item(String evaluationId, String score) {
        return Map.of(
                "evaluationId", AttributeValue.fromS(evaluationId),
                "description", AttributeValue.fromS("Too fast"),
                "score", AttributeValue.fromN(score),
                "urgency", AttributeValue.fromS(Integer.parseInt(score) <= 3 ? "high" : "low"),
                "submittedAt", AttributeValue.fromS("2026-11-03T15:00:00.000Z"),
                "day", AttributeValue.fromS("2026-11-03"));
    }
}
