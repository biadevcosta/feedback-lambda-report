package com.feedback.report.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/** An evaluation as read for the report (the index doesn't project userId). */
public record Evaluation(String evaluationId, String description, int score, Urgency urgency,
        Instant submittedAt, LocalDate day) {

    public Evaluation {
        Objects.requireNonNull(evaluationId, "evaluationId");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(urgency, "urgency");
        Objects.requireNonNull(submittedAt, "submittedAt");
        Objects.requireNonNull(day, "day");
    }
}
