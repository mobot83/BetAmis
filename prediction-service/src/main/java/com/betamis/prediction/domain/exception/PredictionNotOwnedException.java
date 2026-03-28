package com.betamis.prediction.domain.exception;

public class PredictionNotOwnedException extends RuntimeException {
    public PredictionNotOwnedException(String message) {
        super(message);
    }
}
