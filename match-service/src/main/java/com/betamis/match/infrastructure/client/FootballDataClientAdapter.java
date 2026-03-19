package com.betamis.match.infrastructure.client;

import com.betamis.match.domain.model.match.ExternalMatch;
import com.betamis.match.domain.port.out.MatchDataProvider;
import com.betamis.match.infrastructure.client.dto.FootballMatchResponse;
import com.betamis.match.infrastructure.client.dto.FootballScoreDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FootballDataClientAdapter implements MatchDataProvider {

    private final FootballDataClient footballDataClient;

    public FootballDataClientAdapter(@RestClient FootballDataClient footballDataClient) {
        this.footballDataClient = footballDataClient;
    }

    @Override
    public List<ExternalMatch> getMatchesByCompetition(String competitionId) {
        return footballDataClient.getMatchesByCompetition(competitionId, null, null)
                .matches()
                .stream()
                .map(FootballDataClientAdapter::toExternalMatch)
                .toList();
    }

    private static ExternalMatch toExternalMatch(FootballMatchResponse r) {
        int homeScore = Optional.ofNullable(r.score())
                .map(FootballScoreDto::fullTime)
                .map(FootballScoreDto.HalfDto::home)
                .orElse(0);
        int awayScore = Optional.ofNullable(r.score())
                .map(FootballScoreDto::fullTime)
                .map(FootballScoreDto.HalfDto::away)
                .orElse(0);
        return new ExternalMatch(
                r.id(),
                r.status(),
                String.valueOf(r.homeTeam().id()),
                String.valueOf(r.awayTeam().id()),
                homeScore,
                awayScore
        );
    }
}
