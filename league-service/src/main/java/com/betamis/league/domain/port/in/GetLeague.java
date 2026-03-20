package com.betamis.league.domain.port.in;

import com.betamis.league.domain.model.League;

public interface GetLeague {
    League get(String leagueId);
}
