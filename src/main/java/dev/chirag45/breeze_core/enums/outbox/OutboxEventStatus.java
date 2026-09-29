package dev.chirag45.breeze_core.enums.outbox;

public enum OutboxEventStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    DEAD
}
