package com.services.alert.application.port.out;

import com.services.alert.domain.enums.AlertDocumentType;
import com.services.alert.domain.enums.AlertEntityType;
import com.services.alert.domain.enums.AlertStatus;
import com.services.alert.domain.model.Alert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRepositoryPort {

    Alert save(Alert alert);

    Alert findById(UUID id);

    List<Alert> findAll();

    List<Alert> findByStatus(AlertStatus status);

    List<Alert> findByEntity(AlertEntityType entityType, UUID entityId);

    Optional<Alert> findPendingByEntityAndDocument(AlertEntityType entityType,
                                                   UUID entityId,
                                                   AlertDocumentType documentType,
                                                   String documentId);

    long countByStatus(AlertStatus status);

    long countByStatusAndEntityType(AlertStatus status, AlertEntityType entityType);
}
