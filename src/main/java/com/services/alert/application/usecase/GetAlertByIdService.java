package com.services.alert.application.usecase;

import com.services.alert.application.dto.AlertResponse;
import com.services.alert.application.mapper.AlertResponseMapper;
import com.services.alert.application.port.in.GetAlertByIdUseCase;
import com.services.alert.application.port.out.AlertRepositoryPort;
import com.services.alert.domain.model.Alert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAlertByIdService implements GetAlertByIdUseCase {

    private final AlertRepositoryPort alertRepositoryPort;
    private final AlertResponseMapper alertResponseMapper;

    @Override
    public AlertResponse execute(UUID id) {
        Alert alert = alertRepositoryPort.findById(id);
        return alertResponseMapper.toResponse(alert);
    }
}
