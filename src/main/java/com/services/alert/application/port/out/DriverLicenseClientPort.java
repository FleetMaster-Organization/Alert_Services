package com.services.alert.application.port.out;

import com.services.alert.application.dto.ExpiringDocumentInfo;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida hacia el Driver Service.
 * La implementación concreta vive en infrastructure/adapter y usa Feign.
 */
public interface DriverLicenseClientPort {

    /**
     * Devuelve todas las licencias de conductores que están a {@code days}
     * o menos de vencer (incluyendo las ya vencidas), homogeneizadas al
     * formato canónico {@link ExpiringDocumentInfo}.
     */
    List<ExpiringDocumentInfo> getLicensesAboutToExpire(int days);

    /**
     * Licencias próximas a vencer para un único conductor.
     * Usado por el endpoint de recálculo on-demand (REQ-37).
     */
    List<ExpiringDocumentInfo> getLicensesAboutToExpireByDriver(UUID driverId, int days);
}
