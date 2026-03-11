package com.betamis.prediction.domain.exception;

public class PredictionAlreadySubmittedException extends RuntimeException {
    public PredictionAlreadySubmittedException(String message) {
        super(message);
    }
}
