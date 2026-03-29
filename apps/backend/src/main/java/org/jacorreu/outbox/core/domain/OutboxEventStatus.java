package org.jacorreu.outbox.core.domain;

public enum OutboxEventStatus {
    PENDING,
    PROCESSED,
    FAILED
}
