package com.services.alert.application.port.out;

import com.services.alert.application.dto.ExpiringDocumentInfo;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida hacia el Vehicle Service.
 * La implementación concreta vive en infrastructure/adapter y usa Feign.
 */
public interface VehicleDocumentClientPort {

    /**
     * Devuelve todos los documentos de vehículos que están a {@code days}
     * o menos de vencer (incluyendo los ya vencidos), homogeneizados al
     * formato canónico {@link ExpiringDocumentInfo}.
     */
    List<ExpiringDocumentInfo> getDocumentsAboutToExpire(int days);

    /**
     * Documentos próximos a vencer para un único vehículo.
     * Usado por el endpoint de recálculo on-demand (REQ-37).
     */
    List<ExpiringDocumentInfo> getDocumentsAboutToExpireByVehicle(UUID vehicleId, int days);
}
