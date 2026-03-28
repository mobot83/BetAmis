package com.betamis.prediction.interfaces.rest.dto;

import com.betamis.prediction.domain.model.prediction.Prediction;

public record PredictionResponse(
        String id,
        String matchId,
        String userId,
        int homeScore,
        int awayScore,
        String status
) {
    public static PredictionResponse from(Prediction prediction) {
        return new PredictionResponse(
                prediction.getId(),
                prediction.getMatchId(),
                prediction.getUserId(),
                prediction.getScore().homeTeamScore(),
                prediction.getScore().awayTeamScore(),
                prediction.getStatus().name()
        );
    }
}
