package org.jacorreu.onboarding.infra.scheduler;

import org.jacorreu.onboarding.infra.handler.OnboardingEventHandler;
import org.jacorreu.outbox.core.domain.OutboxEvent;
import org.jacorreu.outbox.core.domain.OutboxEventStatus;
import org.jacorreu.outbox.core.gateway.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);

    private final OutboxEventRepository repository;
    private final OnboardingEventHandler handler;

    public OutboxScheduler(OutboxEventRepository repository, OnboardingEventHandler handler) {
        this.repository = repository;
        this.handler = handler;
    }

    @Scheduled(fixedRate = 10000)
    public void processOutboxEvents() {
        List<OutboxEvent> events = repository.findAllByStatusOrderByCreatedAt(OutboxEventStatus.PENDING);

        for (var event : events) {
            try {
                if (handler.canHandle(event)) {
                    handler.handle(event);
                    repository.updateStatus(event.id(), OutboxEventStatus.PROCESSED);
                    log.info("Event processed successfully: {}", event.id());
                }
            } catch (Exception e) {
                log.error("Failed to process event: {}", event.id(), e);
                repository.updateStatus(event.id(), OutboxEventStatus.FAILED);
            }
        }
    }
}
