package com.services.alert.infrastructure.adapter.feign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * Vista resumida de un conductor expuesta por {@code GET /drivers}.
 * Driver Service usa {@code idDriver} en sus respuestas.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DriverSummaryDTO(
        UUID idDriver
) {}
