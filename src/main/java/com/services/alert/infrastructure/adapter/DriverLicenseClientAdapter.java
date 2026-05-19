package com.services.alert.infrastructure.adapter;

import com.services.alert.application.dto.ExpiringDocumentInfo;
import com.services.alert.application.port.out.DriverLicenseClientPort;
import com.services.alert.domain.enums.AlertDocumentType;
import com.services.alert.domain.enums.AlertEntityType;
import com.services.alert.infrastructure.adapter.feign.DriverFeignClient;
import com.services.alert.infrastructure.adapter.feign.dto.DriverSummaryDTO;
import com.services.alert.infrastructure.adapter.feign.dto.LicenseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementación del puerto que consulta licencias de conductores vía Feign.
 *
 * Hace fan-out: listar conductores → consultar las licencias de cada uno.
 * Si Driver Service expone un endpoint global de licencias por vencer en el
 * futuro, basta con reemplazar el bucle por una sola llamada.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DriverLicenseClientAdapter implements DriverLicenseClientPort {

    private final DriverFeignClient driverFeignClient;

    @Override
    public List<ExpiringDocumentInfo> getLicensesAboutToExpire(int days) {
        List<DriverSummaryDTO> drivers;
        try {
            drivers = driverFeignClient.getAllDrivers();
        } catch (Exception ex) {
            log.error("[Feign:Driver] No se pudo obtener listado de conductores: {}", ex.getMessage());
            return List.of();
        }

        List<ExpiringDocumentInfo> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (DriverSummaryDTO driver : drivers) {
            try {
                List<LicenseDTO> licenses = driverFeignClient.getLicensesByDriver(driver.idDriver());
                licenses.stream()
                        .filter(l -> isExpiringOrExpired(l.expirationDate(), days, today))
                        .map(l -> toExpiringLicense(driver.idDriver(), l))
                        .forEach(result::add);
            } catch (Exception ex) {
                log.warn("[Feign:Driver] Falla consultando licencias de {}: {}",
                        driver.idDriver(), ex.getMessage());
            }
        }

        return result;
    }

    @Override
    public List<ExpiringDocumentInfo> getLicensesAboutToExpireByDriver(UUID driverId, int days) {
        try {
            LocalDate today = LocalDate.now();
            return driverFeignClient.getLicensesByDriver(driverId).stream()
                    .filter(l -> isExpiringOrExpired(l.expirationDate(), days, today))
                    .map(l -> toExpiringLicense(driverId, l))
                    .toList();
        } catch (Exception ex) {
            log.warn("[Feign:Driver] Falla consultando licencias del conductor {}: {}",
                    driverId, ex.getMessage());
            return List.of();
        }
    }

    private boolean isExpiringOrExpired(LocalDate expirationDate, int days, LocalDate today) {
        if (expirationDate == null) return false;
        long until = ChronoUnit.DAYS.between(today, expirationDate);
        return until <= days;
    }

    private ExpiringDocumentInfo toExpiringLicense(UUID driverId, LicenseDTO dto) {
        return new ExpiringDocumentInfo(
                AlertEntityType.DRIVER,
                driverId,
                AlertDocumentType.LICENSE,
                dto.idLicense().toString(),
                dto.expirationDate()
        );
    }
}
