package com.services.alert.domain.exception;

public class InvalidDomainDataException extends RuntimeException {
    public InvalidDomainDataException(String message) {
        super(message);
    }
}
