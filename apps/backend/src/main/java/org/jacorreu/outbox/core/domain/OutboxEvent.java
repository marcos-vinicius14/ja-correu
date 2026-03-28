package org.jacorreu.outbox.core.domain;

import com.fasterxml.uuid.Generators;

import java.time.Instant;
import java.util.UUID;

public record OutboxEvent(
        UUID id,
        UUID aggregateId,
        String eventType,
        String payload,
        OutboxEventStatus status,
        Instant createdAt,
        Instant processedAt
) {
    public static OutboxEvent create(UUID aggregateId, String eventType, String payload) {
        return new OutboxEvent(
                Generators.timeBasedEpochGenerator().generate(),
                aggregateId,
                eventType,
                payload,
                OutboxEventStatus.PENDING,
                Instant.now(),
                null
        );
    }

    public OutboxEvent markProcessed() {
        return new OutboxEvent(
                id,
                aggregateId,
                eventType,
                payload,
                OutboxEventStatus.PROCESSED,
                createdAt,
                Instant.now()
        );
    }

    public OutboxEvent markFailed() {
        return new OutboxEvent(
                id,
                aggregateId,
                eventType,
                payload,
                OutboxEventStatus.FAILED,
                createdAt,
                Instant.now()
        );
    }
}
