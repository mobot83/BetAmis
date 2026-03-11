package com.betamis.prediction.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PredictionClosed(String id, String matchId, Instant occurredAt) {
    public static PredictionClosed of(String matchId) {
        return new PredictionClosed(
            UUID.randomUUID().toString(),
            matchId,
            Instant.now()
        );
    }
}
