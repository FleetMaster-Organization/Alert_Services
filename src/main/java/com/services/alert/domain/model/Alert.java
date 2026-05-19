package com.services.alert.domain.model;

import com.services.alert.domain.enums.AlertCriticality;
import com.services.alert.domain.enums.AlertDocumentType;
import com.services.alert.domain.enums.AlertEntityType;
import com.services.alert.domain.enums.AlertStatus;
import com.services.alert.domain.exception.InvalidAlertStateException;
import com.services.alert.domain.exception.InvalidDomainDataException;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Modelo de dominio puro que representa una alerta de vencimiento.
 *
 * Una alerta es generada por el sistema cuando un documento (SOAT,
 * Tecnomecánica o Licencia) está próximo a vencer (≤30 días) o ya vencido.
 *
 * No tiene anotaciones de persistencia; esa responsabilidad recae
 * exclusivamente en la capa de infraestructura.
 *
 * El campo {@code expirationDate} NO se persiste: se enriquece en memoria
 * al consultar al microservicio dueño del documento, y se usa solo para
 * calcular {@link AlertCriticality} y {@code daysUntilExpiration}.
 */
@Getter
@NoArgsConstructor
public class Alert {

    private UUID id;
    private AlertEntityType entityType;
    private UUID entityId;
    private AlertDocumentType documentType;
    private String documentId;
    private AlertStatus status;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Datos enriquecidos en runtime (no persistidos)
    private LocalDate expirationDate;

    // ── Constructor de negocio ────────────────────────────────────────────────

    public Alert(AlertEntityType entityType,
                 UUID entityId,
                 AlertDocumentType documentType,
                 String documentId) {

        if (entityType == null) {
            throw new InvalidDomainDataException("El tipo de entidad es obligatorio");
        }
        if (entityId == null) {
            throw new InvalidDomainDataException("El id de la entidad es obligatorio");
        }
        if (documentType == null) {
            throw new InvalidDomainDataException("El tipo de documento es obligatorio");
        }
        if (documentId == null || documentId.isBlank()) {
            throw new InvalidDomainDataException("El id del documento es obligatorio");
        }

        this.entityType = entityType;
        this.entityId = entityId;
        this.documentType = documentType;
        this.documentId = documentId;
        this.status = AlertStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Alert rehydrate(UUID id,
                                  AlertEntityType entityType,
                                  UUID entityId,
                                  AlertDocumentType documentType,
                                  String documentId,
                                  AlertStatus status,
                                  LocalDateTime resolvedAt,
                                  String resolvedBy,
                                  LocalDateTime createdAt,
                                  LocalDateTime updatedAt) {

        Alert alert = new Alert();
        alert.id = id;
        alert.entityType = entityType;
        alert.entityId = entityId;
        alert.documentType = documentType;
        alert.documentId = documentId;
        alert.status = status;
        alert.resolvedAt = resolvedAt;
        alert.resolvedBy = resolvedBy;
        alert.createdAt = createdAt;
        alert.updatedAt = updatedAt;

        return alert;
    }

    // ── Lógica de negocio ─────────────────────────────────────────────────────

    /**
     * Marca la alerta como gestionada por un Administrador (REQ-38).
     * No elimina el registro; solo cambia el estado para ocultarla
     * del tablero principal.
     */
    public void markAsManaged(String managedBy) {
        if (this.status == AlertStatus.MANAGED) {
            throw new InvalidAlertStateException("La alerta ya está marcada como gestionada");
        }
        if (managedBy == null || managedBy.isBlank()) {
            throw new InvalidDomainDataException("Se requiere el usuario que gestiona la alerta");
        }

        this.status = AlertStatus.MANAGED;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = managedBy;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Revierte una alerta MANAGED de vuelta a PENDING (REQ-39).
     * Aplica cuando el cron detecta que el documento sigue vencido
     * después de haber sido marcado como gestionado.
     */
    public void revertToPending() {
        if (this.status != AlertStatus.MANAGED) {
            throw new InvalidAlertStateException("Solo se puede revertir una alerta gestionada");
        }

        this.status = AlertStatus.PENDING;
        this.resolvedAt = null;
        this.resolvedBy = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Enriquece la alerta con la fecha real de vencimiento del documento
     * referenciado, obtenida del microservicio dueño (Vehicle/Driver).
     * Solo se usa para calcular criticidad y días restantes en respuestas.
     */
    public void enrichWithExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Calcula la criticidad visual de la alerta según los días restantes
     * para el vencimiento (REQ-36).
     *
     * Si no se ha enriquecido con la fecha de vencimiento, se asume WARNING
     * por defecto (la alerta existe, por lo tanto al momento de crearla
     * estaba dentro de los 30 días).
     */
    public AlertCriticality calculateCriticality(LocalDate today) {
        if (expirationDate == null) {
            return AlertCriticality.WARNING;
        }
        return expirationDate.isBefore(today) ? AlertCriticality.EXPIRED : AlertCriticality.WARNING;
    }

    /**
     * Días hasta el vencimiento del documento referenciado.
     * Negativo si ya venció. {@code null} si la alerta no fue enriquecida.
     */
    public Long daysUntilExpiration(LocalDate today) {
        if (expirationDate == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(today, expirationDate);
    }

    public boolean isPending() {
        return this.status == AlertStatus.PENDING;
    }

    public boolean isManaged() {
        return this.status == AlertStatus.MANAGED;
    }
}
