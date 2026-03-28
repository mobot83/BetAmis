package com.betamis.notification.interfaces.rest.dto;

import com.betamis.notification.domain.model.NotificationPreference;

public record PreferenceResponse(boolean emailEnabled, boolean webPushEnabled) {
    public static PreferenceResponse from(NotificationPreference pref) {
        return new PreferenceResponse(pref.isEmailEnabled(), pref.isWebPushEnabled());
    }
}
