package com.services.alert.application.port.in;

import com.services.alert.domain.enums.AlertEntityType;

import java.util.UUID;

/**
 * Recalcula las alertas asociadas a una entidad puntual (REQ-37).
 * Invocado por Vehicle/Driver Service cuando renuevan o modifican
 * un documento de la entidad.
 */
public interface RecalculateAlertsForEntityUseCase {
    int recalculate(AlertEntityType entityType, UUID entityId);
}
