package com.betamis.notification.application.usecase;

import com.betamis.notification.domain.model.ScheduledNotification;
import com.betamis.notification.domain.port.out.ScheduledNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleMatchNotificationsUseCaseTest {

    @Mock
    ScheduledNotificationRepository repository;

    ScheduleMatchNotificationsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ScheduleMatchNotificationsUseCase(repository);
    }

    @Test
    @DisplayName("Creates two notifications: J-1 and H-1")
    void creates_two_notifications() {
        Instant kickoff = Instant.parse("2026-05-01T20:00:00Z");
        when(repository.existsByMatchIdAndNotifyAt(any(), any())).thenReturn(false);

        useCase.schedule("match-1", "team-a", "team-b", kickoff);

        ArgumentCaptor<ScheduledNotification> captor = ArgumentCaptor.forClass(ScheduledNotification.class);
        verify(repository, times(2)).save(captor.capture());

        List<ScheduledNotification> saved = captor.getAllValues();
        Instant oneDayBefore = kickoff.minus(1, ChronoUnit.DAYS);
        Instant oneHourBefore = kickoff.minus(1, ChronoUnit.HOURS);

        assertTrue(saved.stream().anyMatch(n -> n.getNotifyAt().equals(oneDayBefore)));
        assertTrue(saved.stream().anyMatch(n -> n.getNotifyAt().equals(oneHourBefore)));
        saved.forEach(n -> {
            assertEquals("match-1", n.getMatchId());
            assertEquals("team-a", n.getHomeTeamId());
            assertEquals("team-b", n.getAwayTeamId());
            assertEquals(kickoff, n.getKickoffAt());
            assertFalse(n.isSent());
        });
    }

    @Test
    @DisplayName("Skips duplicate notifications for same matchId + notifyAt")
    void skips_duplicates() {
        Instant kickoff = Instant.parse("2026-05-01T20:00:00Z");
        when(repository.existsByMatchIdAndNotifyAt(eq("match-1"), any())).thenReturn(true);

        useCase.schedule("match-1", "team-a", "team-b", kickoff);

        verify(repository, never()).save(any());
    }
}
