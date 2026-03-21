package com.betamis.scoring.domain.port.out;

import java.util.List;

public interface LeagueMembershipRepository {
    void save(String userId, String leagueId);
    boolean exists(String userId, String leagueId);
    List<String> findLeagueIdsByUserId(String userId);
}
