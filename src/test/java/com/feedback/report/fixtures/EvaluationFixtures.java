package com.feedback.report.fixtures;

import com.feedback.report.domain.model.Evaluation;
import com.feedback.report.domain.model.Urgency;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/** Test Data Builder: valid defaults, tests override only what matters to them. */
public final class EvaluationFixtures {

    private static int sequence;

    private String evaluationId = "eval-" + (++sequence);
    private String description = "Great class";
    private int score = 8;
    private Urgency urgency = Urgency.LOW;
    private Instant submittedAt = Instant.parse("2026-11-03T15:00:00Z");

    private EvaluationFixtures() {
    }

    public static EvaluationFixtures anEvaluation() {
        return new EvaluationFixtures();
    }

    /** Sets the score and the urgency the ingestion would have derived from it. */
    public EvaluationFixtures withScore(int score) {
        this.score = score;
        this.urgency = score <= 3 ? Urgency.HIGH : score <= 6 ? Urgency.MEDIUM : Urgency.LOW;
        return this;
    }

    public EvaluationFixtures withDescription(String description) {
        this.description = description;
        return this;
    }

    public EvaluationFixtures submittedAt(String instant) {
        this.submittedAt = Instant.parse(instant);
        return this;
    }

    public Evaluation build() {
        LocalDate day = submittedAt.atOffset(ZoneOffset.ofHours(-3)).toLocalDate();
        return new Evaluation(evaluationId, description, score, urgency, submittedAt, day);
    }
}
