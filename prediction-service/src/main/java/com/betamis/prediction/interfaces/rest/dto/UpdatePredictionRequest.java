package com.betamis.prediction.interfaces.rest.dto;

import java.time.Instant;

public record UpdatePredictionRequest(
        int homeScore,
        int awayScore,
        Instant kickOffTime
) {}
