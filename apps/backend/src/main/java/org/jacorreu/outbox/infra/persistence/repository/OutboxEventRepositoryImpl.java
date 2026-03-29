package org.jacorreu.outbox.infra.persistence.repository;

import org.jacorreu.outbox.core.domain.OutboxEvent;
import org.jacorreu.outbox.core.domain.OutboxEventStatus;
import org.jacorreu.outbox.core.gateway.OutboxEventRepository;
import org.jacorreu.outbox.infra.persistence.entity.OutboxEventEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public class OutboxEventRepositoryImpl implements OutboxEventRepository {

    private final SpringDataOutboxEventRepository repository;

    public OutboxEventRepositoryImpl(SpringDataOutboxEventRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEvent save(OutboxEvent event) {
        return repository.save(OutboxEventEntity.fromDomain(event)).toDomain();
    }

    @Override
    public List<OutboxEvent> findAllByStatusOrderByCreatedAt(OutboxEventStatus status) {
        return repository.findAllByStatusOrderByCreatedAt(status).stream()
                .map(OutboxEventEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void updateStatus(UUID id, OutboxEventStatus status) {
        var entity = repository.findById(id);
        if (entity.isPresent()) {
            var e = entity.get();
            e.setStatus(status);
            if (status == OutboxEventStatus.PROCESSED || status == OutboxEventStatus.FAILED) {
                e.setProcessedAt(java.time.Instant.now());
            }
            repository.save(e);
        }
    }
}
