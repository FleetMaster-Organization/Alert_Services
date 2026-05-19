package com.services.alert.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        String entityType,
        UUID entityId,
        String documentType,
        String documentId,
        String status,
        String criticality,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long daysUntilExpiration,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        LocalDate expirationDate,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        LocalDateTime resolvedAt,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String resolvedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
