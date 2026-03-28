package com.betamis.match.domain.port.in;

import com.betamis.match.domain.model.match.Match;
import com.betamis.match.domain.model.match.MatchStatus;

import java.util.List;
import java.util.Optional;

public interface GetMatches {

    List<Match> getAll(Optional<MatchStatus> status);
}
