package com.betamis.notification.infrastructure.persistence;

import com.betamis.notification.domain.model.NotificationPreference;
import com.betamis.notification.domain.port.out.NotificationPreferenceRepository;
import com.betamis.notification.infrastructure.persistence.entity.NotificationPreferenceEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class NotificationPreferenceRepositoryAdapter implements NotificationPreferenceRepository {

    @Override
    @Transactional
    public void save(NotificationPreference pref) {
        NotificationPreferenceEntity entity = NotificationPreferenceEntity
                .<NotificationPreferenceEntity>find("userId", pref.getUserId())
                .firstResultOptional()
                .orElse(new NotificationPreferenceEntity());

        entity.userId = pref.getUserId();
        entity.email = pref.getEmail();
        entity.emailEnabled = pref.isEmailEnabled();
        entity.webPushEnabled = pref.isWebPushEnabled();
        entity.unsubscribeToken = pref.getUnsubscribeToken();
        entity.pushSubscriptionJson = pref.getPushSubscriptionJson();

        entity.persist();
    }

    @Override
    public Optional<NotificationPreference> findByUserId(String userId) {
        return NotificationPreferenceEntity.<NotificationPreferenceEntity>find("userId", userId)
                .firstResultOptional()
                .map(NotificationPreferenceRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<NotificationPreference> findByUnsubscribeToken(String token) {
        return NotificationPreferenceEntity.<NotificationPreferenceEntity>find("unsubscribeToken", token)
                .firstResultOptional()
                .map(NotificationPreferenceRepositoryAdapter::toDomain);
    }

    @Override
    public List<NotificationPreference> findAllEmailEnabled() {
        return NotificationPreferenceEntity.<NotificationPreferenceEntity>find("emailEnabled", true)
                .stream()
                .map(NotificationPreferenceRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public List<NotificationPreference> findAllWebPushEnabled() {
        return NotificationPreferenceEntity.<NotificationPreferenceEntity>find("webPushEnabled", true)
                .stream()
                .map(NotificationPreferenceRepositoryAdapter::toDomain)
                .toList();
    }

    private static NotificationPreference toDomain(NotificationPreferenceEntity e) {
        return new NotificationPreference(
                e.userId, e.email, e.emailEnabled, e.webPushEnabled,
                e.unsubscribeToken, e.pushSubscriptionJson);
    }
}
