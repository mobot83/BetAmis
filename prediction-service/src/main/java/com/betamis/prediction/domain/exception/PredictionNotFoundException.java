package com.betamis.prediction.domain.exception;

public class PredictionNotFoundException extends RuntimeException {
    public PredictionNotFoundException(String message) {
        super(message);
    }
}
