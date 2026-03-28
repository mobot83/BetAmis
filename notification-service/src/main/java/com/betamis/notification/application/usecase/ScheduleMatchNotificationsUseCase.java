package com.betamis.notification.application.usecase;

import com.betamis.notification.domain.model.ScheduledNotification;
import com.betamis.notification.domain.port.in.ScheduleMatchNotifications;
import com.betamis.notification.domain.port.out.ScheduledNotificationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class ScheduleMatchNotificationsUseCase implements ScheduleMatchNotifications {

    private final ScheduledNotificationRepository repository;

    public ScheduleMatchNotificationsUseCase(ScheduledNotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void schedule(String matchId, String homeTeamId, String awayTeamId, Instant kickoffAt) {
        Instant oneDayBefore = kickoffAt.minus(1, ChronoUnit.DAYS);
        Instant oneHourBefore = kickoffAt.minus(1, ChronoUnit.HOURS);

        if (!repository.existsByMatchIdAndNotifyAt(matchId, oneDayBefore)) {
            repository.save(new ScheduledNotification(null, matchId, homeTeamId, awayTeamId,
                    kickoffAt, oneDayBefore, false));
        }
        if (!repository.existsByMatchIdAndNotifyAt(matchId, oneHourBefore)) {
            repository.save(new ScheduledNotification(null, matchId, homeTeamId, awayTeamId,
                    kickoffAt, oneHourBefore, false));
        }
    }
}
