package com.services.alert.application.usecase;

import com.services.alert.application.dto.DashboardResponse;
import com.services.alert.application.port.in.GetDashboardUseCase;
import com.services.alert.application.port.out.AlertRepositoryPort;
import com.services.alert.domain.enums.AlertCriticality;
import com.services.alert.domain.enums.AlertEntityType;
import com.services.alert.domain.enums.AlertStatus;
import com.services.alert.domain.model.Alert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetDashboardService implements GetDashboardUseCase {

    private final AlertRepositoryPort alertRepositoryPort;

    @Override
    public DashboardResponse execute() {

        long totalPending = alertRepositoryPort.countByStatus(AlertStatus.PENDING);
        long totalManaged = alertRepositoryPort.countByStatus(AlertStatus.MANAGED);
        long vehiclePending = alertRepositoryPort.countByStatusAndEntityType(AlertStatus.PENDING, AlertEntityType.VEHICLE);
        long driverPending = alertRepositoryPort.countByStatusAndEntityType(AlertStatus.PENDING, AlertEntityType.DRIVER);

        // Para WARNING / EXPIRED necesitamos enriquecer con la fecha real;
        // sin enriquecimiento devolvemos totalPending bajo WARNING como fallback,
        // ya que el frontend que renderiza por color hará la cuenta fina con la
        // lista completa de /alerts.
        List<Alert> pending = alertRepositoryPort.findByStatus(AlertStatus.PENDING);
        LocalDate today = LocalDate.now();

        long expired = pending.stream()
                .filter(a -> a.calculateCriticality(today) == AlertCriticality.EXPIRED)
                .count();
        long warning = pending.size() - expired;

        return new DashboardResponse(
                totalPending,
                expired,
                warning,
                totalManaged,
                vehiclePending,
                driverPending
        );
    }
}
