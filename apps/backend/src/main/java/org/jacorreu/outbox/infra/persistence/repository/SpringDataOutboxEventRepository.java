package org.jacorreu.outbox.infra.persistence.repository;

import org.jacorreu.outbox.core.domain.OutboxEventStatus;
import org.jacorreu.outbox.infra.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataOutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Query("SELECT e FROM OutboxEventEntity e WHERE e.status = :status ORDER BY e.createdAt")
    List<OutboxEventEntity> findAllByStatusOrderByCreatedAt(@Param("status") OutboxEventStatus status);
}
