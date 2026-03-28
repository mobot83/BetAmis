package com.betamis.notification.infrastructure.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreferenceEntity extends PanacheEntity {

    @Column(name = "user_id", unique = true, nullable = false)
    public String userId;

    @Column(name = "email")
    public String email;

    @Column(name = "email_enabled", nullable = false)
    public boolean emailEnabled = true;

    @Column(name = "web_push_enabled", nullable = false)
    public boolean webPushEnabled = false;

    @Column(name = "unsubscribe_token", unique = true, nullable = false)
    public String unsubscribeToken;

    @Column(name = "push_subscription_json", columnDefinition = "TEXT")
    public String pushSubscriptionJson;
}
