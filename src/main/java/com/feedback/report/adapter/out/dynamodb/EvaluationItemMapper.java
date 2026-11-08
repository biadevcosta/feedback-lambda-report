package com.feedback.report.adapter.out.dynamodb;

import com.feedback.report.domain.model.Evaluation;
import com.feedback.report.domain.model.Urgency;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/** Reads an item projected by the by-day index (data contract shared by the 3 functions). */
final class EvaluationItemMapper {

    private EvaluationItemMapper() {
    }

    static Evaluation fromItem(Map<String, AttributeValue> item) {
        String urgency = string(item, "urgency");
        return new Evaluation(
                string(item, "evaluationId"),
                string(item, "description"),
                Integer.parseInt(number(item, "score")),
                Urgency.fromValue(urgency)
                        .orElseThrow(() -> new IllegalStateException("Item with unknown urgency")),
                Instant.parse(string(item, "submittedAt")),
                LocalDate.parse(string(item, "day")));
    }

    private static String string(Map<String, AttributeValue> item, String name) {
        AttributeValue value = item.get(name);
        if (value == null || value.s() == null) {
            throw new IllegalStateException("Item without " + name);
        }
        return value.s();
    }

    private static String number(Map<String, AttributeValue> item, String name) {
        AttributeValue value = item.get(name);
        if (value == null || value.n() == null) {
            throw new IllegalStateException("Item without " + name);
        }
        return value.n();
    }
}
