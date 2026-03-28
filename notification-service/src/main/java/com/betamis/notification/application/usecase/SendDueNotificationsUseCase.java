package com.betamis.notification.application.usecase;

import com.betamis.notification.domain.model.NotificationPreference;
import com.betamis.notification.domain.model.ScheduledNotification;
import com.betamis.notification.domain.port.in.SendDueNotifications;
import com.betamis.notification.domain.port.out.EmailSender;
import com.betamis.notification.domain.port.out.NotificationPreferenceRepository;
import com.betamis.notification.domain.port.out.ScheduledNotificationRepository;
import com.betamis.notification.domain.port.out.WebPushSender;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class SendDueNotificationsUseCase implements SendDueNotifications {

    private final ScheduledNotificationRepository scheduledRepo;
    private final NotificationPreferenceRepository preferenceRepo;
    private final EmailSender emailSender;
    private final WebPushSender webPushSender;

    public SendDueNotificationsUseCase(ScheduledNotificationRepository scheduledRepo,
                                       NotificationPreferenceRepository preferenceRepo,
                                       EmailSender emailSender,
                                       WebPushSender webPushSender) {
        this.scheduledRepo = scheduledRepo;
        this.preferenceRepo = preferenceRepo;
        this.emailSender = emailSender;
        this.webPushSender = webPushSender;
    }

    @Override
    @Transactional
    public void sendDue() {
        List<ScheduledNotification> due = scheduledRepo.findDue(Instant.now());
        if (due.isEmpty()) return;

        List<NotificationPreference> emailRecipients = preferenceRepo.findAllEmailEnabled();
        List<NotificationPreference> pushRecipients = preferenceRepo.findAllWebPushEnabled();

        for (ScheduledNotification notification : due) {
            sendToAllUsers(notification, emailRecipients, pushRecipients);
            scheduledRepo.markSent(notification.getId());
        }
    }

    private void sendToAllUsers(ScheduledNotification notification,
                                List<NotificationPreference> emailRecipients,
                                List<NotificationPreference> pushRecipients) {
        for (NotificationPreference pref : emailRecipients) {
            if (pref.getEmail() == null) continue;
            try {
                emailSender.sendMatchReminder(pref.getEmail(), pref.getUnsubscribeToken(), notification);
            } catch (Exception e) {
                Log.errorf(e, "Failed to send email to %s for match %s", pref.getEmail(), notification.getMatchId());
            }
        }

        for (NotificationPreference pref : pushRecipients) {
            if (pref.getPushSubscriptionJson() == null) continue;
            try {
                webPushSender.sendMatchReminder(pref.getPushSubscriptionJson(), notification);
            } catch (Exception e) {
                Log.errorf(e, "Failed to send web push to user %s for match %s", pref.getUserId(), notification.getMatchId());
            }
        }
    }
}
