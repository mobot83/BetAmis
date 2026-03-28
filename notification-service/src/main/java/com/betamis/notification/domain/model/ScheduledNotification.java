package com.betamis.notification.domain.model;

import java.time.Instant;

public class ScheduledNotification {

    private final Long id;
    private final String matchId;
    private final String homeTeamId;
    private final String awayTeamId;
    private final Instant kickoffAt;
    private final Instant notifyAt;
    private final boolean sent;

    public ScheduledNotification(Long id, String matchId, String homeTeamId, String awayTeamId,
                                 Instant kickoffAt, Instant notifyAt, boolean sent) {
        this.id = id;
        this.matchId = matchId;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.kickoffAt = kickoffAt;
        this.notifyAt = notifyAt;
        this.sent = sent;
    }

    public Long getId() { return id; }
    public String getMatchId() { return matchId; }
    public String getHomeTeamId() { return homeTeamId; }
    public String getAwayTeamId() { return awayTeamId; }
    public Instant getKickoffAt() { return kickoffAt; }
    public Instant getNotifyAt() { return notifyAt; }
    public boolean isSent() { return sent; }
}
