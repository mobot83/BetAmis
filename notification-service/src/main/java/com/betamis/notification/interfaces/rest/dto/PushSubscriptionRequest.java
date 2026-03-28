package com.betamis.notification.interfaces.rest.dto;

/**
 * Browser-provided Web Push subscription object.
 * Contains the endpoint URL and VAPID encryption keys.
 */
public record PushSubscriptionRequest(String endpoint, Keys keys) {
    public record Keys(String p256dh, String auth) {}
}
