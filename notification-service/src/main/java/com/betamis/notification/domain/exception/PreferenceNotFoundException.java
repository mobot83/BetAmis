package com.betamis.notification.domain.exception;

public class PreferenceNotFoundException extends RuntimeException {

    public PreferenceNotFoundException(String message) {
        super(message);
    }
}
