package com.services.alert.application.usecase;

import com.services.alert.application.dto.AlertResponse;
import com.services.alert.application.mapper.AlertResponseMapper;
import com.services.alert.application.port.in.GetAllAlertsUseCase;
import com.services.alert.application.port.out.AlertRepositoryPort;
import com.services.alert.domain.enums.AlertStatus;
import com.services.alert.domain.model.Alert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllAlertsService implements GetAllAlertsUseCase {

    private final AlertRepositoryPort alertRepositoryPort;
    private final AlertResponseMapper alertResponseMapper;

    @Override
    public List<AlertResponse> execute(AlertStatus status) {
        List<Alert> alerts = (status == null)
                ? alertRepositoryPort.findAll()
                : alertRepositoryPort.findByStatus(status);

        return alerts.stream()
                .map(alertResponseMapper::toResponse)
                .toList();
    }
}
