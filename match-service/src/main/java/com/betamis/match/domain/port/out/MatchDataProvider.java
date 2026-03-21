package com.betamis.match.domain.port.out;

import com.betamis.match.domain.model.match.ExternalMatch;

import java.util.List;

public interface MatchDataProvider {

    List<ExternalMatch> getMatchesByCompetition(String competitionId);
}
