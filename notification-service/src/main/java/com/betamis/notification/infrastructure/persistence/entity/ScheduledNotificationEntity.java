package com.betamis.notification.infrastructure.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "scheduled_notifications",
        indexes = @Index(name = "idx_scheduled_notifications_notify_at_sent",
                columnList = "notify_at, sent"))
public class ScheduledNotificationEntity extends PanacheEntity {

    @Column(name = "match_id", nullable = false)
    public String matchId;

    @Column(name = "home_team_id", nullable = false)
    public String homeTeamId;

    @Column(name = "away_team_id", nullable = false)
    public String awayTeamId;

    @Column(name = "kickoff_at", nullable = false)
    public Instant kickoffAt;

    @Column(name = "notify_at", nullable = false)
    public Instant notifyAt;

    @Column(name = "sent", nullable = false)
    public boolean sent = false;

    @Column(name = "sent_at")
    public Instant sentAt;
}
