package com.betamis.league.domain.port.in;

import com.betamis.league.domain.model.Membership;

import java.util.List;

public interface ListMembers {
    List<Membership> list(String leagueId);
}

