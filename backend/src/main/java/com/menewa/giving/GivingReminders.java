package com.menewa.giving;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Relances d'échéances, chaque jour à 8 h (heure de Lomé) : rappel avant l'échéance (délai réglable, paramètre
 * « requests.late_alert_days ») et relance unique en cas de retard. Donateur et gestionnaires des demandes sont prévenus.
 */
@Component
class GivingReminders {
    private static final Logger log = LoggerFactory.getLogger(GivingReminders.class);
    private final Requests requests;

    GivingReminders(Requests requests) { this.requests = requests; }

    record ReminderResult(int dueSoon, int overdue) {}

    @Scheduled(cron = "${menewa.giving.reminders-cron:0 0 8 * * *}", zone = "Africa/Lome")
    @Transactional
    public ReminderResult run() {
        var result = new ReminderResult(requests.runDueScan(), requests.runOverdueScan());
        if (result.dueSoon() + result.overdue() > 0)
            log.info("Relances d'échéances : {} rappel(s), {} retard(s)", result.dueSoon(), result.overdue());
        return result;
    }
}
