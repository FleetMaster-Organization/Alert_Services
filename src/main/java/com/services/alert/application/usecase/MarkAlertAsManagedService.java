package com.services.alert.application.usecase;

import com.services.alert.application.port.in.MarkAlertAsManagedUseCase;
import com.services.alert.application.port.out.AlertRepositoryPort;
import com.services.alert.domain.model.Alert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarkAlertAsManagedService implements MarkAlertAsManagedUseCase {

    private final AlertRepositoryPort alertRepositoryPort;

    @Override
    @Transactional
    public void manage(UUID alertId, String managedBy) {
        Alert alert = alertRepositoryPort.findById(alertId);
        alert.markAsManaged(managedBy);
        alertRepositoryPort.save(alert);
    }
}
