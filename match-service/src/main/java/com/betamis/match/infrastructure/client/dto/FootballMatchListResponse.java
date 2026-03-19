package com.betamis.match.infrastructure.client.dto;

import java.util.List;

public record FootballMatchListResponse(
        int count,
        List<FootballMatchResponse> matches
) {}
