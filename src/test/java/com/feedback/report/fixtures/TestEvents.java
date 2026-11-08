package com.feedback.report.fixtures;

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Loads real Lambda event payloads from src/test/resources/events/ using the same
 * serializer the Lambda runtime uses, so field names and casing match production.
 */
public final class TestEvents {

    private TestEvents() {
    }

    public static ScheduledEvent scheduled(String fileName) {
        return load(fileName, ScheduledEvent.class);
    }


    public static <T> T load(String fileName, Class<T> type) {
        try (InputStream json = TestEvents.class.getResourceAsStream("/events/" + fileName)) {
            if (json == null) {
                throw new IllegalArgumentException("Event file not found: events/" + fileName);
            }
            return LambdaEventSerializers.serializerFor(type, TestEvents.class.getClassLoader()).fromJson(json);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
