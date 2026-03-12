package com.betamis.match.infrastructure.client.dto;

public record FootballScoreDto(
        String winner,
        HalfDto fullTime,
        HalfDto halfTime
) {
    public record HalfDto(Integer home, Integer away) {}
}
