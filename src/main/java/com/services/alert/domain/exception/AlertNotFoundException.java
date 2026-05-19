package com.services.alert.domain.exception;

import java.util.UUID;

public class AlertNotFoundException extends RuntimeException {
    public AlertNotFoundException(UUID id) {
        super("No se encontró la alerta con id: " + id);
    }
}
