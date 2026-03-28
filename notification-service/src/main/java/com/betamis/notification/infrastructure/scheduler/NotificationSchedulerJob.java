package com.betamis.notification.infrastructure.scheduler;

import com.betamis.notification.domain.port.in.SendDueNotifications;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificationSchedulerJob {

    private final SendDueNotifications sendDueNotifications;

    public NotificationSchedulerJob(SendDueNotifications sendDueNotifications) {
        this.sendDueNotifications = sendDueNotifications;
    }

    @Scheduled(every = "1m")
    void checkAndSend() {
        try {
            sendDueNotifications.sendDue();
        } catch (Exception e) {
            Log.error("Error during scheduled notification dispatch", e);
        }
    }
}
