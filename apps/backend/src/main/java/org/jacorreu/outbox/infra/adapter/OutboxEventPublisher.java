package org.jacorreu.outbox.infra.adapter;

import org.jacorreu.onboarding.core.gateway.EventPublisher;
import org.jacorreu.outbox.application.OutboxEventUseCase;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OutboxEventPublisher implements EventPublisher {

    private final OutboxEventUseCase outboxEventUseCase;

    public OutboxEventPublisher(OutboxEventUseCase outboxEventUseCase) {
        this.outboxEventUseCase = outboxEventUseCase;
    }

    @Override
    public void publish(UUID aggregateId, String eventType, String payload) {
        outboxEventUseCase.publish(aggregateId, eventType, payload);
    }
}
