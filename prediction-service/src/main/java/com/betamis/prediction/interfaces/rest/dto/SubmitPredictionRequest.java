package com.betamis.prediction.interfaces.rest.dto;

import java.time.Instant;

public record SubmitPredictionRequest(
        String matchId,
        int homeScore,
        int awayScore,
        Instant kickOffTime
) {}
