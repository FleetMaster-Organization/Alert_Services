package com.services.alert.application.dto;

/**
 * KPIs principales del dashboard de alertas (REQ-36, RNF-01).
 */
public record DashboardResponse(
        long totalPending,
        long totalExpired,
        long totalWarning,
        long totalManaged,
        long vehiclePending,
        long driverPending
) {}
