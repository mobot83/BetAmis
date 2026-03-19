package com.betamis.league.domain.exception;

public class AlreadyMemberException extends RuntimeException {
    public AlreadyMemberException(String userId, String leagueId) {
        super("User " + userId + " is already a member of league " + leagueId);
    }
}

