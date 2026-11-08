package com.feedback.report.domain.model;

import java.util.Arrays;
import java.util.Optional;

/** Urgency level as stored in the Evaluations table. */
public enum Urgency {
    HIGH("high"),
    MEDIUM("medium"),
    LOW("low");

    private final String value;

    Urgency(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Optional<Urgency> fromValue(String value) {
        return Arrays.stream(values()).filter(urgency -> urgency.value.equals(value)).findFirst();
    }
}
