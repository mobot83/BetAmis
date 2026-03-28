package com.betamis.prediction.domain.exception;

public class MatchAlreadyStartedException extends RuntimeException {
    public MatchAlreadyStartedException(String message) {
        super(message);
    }
}
