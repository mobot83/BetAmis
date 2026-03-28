package com.betamis.notification.domain.port.out;

import com.betamis.notification.domain.model.ScheduledNotification;

public interface WebPushSender {
    void sendMatchReminder(String subscriptionJson, ScheduledNotification notification);
}
