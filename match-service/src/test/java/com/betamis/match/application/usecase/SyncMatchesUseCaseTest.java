package com.betamis.match.application.usecase;

import com.betamis.match.domain.event.MatchFinished;
import com.betamis.match.domain.event.MatchScheduled;
import com.betamis.match.domain.event.MatchStarted;
import com.betamis.match.domain.model.match.ExternalMatch;
import com.betamis.match.domain.model.match.Match;
import com.betamis.match.domain.model.match.MatchStatus;
import com.betamis.match.domain.port.out.MatchDataProvider;
import com.betamis.match.domain.port.out.MatchEventPublisher;
import com.betamis.match.domain.port.out.MatchRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncMatchesUseCaseTest {

    @Mock MatchDataProvider matchDataProvider;
    @Mock MatchRepository matchRepository;
    @Mock MatchEventPublisher eventPublisher;

    SimpleMeterRegistry registry;
    SyncMatchesUseCase useCase;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        useCase = new SyncMatchesUseCase(matchDataProvider, matchRepository, eventPublisher, registry);
    }

    @Test
    @DisplayName("Should create a new match when not found in repository")
    void shouldCreateNewMatchWhenNotFound() {
        when(matchDataProvider.getMatchesByCompetition("PL"))
                .thenReturn(List.of(externalMatch(11111L, "SCHEDULED", 0, 0)));
        when(matchRepository.findByExternalId(11111L)).thenReturn(Optional.empty());

        useCase.syncByCompetition("PL");

        var captor = ArgumentCaptor.forClass(Match.class);
        verify(matchRepository).save(captor.capture());
        Match saved = captor.getValue();
        assertEquals(Optional.of(11111L), saved.getExternalId());
        assertEquals("1", saved.getHomeTeamId());
        assertEquals("2", saved.getAwayTeamId());
        assertEquals(MatchStatus.PLANNED, saved.getStatus());
        assertEquals(0, saved.getHomeTeamScore());
        assertEquals(0, saved.getAwayTeamScore());
    }

    @Test
    @DisplayName("Should emit MatchScheduled when a new PLANNED match with kickoff time is discovered")
    void shouldEmitMatchScheduledForNewPlannedMatch() {
        Instant kickoff = Instant.parse("2026-04-01T20:00:00Z");
        when(matchDataProvider.getMatchesByCompetition("PL"))
                .thenReturn(List.of(externalMatchWithKickoff(11112L, "SCHEDULED", 0, 0, kickoff)));
        when(matchRepository.findByExternalId(11112L)).thenReturn(Optional.empty());

        Instant before = Instant.now();
        useCase.syncByCompetition("PL");
        Instant after = Instant.now();

        ArgumentCaptor<MatchScheduled> captor = ArgumentCaptor.forClass(MatchScheduled.class);
        verify(eventPublisher).publish(captor.capture());
        verify(eventPublisher, never()).publish(any(MatchStarted.class));
        verify(eventPublisher, never()).publish(any(MatchFinished.class));
        MatchScheduled event = captor.getValue();
        assertEquals(kickoff, event.kickoffAt());
        assertFalse(event.occurredAt().isBefore(before));
        assertFalse(event.occurredAt().isAfter(after));
    }

    @Test
    @DisplayName("Should update existing match with new score and status")
    void shouldUpdateExistingMatch() {
        Match existing = Match.fromExternal(22222L, "1", "2", 0, 0, MatchStatus.PLANNED, null);
        when(matchDataProvider.getMatchesByCompetition("PL"))
                .thenReturn(List.of(externalMatch(22222L, "FINISHED", 2, 1)));
        when(matchRepository.findByExternalId(22222L)).thenReturn(Optional.of(existing));

        useCase.syncByCompetition("PL");

        var captor = ArgumentCaptor.forClass(Match.class);
        verify(matchRepository).save(captor.capture());
        Match saved = captor.getValue();
        assertEquals(existing.getId(), saved.getId());
        assertEquals(MatchStatus.FINISHED, saved.getStatus());
        assertEquals(2, saved.getHomeTeamScore());
        assertEquals(1, saved.getAwayTeamScore());
    }

    @Test
    @DisplayName("Should emit MatchStarted when match transitions from PLANNED to STARTED")
    void shouldEmitMatchStartedOnTransition() {
        Match existing = Match.fromExternal(44444L, "1", "2", 0, 0, MatchStatus.PLANNED, null);
        when(matchDataProvider.getMatchesByCompetition("PL"))
                .thenReturn(List.of(externalMatch(44444L, "IN_PLAY", 0, 0)));
        when(matchRepository.findByExternalId(44444L)).thenReturn(Optional.of(existing));

        useCase.syncByCompetition("PL");

        verify(eventPublisher).publish(any(MatchStarted.class));
        verify(eventPublisher, never()).publish(any(MatchFinished.class));
    }

    @Test
    @DisplayName("Should emit MatchFinished when match transitions from STARTED to FINISHED")
    void shouldEmitMatchFinishedOnTransition() {
        Match existing = Match.fromExternal(55555L, "1", "2", 1, 0, MatchStatus.STARTED, null);
        when(matchDataProvider.getMatchesByCompetition("PL"))
                .thenReturn(List.of(externalMatch(55555L, "FINISHED", 2, 1)));
        when(matchRepository.findByExternalId(55555L)).thenReturn(Optional.of(existing));

        useCase.syncByCompetition("PL");

        verify(eventPublisher).publish(any(MatchFinished.class));
        verify(eventPublisher, never()).publish(any(MatchStarted.class));
    }

    @Test
    @DisplayName("Should not emit any event when status has not changed")
    void shouldNotEmitEventWhenStatusUnchanged() {
        Match existing = Match.fromExternal(66666L, "1", "2", 1, 0, MatchStatus.STARTED, null);
        when(matchDataProvider.getMatchesByCompetition("PL"))
                .thenReturn(List.of(externalMatch(66666L, "IN_PLAY", 2, 0)));
        when(matchRepository.findByExternalId(66666L)).thenReturn(Optional.of(existing));

        useCase.syncByCompetition("PL");

        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Should not emit any event for a newly discovered non-PLANNED match")
    void shouldNotEmitEventForNewNonPlannedMatch() {
        when(matchDataProvider.getMatchesByCompetition("PL"))
                .thenReturn(List.of(externalMatch(77777L, "IN_PLAY", 1, 0)));
        when(matchRepository.findByExternalId(77777L)).thenReturn(Optional.empty());

        useCase.syncByCompetition("PL");

        verifyNoInteractions(eventPublisher);
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @DisplayName("Should map external status correctly")
    @CsvSource({
            "SCHEDULED, PLANNED",
            "TIMED,     PLANNED",
            "POSTPONED, PLANNED",
            "IN_PLAY,   STARTED",
            "PAUSED,    STARTED",
            "FINISHED,  FINISHED",
            "AWARDED,   FINISHED"
    })
    void shouldMapExternalStatusToDomainStatus(String externalStatus, MatchStatus expectedStatus) {
        when(matchDataProvider.getMatchesByCompetition("PL"))
                .thenReturn(List.of(externalMatch(33333L, externalStatus, 0, 0)));
        when(matchRepository.findByExternalId(anyLong())).thenReturn(Optional.empty());

        useCase.syncByCompetition("PL");

        var captor = ArgumentCaptor.forClass(Match.class);
        verify(matchRepository).save(captor.capture());
        assertEquals(expectedStatus, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Should handle empty match list without calling save")
    void shouldHandleEmptyMatchList() {
        when(matchDataProvider.getMatchesByCompetition("PL")).thenReturn(List.of());

        useCase.syncByCompetition("PL");

        verify(matchRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Should propagate exception when data provider throws")
    void shouldPropagateDataProviderException() {
        when(matchDataProvider.getMatchesByCompetition("PL"))
                .thenThrow(new RuntimeException("API unavailable"));

        assertThrows(RuntimeException.class, () -> useCase.syncByCompetition("PL"));
        verify(matchRepository, never()).save(any());
    }

    @Test
    @DisplayName("betamis_matches_synced_total increments once per match processed, 0 on empty list")
    void shouldIncrementMatchesSyncedCounterPerMatch() {
        assertEquals(0.0, registry.counter("betamis_matches_synced_total").count());

        when(matchDataProvider.getMatchesByCompetition("PL")).thenReturn(List.of());
        useCase.syncByCompetition("PL");
        assertEquals(0.0, registry.counter("betamis_matches_synced_total").count());

        when(matchDataProvider.getMatchesByCompetition("PL")).thenReturn(List.of(
                externalMatch(11L, "SCHEDULED", 0, 0),
                externalMatch(12L, "SCHEDULED", 0, 0),
                externalMatch(13L, "SCHEDULED", 0, 0)
        ));
        when(matchRepository.findByExternalId(anyLong())).thenReturn(Optional.empty());
        useCase.syncByCompetition("PL");
        assertEquals(3.0, registry.counter("betamis_matches_synced_total").count());
    }

    // --- helpers ---

    private static ExternalMatch externalMatch(long id, String status, int homeScore, int awayScore) {
        return new ExternalMatch(id, status, "1", "2", homeScore, awayScore, null);
    }

    private static ExternalMatch externalMatchWithKickoff(long id, String status, int homeScore, int awayScore, Instant kickoffAt) {
        return new ExternalMatch(id, status, "1", "2", homeScore, awayScore, kickoffAt);
    }
}
