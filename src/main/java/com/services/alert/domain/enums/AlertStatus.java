package com.services.alert.domain.enums;

/**
 * Ciclo de vida de una alerta.
 *
 * PENDING  → alerta vigente que debe verse en el dashboard.
 * MANAGED  → un Administrador la marcó como gestionada (REQ-38).
 *            El cron diario puede revertirla a PENDING si el documento
 *            sigue vencido (REQ-39).
 */
public enum AlertStatus {
    PENDING,
    MANAGED
}
