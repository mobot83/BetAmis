package com.betamis.notification.domain.port.out;

import com.betamis.notification.domain.model.ScheduledNotification;

public interface EmailSender {
    void sendMatchReminder(String toEmail, String unsubscribeToken, ScheduledNotification notification);
}
