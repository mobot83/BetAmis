package com.betamis.league.domain.port.in;

public interface JoinLeague {
    void join(String leagueId, String userId, String invitationCode);
}

