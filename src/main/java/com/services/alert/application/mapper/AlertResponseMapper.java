package com.services.alert.application.mapper;

import com.services.alert.application.dto.AlertResponse;
import com.services.alert.domain.model.Alert;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AlertResponseMapper {

    public AlertResponse toResponse(Alert alert) {
        if (alert == null) return null;

        LocalDate today = LocalDate.now();

        return new AlertResponse(
                alert.getId(),
                alert.getEntityType() != null ? alert.getEntityType().name() : null,
                alert.getEntityId(),
                alert.getDocumentType() != null ? alert.getDocumentType().name() : null,
                alert.getDocumentId(),
                alert.getStatus() != null ? alert.getStatus().name() : null,
                alert.calculateCriticality(today).name(),
                alert.daysUntilExpiration(today),
                alert.getExpirationDate(),
                alert.getResolvedAt(),
                alert.getResolvedBy(),
                alert.getCreatedAt(),
                alert.getUpdatedAt()
        );
    }
}
