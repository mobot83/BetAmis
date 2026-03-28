package com.betamis.match.infrastructure.persistence;

import com.betamis.match.domain.model.match.Match;
import com.betamis.match.domain.port.out.MatchRepository;
import com.betamis.match.infrastructure.persistence.entity.MatchEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class MatchRepositoryAdapter implements MatchRepository {

    @Override
    public void save(Match match) {
        MatchEntity entity = match.getExternalId()
                .map(extId -> MatchEntity.<MatchEntity>find("externalId", extId).firstResultOptional())
                .orElseGet(() -> MatchEntity.<MatchEntity>find("matchId", match.getId()).firstResultOptional())
                .orElse(new MatchEntity());

        entity.matchId = match.getId();
        entity.externalId = match.getExternalId().orElse(null);
        entity.homeTeamId = match.getHomeTeamId();
        entity.awayTeamId = match.getAwayTeamId();
        entity.homeTeamScore = match.getHomeTeamScore();
        entity.awayTeamScore = match.getAwayTeamScore();
        entity.status = match.getStatus();
        entity.kickoffAt = match.getKickoffAt().orElse(null);

        entity.persist();
    }

    @Override
    public Optional<Match> findByExternalId(long externalId) {
        return MatchEntity.<MatchEntity>find("externalId", externalId)
                .firstResultOptional()
                .map(MatchRepositoryAdapter::toDomain);
    }

    private static Match toDomain(MatchEntity e) {
        return new Match(
                e.matchId,
                e.externalId,
                e.homeTeamId,
                e.awayTeamId,
                e.homeTeamScore,
                e.awayTeamScore,
                e.status,
                e.kickoffAt
        );
    }
}
