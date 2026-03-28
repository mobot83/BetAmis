package com.betamis.notification.domain.port.out;

import com.betamis.notification.domain.model.ScheduledNotification;

import java.time.Instant;
import java.util.List;

public interface ScheduledNotificationRepository {
    void save(ScheduledNotification notification);
    List<ScheduledNotification> findDue(Instant now);
    void markSent(Long id);
    boolean existsByMatchIdAndNotifyAt(String matchId, Instant notifyAt);
}
