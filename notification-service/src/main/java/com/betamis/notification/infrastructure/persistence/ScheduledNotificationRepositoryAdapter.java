package com.betamis.notification.infrastructure.persistence;

import com.betamis.notification.domain.model.ScheduledNotification;
import com.betamis.notification.domain.port.out.ScheduledNotificationRepository;
import com.betamis.notification.infrastructure.persistence.entity.ScheduledNotificationEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class ScheduledNotificationRepositoryAdapter implements ScheduledNotificationRepository {

    @Override
    @Transactional
    public void save(ScheduledNotification notification) {
        ScheduledNotificationEntity entity = new ScheduledNotificationEntity();
        entity.matchId = notification.getMatchId();
        entity.homeTeamId = notification.getHomeTeamId();
        entity.awayTeamId = notification.getAwayTeamId();
        entity.kickoffAt = notification.getKickoffAt();
        entity.notifyAt = notification.getNotifyAt();
        entity.sent = notification.isSent();
        entity.persist();
    }

    @Override
    public List<ScheduledNotification> findDue(Instant now) {
        return ScheduledNotificationEntity
                .<ScheduledNotificationEntity>find("sent = false AND notifyAt <= ?1", now)
                .stream()
                .map(ScheduledNotificationRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void markSent(Long id) {
        ScheduledNotificationEntity entity = ScheduledNotificationEntity.findById(id);
        if (entity != null) {
            entity.sent = true;
            entity.sentAt = Instant.now();
        }
    }

    @Override
    public boolean existsByMatchIdAndNotifyAt(String matchId, Instant notifyAt) {
        return ScheduledNotificationEntity.count("matchId = ?1 AND notifyAt = ?2", matchId, notifyAt) > 0;
    }

    private static ScheduledNotification toDomain(ScheduledNotificationEntity e) {
        return new ScheduledNotification(e.id, e.matchId, e.homeTeamId, e.awayTeamId,
                e.kickoffAt, e.notifyAt, e.sent);
    }
}
