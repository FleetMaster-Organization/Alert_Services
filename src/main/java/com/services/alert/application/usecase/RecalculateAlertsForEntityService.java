package com.services.alert.application.usecase;

import com.services.alert.application.dto.ExpiringDocumentInfo;
import com.services.alert.application.port.in.RecalculateAlertsForEntityUseCase;
import com.services.alert.application.port.out.AlertRepositoryPort;
import com.services.alert.application.port.out.DriverLicenseClientPort;
import com.services.alert.application.port.out.VehicleDocumentClientPort;
import com.services.alert.domain.enums.AlertEntityType;
import com.services.alert.domain.model.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecalculateAlertsForEntityService implements RecalculateAlertsForEntityUseCase {

    private static final int EXPIRATION_THRESHOLD_DAYS = 30;

    private final AlertRepositoryPort alertRepositoryPort;
    private final VehicleDocumentClientPort vehicleDocumentClientPort;
    private final DriverLicenseClientPort driverLicenseClientPort;

    @Override
    @Transactional
    public int recalculate(AlertEntityType entityType, UUID entityId) {
        log.info("[Alerts] Recalculando alertas para {} {}", entityType, entityId);

        List<ExpiringDocumentInfo> expiring = switch (entityType) {
            case VEHICLE -> vehicleDocumentClientPort.getDocumentsAboutToExpireByVehicle(entityId, EXPIRATION_THRESHOLD_DAYS);
            case DRIVER  -> driverLicenseClientPort.getLicensesAboutToExpireByDriver(entityId, EXPIRATION_THRESHOLD_DAYS);
        };

        int created = 0;
        for (ExpiringDocumentInfo info : expiring) {
            boolean alreadyExists = alertRepositoryPort.findPendingByEntityAndDocument(
                    info.entityType(),
                    info.entityId(),
                    info.documentType(),
                    info.documentId()
            ).isPresent();

            if (alreadyExists) continue;

            Alert alert = new Alert(
                    info.entityType(),
                    info.entityId(),
                    info.documentType(),
                    info.documentId()
            );
            alertRepositoryPort.save(alert);
            created++;
        }

        log.info("[Alerts] Recálculo finalizado. Nuevas alertas: {}", created);
        return created;
    }
}
