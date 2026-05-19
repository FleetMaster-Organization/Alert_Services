package com.services.alert.application.port.in;

/**
 * Revisa todas las alertas MANAGED y las devuelve a PENDING si
 * el documento referenciado sigue vencido (REQ-39).
 */
public interface RevertExpiredAlertsUseCase {
    int revert();
}
