package com.services.alert.infrastructure.web.dto;

public record GenerateAlertsResponseDTO(
        int alertsCreated,
        String message
) {}
