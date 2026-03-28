package com.betamis.notification.application.usecase;

import com.betamis.notification.domain.exception.PreferenceNotFoundException;
import com.betamis.notification.domain.model.NotificationPreference;
import com.betamis.notification.domain.port.in.UpdatePreferences;
import com.betamis.notification.domain.port.out.NotificationPreferenceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

@ApplicationScoped
public class UpdatePreferencesUseCase implements UpdatePreferences {

    private final NotificationPreferenceRepository repository;

    public UpdatePreferencesUseCase(NotificationPreferenceRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public NotificationPreference getOrCreate(String userId, String email) {
        return repository.findByUserId(userId).orElseGet(() -> {
            NotificationPreference pref = new NotificationPreference(
                    userId, email, true, false, UUID.randomUUID().toString(), null);
            repository.save(pref);
            return pref;
        });
    }

    @Override
    @Transactional
    public NotificationPreference update(String userId, boolean emailEnabled, boolean webPushEnabled) {
        NotificationPreference existing = repository.findByUserId(userId)
                .orElseThrow(() -> new PreferenceNotFoundException("Preferences not found for user " + userId));
        NotificationPreference updated = existing
                .withEmailEnabled(emailEnabled)
                .withWebPushEnabled(webPushEnabled);
        repository.save(updated);
        return updated;
    }

    @Override
    @Transactional
    public void unsubscribeByToken(String token) {
        NotificationPreference existing = repository.findByUnsubscribeToken(token)
                .orElseThrow(() -> new PreferenceNotFoundException("Invalid unsubscribe token"));
        repository.save(existing.withEmailEnabled(false).withWebPushEnabled(false));
    }

    @Override
    @Transactional
    public void savePushSubscription(String userId, String subscriptionJson) {
        NotificationPreference existing = repository.findByUserId(userId)
                .orElseThrow(() -> new PreferenceNotFoundException("Preferences not found for user " + userId));
        repository.save(existing.withPushSubscription(subscriptionJson));
    }
}
