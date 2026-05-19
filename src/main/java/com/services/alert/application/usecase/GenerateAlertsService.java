package com.services.alert.application.usecase;

import com.services.alert.application.dto.ExpiringDocumentInfo;
import com.services.alert.application.port.in.GenerateAlertsUseCase;
import com.services.alert.application.port.out.AlertRepositoryPort;
import com.services.alert.application.port.out.DriverLicenseClientPort;
import com.services.alert.application.port.out.VehicleDocumentClientPort;
import com.services.alert.domain.model.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateAlertsService implements GenerateAlertsUseCase {

    private static final int EXPIRATION_THRESHOLD_DAYS = 30;

    private final AlertRepositoryPort alertRepositoryPort;
    private final VehicleDocumentClientPort vehicleDocumentClientPort;
    private final DriverLicenseClientPort driverLicenseClientPort;

    @Override
    public int generate() {
        log.info("[Alerts] Iniciando generación de alertas (umbral={} días)", EXPIRATION_THRESHOLD_DAYS);

        List<ExpiringDocumentInfo> expiring = new ArrayList<>();

        // Vehicle Service: si está caído, no debe tumbar el job entero
        try {
            expiring.addAll(vehicleDocumentClientPort.getDocumentsAboutToExpire(EXPIRATION_THRESHOLD_DAYS));
        } catch (Exception ex) {
            log.error("[Alerts] Falla al consultar Vehicle Service: {}", ex.getMessage());
        }

        // Driver Service
        try {
            expiring.addAll(driverLicenseClientPort.getLicensesAboutToExpire(EXPIRATION_THRESHOLD_DAYS));
        } catch (Exception ex) {
            log.error("[Alerts] Falla al consultar Driver Service: {}", ex.getMessage());
        }

        int created = 0;
        for (ExpiringDocumentInfo info : expiring) {
            // Anti-duplicado: si ya existe una PENDING para ese entityId+documentId, saltar
            boolean alreadyExists = alertRepositoryPort.findPendingByEntityAndDocument(
                    info.entityType(),
                    info.entityId(),
                    info.documentType(),
                    info.documentId()
            ).isPresent();

            if (alreadyExists) {
                continue;
            }

            Alert alert = new Alert(
                    info.entityType(),
                    info.entityId(),
                    info.documentType(),
                    info.documentId()
            );
            alertRepositoryPort.save(alert);
            created++;
        }

        log.info("[Alerts] Generación finalizada. Nuevas alertas: {}", created);
        return created;
    }
}
