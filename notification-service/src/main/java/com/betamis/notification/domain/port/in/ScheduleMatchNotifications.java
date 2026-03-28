package com.betamis.notification.domain.port.in;

import java.time.Instant;

public interface ScheduleMatchNotifications {
    void schedule(String matchId, String homeTeamId, String awayTeamId, Instant kickoffAt);
}
