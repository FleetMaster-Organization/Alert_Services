package com.services.alert.infrastructure.adapter.feign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * Vista mínima de un vehículo expuesta por {@code GET /vehicles}.
 * Solo se mapean los campos que el Alert Service necesita.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VehicleSummaryDTO(
        UUID id,
        String plate
) {}
