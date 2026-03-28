package com.betamis.notification.domain.model;

public class NotificationPreference {

    private final String userId;
    private final String email;
    private final boolean emailEnabled;
    private final boolean webPushEnabled;
    private final String unsubscribeToken;
    private final String pushSubscriptionJson;

    public NotificationPreference(String userId, String email, boolean emailEnabled,
                                  boolean webPushEnabled, String unsubscribeToken,
                                  String pushSubscriptionJson) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId cannot be blank");
        this.userId = userId;
        this.email = email;
        this.emailEnabled = emailEnabled;
        this.webPushEnabled = webPushEnabled;
        this.unsubscribeToken = unsubscribeToken;
        this.pushSubscriptionJson = pushSubscriptionJson;
    }

    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public boolean isEmailEnabled() { return emailEnabled; }
    public boolean isWebPushEnabled() { return webPushEnabled; }
    public String getUnsubscribeToken() { return unsubscribeToken; }
    public String getPushSubscriptionJson() { return pushSubscriptionJson; }

    public NotificationPreference withEmailEnabled(boolean emailEnabled) {
        return new NotificationPreference(userId, email, emailEnabled, webPushEnabled, unsubscribeToken, pushSubscriptionJson);
    }

    public NotificationPreference withWebPushEnabled(boolean webPushEnabled) {
        return new NotificationPreference(userId, email, emailEnabled, webPushEnabled, unsubscribeToken, pushSubscriptionJson);
    }

    public NotificationPreference withPushSubscription(String subscriptionJson) {
        return new NotificationPreference(userId, email, emailEnabled, true, unsubscribeToken, subscriptionJson);
    }
}
