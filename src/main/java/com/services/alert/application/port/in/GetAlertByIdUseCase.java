package com.services.alert.application.port.in;

import com.services.alert.application.dto.AlertResponse;

import java.util.UUID;

public interface GetAlertByIdUseCase {
    AlertResponse execute(UUID id);
}
