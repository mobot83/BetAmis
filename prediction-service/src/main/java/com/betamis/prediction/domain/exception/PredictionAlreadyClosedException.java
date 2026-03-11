package com.betamis.prediction.domain.exception;

public class PredictionAlreadyClosedException extends RuntimeException {
    public PredictionAlreadyClosedException(String message) {
        super(message);
    }
}
