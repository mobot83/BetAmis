package com.betamis.league.domain.exception;

public class InvalidInvitationCodeException extends RuntimeException {
    public InvalidInvitationCodeException(String code) {
        super("Invitation code is invalid or expired: " + code);
    }
}

