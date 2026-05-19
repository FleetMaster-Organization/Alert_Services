package com.services.alert.infrastructure.adapter.feign;

import com.services.alert.infrastructure.adapter.feign.dto.VehicleDocumentDTO;
import com.services.alert.infrastructure.adapter.feign.dto.VehicleSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

/**
 * Cliente Feign hacia Vehicle Service.
 * Resuelve el host vía Eureka usando el nombre lógico {@code vehicle-services}.
 */
@FeignClient(name = "vehicle-services")
public interface VehicleFeignClient {

    @GetMapping("/vehicles")
    List<VehicleSummaryDTO> getAllVehicles();

    @GetMapping("/vehicles/{id}/documents")
    List<VehicleDocumentDTO> getAllDocuments(@PathVariable("id") UUID vehicleId);

    @GetMapping("/vehicles/{id}/documentsAboutToExpire")
    List<VehicleDocumentDTO> getDocumentsAboutToExpire(@PathVariable("id") UUID vehicleId);
}
