package com.betamis.match.domain.port.out;

import com.betamis.match.domain.model.match.Match;
import com.betamis.match.domain.model.match.MatchStatus;

import java.util.List;
import java.util.Optional;

public interface MatchRepository {

    void save(Match match);

    Optional<Match> findByExternalId(long externalId);

    List<Match> findAll(Optional<MatchStatus> status);
}
