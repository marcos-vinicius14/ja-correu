package org.jacorreu.outbox.infra.persistence.entity;

import jakarta.persistence.*;
import org.jacorreu.outbox.core.domain.OutboxEvent;
import org.jacorreu.outbox.core.domain.OutboxEventStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
public class OutboxEventEntity {

    @Id
    private UUID id;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxEventStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    public OutboxEventEntity() {}

    public static OutboxEventEntity fromDomain(OutboxEvent event) {
        var entity = new OutboxEventEntity();
        entity.id = event.id();
        entity.aggregateId = event.aggregateId();
        entity.eventType = event.eventType();
        entity.payload = event.payload();
        entity.status = event.status();
        entity.createdAt = event.createdAt();
        entity.processedAt = event.processedAt();
        return entity;
    }

    public OutboxEvent toDomain() {
        return new OutboxEvent(
                id,
                aggregateId,
                eventType,
                payload,
                status,
                createdAt,
                processedAt
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAggregateId() { return aggregateId; }
    public void setAggregateId(UUID aggregateId) { this.aggregateId = aggregateId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public OutboxEventStatus getStatus() { return status; }
    public void setStatus(OutboxEventStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
