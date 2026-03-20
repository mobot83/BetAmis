package com.betamis.league.domain.port.in;

import com.betamis.league.domain.model.League;

public interface CreateLeague {
    League create(String name, String ownerId);
}

