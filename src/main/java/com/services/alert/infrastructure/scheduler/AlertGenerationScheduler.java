package com.services.alert.infrastructure.scheduler;

import com.services.alert.application.port.in.GenerateAlertsUseCase;
import com.services.alert.application.port.in.RevertExpiredAlertsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cron interno del Alert Service.
 *
 * Cubre la lógica HU-13/HU-14 + REQ-36/REQ-39:
 *   - Cada día a las 02:00 (zona Bogotá): genera alertas faltantes.
 *   - Cada día a las 02:15: revierte alertas MANAGED cuyo documento siga vencido.
 *
 * Si por algún motivo el servicio está caído a esa hora, basta con disparar
 * los endpoints administrativos {@code POST /alerts/generate} y
 * {@code POST /alerts/revert} para ejecutarlos a demanda.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertGenerationScheduler {

    private final GenerateAlertsUseCase generateAlertsUseCase;
    private final RevertExpiredAlertsUseCase revertExpiredAlertsUseCase;

    @Scheduled(cron = "0 0 2 * * *", zone = "America/Bogota")
    public void runDailyGeneration() {
        log.info("[Scheduler] Disparando generación diaria de alertas");
        try {
            int created = generateAlertsUseCase.generate();
            log.info("[Scheduler] Generación diaria completada. Nuevas alertas: {}", created);
        } catch (Exception ex) {
            log.error("[Scheduler] Falla en generación diaria: {}", ex.getMessage(), ex);
        }
    }

    @Scheduled(cron = "0 15 2 * * *", zone = "America/Bogota")
    public void runDailyReversion() {
        log.info("[Scheduler] Disparando reversión diaria de alertas gestionadas");
        try {
            int reverted = revertExpiredAlertsUseCase.revert();
            log.info("[Scheduler] Reversión diaria completada. Alertas revertidas: {}", reverted);
        } catch (Exception ex) {
            log.error("[Scheduler] Falla en reversión diaria: {}", ex.getMessage(), ex);
        }
    }
}
