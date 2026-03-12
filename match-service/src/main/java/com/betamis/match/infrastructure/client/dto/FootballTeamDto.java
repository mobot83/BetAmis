package com.betamis.match.infrastructure.client.dto;

public record FootballTeamDto(
        long id,
        String name,
        String shortName,
        String tla
) {}
