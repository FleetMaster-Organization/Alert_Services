package com.services.alert.infrastructure.persistence.mapper;

import com.services.alert.domain.model.Alert;
import com.services.alert.infrastructure.persistence.entity.AlertEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlertMapper {

    public Alert toDomain(AlertEntity entity) {
        if (entity == null) return null;

        return Alert.rehydrate(
                entity.getId(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getDocumentType(),
                entity.getDocumentId(),
                entity.getStatus(),
                entity.getResolvedAt(),
                entity.getResolvedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public List<Alert> toDomainList(List<AlertEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toDomain).toList();
    }

    public AlertEntity toEntity(Alert domain) {
        if (domain == null) return null;

        return AlertEntity.builder()
                .id(domain.getId())
                .entityType(domain.getEntityType())
                .entityId(domain.getEntityId())
                .documentType(domain.getDocumentType())
                .documentId(domain.getDocumentId())
                .status(domain.getStatus())
                .resolvedAt(domain.getResolvedAt())
                .resolvedBy(domain.getResolvedBy())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
