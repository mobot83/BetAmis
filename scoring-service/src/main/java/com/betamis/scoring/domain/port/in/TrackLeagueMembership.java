package com.betamis.scoring.domain.port.in;

public interface TrackLeagueMembership {
    void track(String userId, String leagueId);
}
