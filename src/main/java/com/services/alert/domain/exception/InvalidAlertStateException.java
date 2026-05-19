package com.services.alert.domain.exception;

public class InvalidAlertStateException extends RuntimeException {
    public InvalidAlertStateException(String message) {
        super(message);
    }
}
