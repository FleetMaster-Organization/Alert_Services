package com.services.alert.application.dto;

import com.services.alert.domain.enums.AlertDocumentType;
import com.services.alert.domain.enums.AlertEntityType;

import java.util.UUID;

public record CreateAlertCommand(
        AlertEntityType entityType,
        UUID entityId,
        AlertDocumentType documentType,
        String documentId
) {}
