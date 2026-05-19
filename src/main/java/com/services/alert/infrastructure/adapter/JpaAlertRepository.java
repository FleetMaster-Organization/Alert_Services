package com.services.alert.infrastructure.adapter;

import com.services.alert.domain.enums.AlertDocumentType;
import com.services.alert.domain.enums.AlertEntityType;
import com.services.alert.domain.enums.AlertStatus;
import com.services.alert.infrastructure.persistence.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaAlertRepository extends JpaRepository<AlertEntity, UUID> {

    List<AlertEntity> findByStatus(AlertStatus status);

    List<AlertEntity> findByEntityTypeAndEntityId(AlertEntityType entityType, UUID entityId);

    Optional<AlertEntity> findByEntityTypeAndEntityIdAndDocumentTypeAndDocumentIdAndStatus(
            AlertEntityType entityType,
            UUID entityId,
            AlertDocumentType documentType,
            String documentId,
            AlertStatus status
    );

    long countByStatus(AlertStatus status);

    long countByStatusAndEntityType(AlertStatus status, AlertEntityType entityType);
}
