package com.services.alert.infrastructure.adapter.feign;

import com.services.alert.infrastructure.adapter.feign.dto.DriverSummaryDTO;
import com.services.alert.infrastructure.adapter.feign.dto.LicenseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

/**
 * Cliente Feign hacia Driver Service.
 * Resuelve el host vía Eureka usando el nombre lógico {@code driver-services}.
 */
@FeignClient(name = "driver-services")
public interface DriverFeignClient {

    @GetMapping("/drivers")
    List<DriverSummaryDTO> getAllDrivers();

    @GetMapping("/drivers/{driverId}/licenses")
    List<LicenseDTO> getLicensesByDriver(@PathVariable("driverId") UUID driverId);
}
