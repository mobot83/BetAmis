package com.betamis.prediction.domain.exception;

public class KickOffAlreadyPassedException extends RuntimeException {
    public KickOffAlreadyPassedException(String message) {
        super(message);
    }
}
