package org.jacorreu.onboarding.core.gateway;

import java.util.UUID;

public interface EventPublisher {
    void publish(UUID aggregateId, String eventType, String payload);
}
