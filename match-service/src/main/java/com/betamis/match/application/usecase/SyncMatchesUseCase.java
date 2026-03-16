package com.betamis.match.application.usecase;

import com.betamis.match.domain.event.MatchFinished;
import com.betamis.match.domain.event.MatchStarted;
import com.betamis.match.domain.model.match.Match;
import com.betamis.match.domain.model.match.MatchStatus;
import com.betamis.match.domain.port.in.SyncMatches;
import com.betamis.match.domain.port.out.MatchEventPublisher;
import com.betamis.match.domain.port.out.MatchRepository;
import com.betamis.match.infrastructure.client.FootballDataClient;
import com.betamis.match.infrastructure.client.dto.FootballMatchResponse;
import com.betamis.match.infrastructure.client.dto.FootballScoreDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Optional;

@ApplicationScoped
public class SyncMatchesUseCase implements SyncMatches {

    private final FootballDataClient footballDataClient;
    private final MatchRepository matchRepository;
    private final MatchEventPublisher eventPublisher;

    public SyncMatchesUseCase(
            @RestClient FootballDataClient footballDataClient,
            MatchRepository matchRepository,
            MatchEventPublisher eventPublisher
    ) {
        this.footballDataClient = footballDataClient;
        this.matchRepository = matchRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void syncByCompetition(String competitionId) {
        var response = footballDataClient.getMatchesByCompetition(competitionId, null, null);

        for (FootballMatchResponse external : response.matches()) {
            Optional<Match> existing = matchRepository.findByExternalId(external.id());
            MatchStatus newStatus = toStatus(external.status());

            Match match = existing
                    .map(m -> m.withUpdate(homeScore(external), awayScore(external), newStatus))
                    .orElseGet(() -> Match.fromExternal(
                            external.id(),
                            String.valueOf(external.homeTeam().id()),
                            String.valueOf(external.awayTeam().id()),
                            homeScore(external),
                            awayScore(external),
                            newStatus
                    ));

            matchRepository.save(match);
            emitTransitionEvent(existing, newStatus, match);
        }
    }

    private void emitTransitionEvent(Optional<Match> existing, MatchStatus newStatus, Match match) {
        existing.ifPresent(prev -> {
            if (prev.getStatus() == newStatus) return;
            if (newStatus == MatchStatus.STARTED) {
                eventPublisher.publish(MatchStarted.of(match.getId(), match.getHomeTeamId(), match.getAwayTeamId()));
            } else if (newStatus == MatchStatus.FINISHED) {
                eventPublisher.publish(MatchFinished.of(match.getId(), match.getHomeTeamId(), match.getAwayTeamId(),
                        match.getHomeTeamScore(), match.getAwayTeamScore()));
            }
        });
    }

    private static MatchStatus toStatus(String externalStatus) {
        return switch (externalStatus) {
            case "IN_PLAY", "PAUSED" -> MatchStatus.STARTED;
            case "FINISHED", "AWARDED" -> MatchStatus.FINISHED;
            default -> MatchStatus.PLANNED;
        };
    }

    private static int homeScore(FootballMatchResponse r) {
        return Optional.ofNullable(r.score())
                .map(FootballScoreDto::fullTime)
                .map(FootballScoreDto.HalfDto::home)
                .orElse(0);
    }

    private static int awayScore(FootballMatchResponse r) {
        return Optional.ofNullable(r.score())
                .map(FootballScoreDto::fullTime)
                .map(FootballScoreDto.HalfDto::away)
                .orElse(0);
    }
}
