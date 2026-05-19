package com.services.alert.application.usecase;

import com.services.alert.application.dto.ExpiringDocumentInfo;
import com.services.alert.application.port.in.RevertExpiredAlertsUseCase;
import com.services.alert.application.port.out.AlertRepositoryPort;
import com.services.alert.application.port.out.DriverLicenseClientPort;
import com.services.alert.application.port.out.VehicleDocumentClientPort;
import com.services.alert.domain.enums.AlertEntityType;
import com.services.alert.domain.enums.AlertStatus;
import com.services.alert.domain.model.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementación del REQ-39.
 *
 * Trae el listado completo de documentos vencidos / por vencer y lo
 * convierte en un Set indexado por (entityType, entityId, documentId).
 * Luego recorre todas las alertas MANAGED: si su llave aparece en el
 * Set significa que el documento sigue vencido (o sigue ≤30 días),
 * por lo tanto la alerta debe regresar a PENDING.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RevertExpiredAlertsService implements RevertExpiredAlertsUseCase {

    private static final int EXPIRATION_THRESHOLD_DAYS = 30;

    private final AlertRepositoryPort alertRepositoryPort;
    private final VehicleDocumentClientPort vehicleDocumentClientPort;
    private final DriverLicenseClientPort driverLicenseClientPort;

    @Override
    @Transactional
    public int revert() {
        log.info("[Alerts] Iniciando reversión de alertas gestionadas (REQ-39)");

        Set<String> stillExpiringKeys = new HashSet<>();
        LocalDate today = LocalDate.now();

        try {
            List<ExpiringDocumentInfo> vehicleDocs =
                    vehicleDocumentClientPort.getDocumentsAboutToExpire(EXPIRATION_THRESHOLD_DAYS);
            vehicleDocs.stream()
                    .filter(d -> d.expirationDate() == null || !d.expirationDate().isAfter(today))
                    .forEach(d -> stillExpiringKeys.add(key(d.entityType(), d.entityId(), d.documentId())));
        } catch (Exception ex) {
            log.error("[Alerts] Falla al refrescar docs de vehículo en reversión: {}", ex.getMessage());
        }

        try {
            List<ExpiringDocumentInfo> driverDocs =
                    driverLicenseClientPort.getLicensesAboutToExpire(EXPIRATION_THRESHOLD_DAYS);
            driverDocs.stream()
                    .filter(d -> d.expirationDate() == null || !d.expirationDate().isAfter(today))
                    .forEach(d -> stillExpiringKeys.add(key(d.entityType(), d.entityId(), d.documentId())));
        } catch (Exception ex) {
            log.error("[Alerts] Falla al refrescar licencias en reversión: {}", ex.getMessage());
        }

        List<Alert> managed = alertRepositoryPort.findByStatus(AlertStatus.MANAGED);
        int reverted = 0;

        for (Alert alert : managed) {
            String alertKey = key(alert.getEntityType(), alert.getEntityId(), alert.getDocumentId());
            if (stillExpiringKeys.contains(alertKey)) {
                alert.revertToPending();
                alertRepositoryPort.save(alert);
                reverted++;
            }
        }

        log.info("[Alerts] Reversión finalizada. Alertas revertidas: {}", reverted);
        return reverted;
    }

    private static String key(AlertEntityType type, java.util.UUID entityId, String documentId) {
        return type.name() + "|" + entityId + "|" + documentId;
    }
}
