package com.betamis.match.application.usecase;

import com.betamis.match.domain.event.MatchFinished;
import com.betamis.match.domain.event.MatchStarted;
import com.betamis.match.domain.model.match.ExternalMatch;
import com.betamis.match.domain.model.match.Match;
import com.betamis.match.domain.model.match.MatchStatus;
import com.betamis.match.domain.port.in.SyncMatches;
import com.betamis.match.domain.port.out.MatchDataProvider;
import com.betamis.match.domain.port.out.MatchEventPublisher;
import com.betamis.match.domain.port.out.MatchRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class SyncMatchesUseCase implements SyncMatches {

    private final MatchDataProvider matchDataProvider;
    private final MatchRepository matchRepository;
    private final MatchEventPublisher eventPublisher;
    private final Counter matchesSyncedCounter;

    public SyncMatchesUseCase(
            MatchDataProvider matchDataProvider,
            MatchRepository matchRepository,
            MatchEventPublisher eventPublisher,
            MeterRegistry registry
    ) {
        this.matchDataProvider = matchDataProvider;
        this.matchRepository = matchRepository;
        this.eventPublisher = eventPublisher;
        this.matchesSyncedCounter = registry.counter("betamis_matches_synced_total");
    }

    @Override
    @Transactional
    public void syncByCompetition(String competitionId) {
        for (ExternalMatch external : matchDataProvider.getMatchesByCompetition(competitionId)) {
            Optional<Match> existing = matchRepository.findByExternalId(external.externalId());
            MatchStatus newStatus = toStatus(external.status());

            Match match = existing
                    .map(m -> m.withUpdate(external.homeScore(), external.awayScore(), newStatus))
                    .orElseGet(() -> Match.fromExternal(
                            external.externalId(),
                            external.homeTeamId(),
                            external.awayTeamId(),
                            external.homeScore(),
                            external.awayScore(),
                            newStatus
                    ));

            matchRepository.save(match);
            // Events are only emitted on status transitions for existing matches.
            // A match discovered for the first time is silently persisted regardless of status,
            // to avoid duplicate events if the service restarts mid-match.
            emitTransitionEvent(existing, newStatus, match);
            matchesSyncedCounter.increment();
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
}
