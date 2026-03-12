package com.betamis.match.application.usecase;

import com.betamis.match.domain.event.MatchFinished;
import com.betamis.match.domain.event.MatchStarted;
import com.betamis.match.domain.model.match.Match;
import com.betamis.match.domain.model.match.MatchStatus;
import com.betamis.match.domain.port.out.MatchEventPublisher;
import com.betamis.match.domain.port.out.MatchRepository;
import com.betamis.match.infrastructure.client.FootballDataClient;
import com.betamis.match.infrastructure.client.dto.FootballMatchListResponse;
import com.betamis.match.infrastructure.client.dto.FootballMatchResponse;
import com.betamis.match.infrastructure.client.dto.FootballScoreDto;
import com.betamis.match.infrastructure.client.dto.FootballTeamDto;
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

    @Mock FootballDataClient footballDataClient;
    @Mock MatchRepository matchRepository;
    @Mock MatchEventPublisher eventPublisher;

    SyncMatchesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SyncMatchesUseCase(footballDataClient, matchRepository, eventPublisher);
    }

    @Test
    @DisplayName("Should create a new match when not found in repository")
    void shouldCreateNewMatchWhenNotFound() {
        var external = matchResponse(11111L, "SCHEDULED", null, null);
        when(footballDataClient.getMatchesByCompetition("PL", null, null))
                .thenReturn(new FootballMatchListResponse(1, List.of(external)));
        when(matchRepository.findByExternalId(11111L)).thenReturn(Optional.empty());

        useCase.syncByCompetition("PL");

        var captor = ArgumentCaptor.forClass(Match.class);
        verify(matchRepository).save(captor.capture());
        Match saved = captor.getValue();
        assertEquals(11111L, saved.getExternalId());
        assertEquals("1", saved.getHomeTeamId());
        assertEquals("2", saved.getAwayTeamId());
        assertEquals(MatchStatus.PLANNED, saved.getStatus());
        assertEquals(0, saved.getHomeTeamScore());
        assertEquals(0, saved.getAwayTeamScore());
    }

    @Test
    @DisplayName("Should update existing match with new score and status")
    void shouldUpdateExistingMatch() {
        Match existing = Match.fromExternal(22222L, "1", "2", 0, 0, MatchStatus.PLANNED);
        var external = matchResponse(22222L, "FINISHED", 2, 1);
        when(footballDataClient.getMatchesByCompetition("PL", null, null))
                .thenReturn(new FootballMatchListResponse(1, List.of(external)));
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
        Match existing = Match.fromExternal(44444L, "1", "2", 0, 0, MatchStatus.PLANNED);
        when(footballDataClient.getMatchesByCompetition("PL", null, null))
                .thenReturn(new FootballMatchListResponse(1, List.of(matchResponse(44444L, "IN_PLAY", 0, 0))));
        when(matchRepository.findByExternalId(44444L)).thenReturn(Optional.of(existing));

        useCase.syncByCompetition("PL");

        verify(eventPublisher).publish(any(MatchStarted.class));
        verify(eventPublisher, never()).publish(any(MatchFinished.class));
    }

    @Test
    @DisplayName("Should emit MatchFinished when match transitions from STARTED to FINISHED")
    void shouldEmitMatchFinishedOnTransition() {
        Match existing = Match.fromExternal(55555L, "1", "2", 1, 0, MatchStatus.STARTED);
        when(footballDataClient.getMatchesByCompetition("PL", null, null))
                .thenReturn(new FootballMatchListResponse(1, List.of(matchResponse(55555L, "FINISHED", 2, 1))));
        when(matchRepository.findByExternalId(55555L)).thenReturn(Optional.of(existing));

        useCase.syncByCompetition("PL");

        verify(eventPublisher).publish(any(MatchFinished.class));
        verify(eventPublisher, never()).publish(any(MatchStarted.class));
    }

    @Test
    @DisplayName("Should not emit any event when status has not changed")
    void shouldNotEmitEventWhenStatusUnchanged() {
        Match existing = Match.fromExternal(66666L, "1", "2", 1, 0, MatchStatus.STARTED);
        when(footballDataClient.getMatchesByCompetition("PL", null, null))
                .thenReturn(new FootballMatchListResponse(1, List.of(matchResponse(66666L, "IN_PLAY", 2, 0))));
        when(matchRepository.findByExternalId(66666L)).thenReturn(Optional.of(existing));

        useCase.syncByCompetition("PL");

        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Should not emit any event for a newly created match")
    void shouldNotEmitEventForNewMatch() {
        when(footballDataClient.getMatchesByCompetition("PL", null, null))
                .thenReturn(new FootballMatchListResponse(1, List.of(matchResponse(77777L, "IN_PLAY", 1, 0))));
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
        when(footballDataClient.getMatchesByCompetition("PL", null, null))
                .thenReturn(new FootballMatchListResponse(1, List.of(matchResponse(33333L, externalStatus, null, null))));
        when(matchRepository.findByExternalId(anyLong())).thenReturn(Optional.empty());

        useCase.syncByCompetition("PL");

        var captor = ArgumentCaptor.forClass(Match.class);
        verify(matchRepository).save(captor.capture());
        assertEquals(expectedStatus, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Should handle empty match list without calling save")
    void shouldHandleEmptyMatchList() {
        when(footballDataClient.getMatchesByCompetition("PL", null, null))
                .thenReturn(new FootballMatchListResponse(0, List.of()));

        useCase.syncByCompetition("PL");

        verify(matchRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    // --- helpers ---

    private static FootballMatchResponse matchResponse(long id, String status, Integer homeScore, Integer awayScore) {
        var home = new FootballTeamDto(1L, "Arsenal", "Arsenal", "ARS");
        var away = new FootballTeamDto(2L, "Chelsea", "Chelsea", "CHE");
        var score = new FootballScoreDto(null, new FootballScoreDto.HalfDto(homeScore, awayScore), null);
        return new FootballMatchResponse(id, status, Instant.now(), home, away, score);
    }
}
