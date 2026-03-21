package com.betamis.league.domain.port.out;

import com.betamis.league.domain.model.League;
import com.betamis.league.domain.model.Membership;

import java.util.List;
import java.util.Optional;

public interface LeagueRepository {
    void save(League league);
    Optional<League> findById(String id);
    /**
     * Returns the members of a league without loading the full aggregate.
     * Returns {@code Optional.empty()} when the league does not exist,
     * and {@code Optional.of(emptyList())} when the league exists but has no members.
     */
    Optional<List<Membership>> findMembersByLeagueId(String leagueId);
}

