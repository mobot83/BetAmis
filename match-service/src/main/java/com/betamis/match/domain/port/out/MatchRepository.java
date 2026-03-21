package com.betamis.match.domain.port.out;

import com.betamis.match.domain.model.match.Match;

import java.util.Optional;

public interface MatchRepository {

    void save(Match match);

    Optional<Match> findByExternalId(long externalId);
}
