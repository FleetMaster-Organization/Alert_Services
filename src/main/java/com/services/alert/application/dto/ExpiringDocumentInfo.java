package com.services.alert.application.dto;

import com.services.alert.domain.enums.AlertDocumentType;
import com.services.alert.domain.enums.AlertEntityType;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Información canónica que cada microservicio (Vehicle / Driver) entrega
 * al Alert Service para que este pueda generar o recalcular alertas.
 *
 * Se construye a partir de los DTOs que devuelven los Feign clients,
 * homogeneizando la fuente sin filtrar implementaciones externas hacia
 * el dominio de alertas.
 */
public record ExpiringDocumentInfo(
        AlertEntityType entityType,
        UUID entityId,
        AlertDocumentType documentType,
        String documentId,
        LocalDate expirationDate
) {}
