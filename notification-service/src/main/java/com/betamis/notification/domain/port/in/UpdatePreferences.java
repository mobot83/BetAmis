package com.betamis.notification.domain.port.in;

import com.betamis.notification.domain.model.NotificationPreference;

public interface UpdatePreferences {
    NotificationPreference getOrCreate(String userId, String email);
    NotificationPreference update(String userId, boolean emailEnabled, boolean webPushEnabled);
    void unsubscribeByToken(String token);
    void savePushSubscription(String userId, String subscriptionJson);
}
