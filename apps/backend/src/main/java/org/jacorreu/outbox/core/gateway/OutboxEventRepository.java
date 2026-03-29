package org.jacorreu.outbox.core.gateway;

import org.jacorreu.outbox.core.domain.OutboxEvent;
import org.jacorreu.outbox.core.domain.OutboxEventStatus;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository {
    OutboxEvent save(OutboxEvent event);

    List<OutboxEvent> findAllByStatusOrderByCreatedAt(OutboxEventStatus status);

    void updateStatus(UUID id, OutboxEventStatus status);
}
