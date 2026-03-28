package com.betamis.notification.domain.port.out;

import com.betamis.notification.domain.model.NotificationPreference;

import java.util.List;
import java.util.Optional;

public interface NotificationPreferenceRepository {
    void save(NotificationPreference preference);
    Optional<NotificationPreference> findByUserId(String userId);
    Optional<NotificationPreference> findByUnsubscribeToken(String token);
    List<NotificationPreference> findAllEmailEnabled();
    List<NotificationPreference> findAllWebPushEnabled();
}
