package com.services.alert.infrastructure.adapter.feign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Licencia tal como la expone Driver Service en
 * {@code GET /drivers/{driverId}/licenses}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LicenseDTO(
        UUID idLicense,
        String category,
        LocalDate issueDate,
        LocalDate expirationDate,
        String licenseStatus,
        Long daysUntilExpiration
) {}
