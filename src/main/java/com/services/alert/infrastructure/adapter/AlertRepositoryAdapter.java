package com.services.alert.infrastructure.adapter;

import com.services.alert.application.port.out.AlertRepositoryPort;
import com.services.alert.domain.enums.AlertDocumentType;
import com.services.alert.domain.enums.AlertEntityType;
import com.services.alert.domain.enums.AlertStatus;
import com.services.alert.domain.exception.AlertNotFoundException;
import com.services.alert.domain.model.Alert;
import com.services.alert.infrastructure.persistence.entity.AlertEntity;
import com.services.alert.infrastructure.persistence.mapper.AlertMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AlertRepositoryAdapter implements AlertRepositoryPort {

    private final JpaAlertRepository jpaAlertRepository;
    private final AlertMapper alertMapper;

    @Override
    public Alert save(Alert alert) {

        AlertEntity entity;

        if (alert.getId() == null) {
            entity = alertMapper.toEntity(alert);
        } else {
            entity = jpaAlertRepository.findById(alert.getId())
                    .orElseThrow(() -> new AlertNotFoundException(alert.getId()));

            entity.setStatus(alert.getStatus());
            entity.setResolvedAt(alert.getResolvedAt());
            entity.setResolvedBy(alert.getResolvedBy());
        }

        AlertEntity saved = jpaAlertRepository.save(entity);
        return alertMapper.toDomain(saved);
    }

    @Override
    public Alert findById(UUID id) {
        AlertEntity entity = jpaAlertRepository.findById(id)
                .orElseThrow(() -> new AlertNotFoundException(id));
        return alertMapper.toDomain(entity);
    }

    @Override
    public List<Alert> findAll() {
        return alertMapper.toDomainList(jpaAlertRepository.findAll());
    }

    @Override
    public List<Alert> findByStatus(AlertStatus status) {
        return alertMapper.toDomainList(jpaAlertRepository.findByStatus(status));
    }

    @Override
    public List<Alert> findByEntity(AlertEntityType entityType, UUID entityId) {
        return alertMapper.toDomainList(
                jpaAlertRepository.findByEntityTypeAndEntityId(entityType, entityId)
        );
    }

    @Override
    public Optional<Alert> findPendingByEntityAndDocument(AlertEntityType entityType,
                                                          UUID entityId,
                                                          AlertDocumentType documentType,
                                                          String documentId) {
        return jpaAlertRepository
                .findByEntityTypeAndEntityIdAndDocumentTypeAndDocumentIdAndStatus(
                        entityType, entityId, documentType, documentId, AlertStatus.PENDING
                )
                .map(alertMapper::toDomain);
    }

    @Override
    public long countByStatus(AlertStatus status) {
        return jpaAlertRepository.countByStatus(status);
    }

    @Override
    public long countByStatusAndEntityType(AlertStatus status, AlertEntityType entityType) {
        return jpaAlertRepository.countByStatusAndEntityType(status, entityType);
    }
}
