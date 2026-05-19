package com.services.alert.infrastructure.adapter;

import com.services.alert.application.dto.ExpiringDocumentInfo;
import com.services.alert.application.port.out.VehicleDocumentClientPort;
import com.services.alert.domain.enums.AlertDocumentType;
import com.services.alert.domain.enums.AlertEntityType;
import com.services.alert.infrastructure.adapter.feign.VehicleFeignClient;
import com.services.alert.infrastructure.adapter.feign.dto.VehicleDocumentDTO;
import com.services.alert.infrastructure.adapter.feign.dto.VehicleSummaryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementación del puerto que consulta documentos vehiculares vía Feign.
 *
 * Como Vehicle Service no expone (aún) un endpoint global de "documentos por
 * vencer", se hace fan-out: listar vehículos → consultar cada uno.
 * Cuando se agregue {@code GET /vehicles/documents/about-to-expire?days=30}
 * basta con reemplazar el bucle por una sola llamada.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleDocumentClientAdapter implements VehicleDocumentClientPort {

    private final VehicleFeignClient vehicleFeignClient;

    @Override
    public List<ExpiringDocumentInfo> getDocumentsAboutToExpire(int days) {
        List<VehicleSummaryDTO> vehicles;
        try {
            vehicles = vehicleFeignClient.getAllVehicles();
        } catch (Exception ex) {
            log.error("[Feign:Vehicle] No se pudo obtener listado de vehículos: {}", ex.getMessage());
            return List.of();
        }

        List<ExpiringDocumentInfo> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (VehicleSummaryDTO vehicle : vehicles) {
            try {
                List<VehicleDocumentDTO> docs = vehicleFeignClient.getAllDocuments(vehicle.id());
                docs.stream()
                        .filter(d -> isExpiringOrExpired(d.expirationDate(), days, today))
                        .map(d -> toExpiringDocument(vehicle.id(), d))
                        .filter(java.util.Objects::nonNull)
                        .forEach(result::add);
            } catch (Exception ex) {
                log.warn("[Feign:Vehicle] Falla consultando documentos de {}: {}",
                        vehicle.id(), ex.getMessage());
            }
        }

        return result;
    }

    @Override
    public List<ExpiringDocumentInfo> getDocumentsAboutToExpireByVehicle(UUID vehicleId, int days) {
        try {
            LocalDate today = LocalDate.now();
            return vehicleFeignClient.getAllDocuments(vehicleId).stream()
                    .filter(d -> isExpiringOrExpired(d.expirationDate(), days, today))
                    .map(d -> toExpiringDocument(vehicleId, d))
                    .filter(java.util.Objects::nonNull)
                    .toList();
        } catch (Exception ex) {
            log.warn("[Feign:Vehicle] Falla consultando documentos del vehículo {}: {}",
                    vehicleId, ex.getMessage());
            return List.of();
        }
    }

    private boolean isExpiringOrExpired(LocalDate expirationDate, int days, LocalDate today) {
        if (expirationDate == null) return false;
        long until = ChronoUnit.DAYS.between(today, expirationDate);
        return until <= days;
    }

    private ExpiringDocumentInfo toExpiringDocument(UUID vehicleId, VehicleDocumentDTO dto) {
        AlertDocumentType type = mapDocumentType(dto.documentType());
        if (type == null) return null;

        return new ExpiringDocumentInfo(
                AlertEntityType.VEHICLE,
                vehicleId,
                type,
                dto.id().toString(),
                dto.expirationDate()
        );
    }

    /**
     * Vehicle Service maneja {@code SOAT} y {@code TECNO} en su enum DocumentType.
     * Acá normalizamos {@code TECNO} → {@code TECNOMECANICA} para alinear con la
     * convención de la BD de alertas.
     */
    private AlertDocumentType mapDocumentType(String raw) {
        if (raw == null) return null;
        return switch (raw.toUpperCase()) {
            case "SOAT" -> AlertDocumentType.SOAT;
            case "TECNO", "TECNOMECANICA" -> AlertDocumentType.TECNOMECANICA;
            default -> null;
        };
    }
}
