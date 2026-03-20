package com.betamis.league.domain.exception;

public class LeagueNotFoundException extends RuntimeException {
    public LeagueNotFoundException(String leagueId) {
        super("League not found: " + leagueId);
    }
}

