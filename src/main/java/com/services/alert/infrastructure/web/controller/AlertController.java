package com.services.alert.infrastructure.web.controller;

import com.services.alert.application.dto.AlertResponse;
import com.services.alert.application.dto.DashboardResponse;
import com.services.alert.application.port.in.*;
import com.services.alert.domain.enums.AlertEntityType;
import com.services.alert.domain.enums.AlertStatus;
import com.services.alert.infrastructure.web.dto.GenerateAlertsResponseDTO;
import com.services.alert.infrastructure.web.dto.MarkAsManagedRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints REST del Alert Service.
 *
 * Roles esperados (estándar emitido por Auth Service en el JWT):
 *   ROLE_ADMINISTRADOR → todas las operaciones (lectura + gestión)
 *   ROLE_COORDINADOR   → solo lectura
 *
 * Usamos hasAuthority() (no hasRole()) porque el string en el JWT
 * ya viene con el prefijo "ROLE_". hasRole() añadiría otro "ROLE_"
 * y nunca matchearía.
 */
@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private static final String DEFAULT_USER = "SYSTEM";

    private final GetAllAlertsUseCase getAllAlertsUseCase;
    private final GetAlertByIdUseCase getAlertByIdUseCase;
    private final GetDashboardUseCase getDashboardUseCase;
    private final MarkAlertAsManagedUseCase markAlertAsManagedUseCase;
    private final GenerateAlertsUseCase generateAlertsUseCase;
    private final RevertExpiredAlertsUseCase revertExpiredAlertsUseCase;
    private final RecalculateAlertsForEntityUseCase recalculateAlertsForEntityUseCase;

    // ── Listado paginado simple (filtra por status) ─────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_COORDINADOR', 'ROLE_DESPACHADOR', 'ROLE_MECANICO')")
    public ResponseEntity<List<AlertResponse>> getAlerts(
            @RequestParam(required = false) AlertStatus status) {

        return ResponseEntity.ok(getAllAlertsUseCase.execute(status));
    }

    // ── Detalle de alerta ───────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_COORDINADOR', 'ROLE_DESPACHADOR', 'ROLE_MECANICO')")
    public ResponseEntity<AlertResponse> getAlertById(@PathVariable UUID id) {
        return ResponseEntity.ok(getAlertByIdUseCase.execute(id));
    }

    // ── Dashboard / KPIs ────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_COORDINADOR')")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(getDashboardUseCase.execute());
    }

    // ── Marcar como gestionada (REQ-38) — Solo Administrador ────────────────

    @PatchMapping("/{id}/manage")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<Void> markAsManaged(
            @PathVariable UUID id,
            @RequestBody(required = false) MarkAsManagedRequestDTO body) {

        String resolvedBy = resolveUser(body);
        markAlertAsManagedUseCase.manage(id, resolvedBy);
        return ResponseEntity.noContent().build();
    }

    // ── Disparar generación manualmente (debug / cuando el cron falló) ──────

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<GenerateAlertsResponseDTO> generate() {
        int created = generateAlertsUseCase.generate();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenerateAlertsResponseDTO(created, "Generación ejecutada"));
    }

    // ── Disparar reversión manualmente (REQ-39) ─────────────────────────────

    @PostMapping("/revert")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<GenerateAlertsResponseDTO> revert() {
        int reverted = revertExpiredAlertsUseCase.revert();
        return ResponseEntity.ok(new GenerateAlertsResponseDTO(reverted, "Reversión ejecutada"));
    }

    // ── Recalcular para una entidad puntual (REQ-37) ────────────────────────

    @PostMapping("/recalculate")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<GenerateAlertsResponseDTO> recalculate(
            @RequestParam AlertEntityType entityType,
            @RequestParam UUID entityId) {

        int created = recalculateAlertsForEntityUseCase.recalculate(entityType, entityId);
        return ResponseEntity.ok(new GenerateAlertsResponseDTO(created, "Recálculo ejecutado"));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private String resolveUser(MarkAsManagedRequestDTO body) {
        if (body != null && body.resolvedBy() != null && !body.resolvedBy().isBlank()) {
            return body.resolvedBy();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : DEFAULT_USER;
    }
}
