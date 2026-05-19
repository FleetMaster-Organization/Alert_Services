package com.services.alert.application.port.in;

import com.services.alert.application.dto.AlertResponse;
import com.services.alert.domain.enums.AlertStatus;

import java.util.List;

public interface GetAllAlertsUseCase {
    /**
     * Devuelve todas las alertas. Si {@code status} es {@code null}
     * trae todas; de lo contrario filtra por el estado indicado.
     */
    List<AlertResponse> execute(AlertStatus status);
}
