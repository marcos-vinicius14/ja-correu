package org.jacorreu.outbox.application;

import org.jacorreu.outbox.core.domain.OutboxEvent;
import org.jacorreu.outbox.core.gateway.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OutboxEventUseCase {

    private final OutboxEventRepository repository;

    public OutboxEventUseCase(OutboxEventRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(UUID aggregateId, String eventType, String payload) {
        var event = OutboxEvent.create(aggregateId, eventType, payload);
        repository.save(event);
    }
}
