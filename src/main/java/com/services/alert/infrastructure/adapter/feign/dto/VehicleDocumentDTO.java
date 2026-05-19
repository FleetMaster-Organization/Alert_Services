package com.services.alert.infrastructure.adapter.feign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Documento vehicular tal como lo expone Vehicle Service en
 * {@code GET /vehicles/{id}/documentsAboutToExpire} y
 * {@code GET /vehicles/{id}/documents}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VehicleDocumentDTO(
        UUID id,
        UUID vehicleId,
        String documentType,
        String documentNumber,
        String issuedBy,
        LocalDate issueDate,
        LocalDate expirationDate,
        String legalStatus
) {}
