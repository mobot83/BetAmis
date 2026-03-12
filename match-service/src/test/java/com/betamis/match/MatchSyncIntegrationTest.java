package com.betamis.match;

import com.betamis.match.domain.event.MatchFinished;
import com.betamis.match.domain.event.MatchStarted;
import com.betamis.match.domain.model.match.MatchStatus;
import com.betamis.match.domain.port.in.SyncMatches;
import com.betamis.match.domain.port.out.MatchEventPublisher;
import com.betamis.match.domain.port.out.MatchRepository;
import com.betamis.match.infrastructure.client.WireMockFootballDataResource;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.persistence.EntityManager;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@QuarkusTestResource(WireMockFootballDataResource.class)
class MatchSyncIntegrationTest {

    @Inject SyncMatches syncMatches;
    @Inject MatchRepository matchRepository;
    @Inject EntityManager em;
    @InjectMock MatchEventPublisher eventPublisher;

    WireMockServer wireMock;

    @BeforeEach
    void setUp() {
        wireMock = WireMockFootballDataResource.SERVER;
        wireMock.resetAll();
        QuarkusTransaction.requiringNew().run(() -> em.createQuery("DELETE FROM MatchEntity").executeUpdate());
    }

    @Test
    @DisplayName("Should create a PLANNED match when API returns SCHEDULED status")
    void shouldCreatePlannedMatchFromScheduledStatus() {
        stubMatches(55501L, "SCHEDULED", null, null);

        syncMatches.syncByCompetition("PL");

        var found = matchRepository.findByExternalId(55501L);
        assertTrue(found.isPresent());
        assertEquals(MatchStatus.PLANNED, found.get().getStatus());
        assertEquals("1", found.get().getHomeTeamId());
        assertEquals("2", found.get().getAwayTeamId());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Should create a STARTED match when API returns IN_PLAY status")
    void shouldCreateStartedMatchFromInPlayStatus() {
        stubMatches(55502L, "IN_PLAY", 1, 0);

        syncMatches.syncByCompetition("PL");

        var found = matchRepository.findByExternalId(55502L);
        assertTrue(found.isPresent());
        assertEquals(MatchStatus.STARTED, found.get().getStatus());
        assertEquals(1, found.get().getHomeTeamScore());
        assertEquals(0, found.get().getAwayTeamScore());
    }

    @Test
    @DisplayName("Should create a FINISHED match with correct score when API returns FINISHED status")
    void shouldCreateFinishedMatchWithScore() {
        stubMatches(55503L, "FINISHED", 3, 2);

        syncMatches.syncByCompetition("PL");

        var found = matchRepository.findByExternalId(55503L);
        assertTrue(found.isPresent());
        assertEquals(MatchStatus.FINISHED, found.get().getStatus());
        assertEquals(3, found.get().getHomeTeamScore());
        assertEquals(2, found.get().getAwayTeamScore());
    }

    @Test
    @DisplayName("Should emit MatchStarted when match transitions to STARTED")
    void shouldEmitMatchStartedEvent() {
        stubMatches(55505L, "SCHEDULED", null, null);
        syncMatches.syncByCompetition("PL");

        wireMock.resetAll();
        stubMatches(55505L, "IN_PLAY", 0, 0);
        syncMatches.syncByCompetition("PL");

        var captor = ArgumentCaptor.forClass(MatchStarted.class);
        verify(eventPublisher).publish(captor.capture());
        verify(eventPublisher, never()).publish(any(MatchFinished.class));

        String expectedMatchId = matchRepository.findByExternalId(55505L).orElseThrow().getId();
        assertEquals(expectedMatchId, captor.getValue().matchId());
    }

    @Test
    @DisplayName("Should emit MatchFinished when match transitions to FINISHED")
    void shouldEmitMatchFinishedEvent() {
        stubMatches(55506L, "IN_PLAY", 1, 0);
        syncMatches.syncByCompetition("PL");
        clearInvocations(eventPublisher);

        wireMock.resetAll();
        stubMatches(55506L, "FINISHED", 2, 1);
        syncMatches.syncByCompetition("PL");

        var captor = ArgumentCaptor.forClass(MatchFinished.class);
        verify(eventPublisher).publish(captor.capture());
        verify(eventPublisher, never()).publish(any(MatchStarted.class));

        String expectedMatchId = matchRepository.findByExternalId(55506L).orElseThrow().getId();
        assertEquals(expectedMatchId, captor.getValue().matchId());
    }

    @Test
    @DisplayName("Should update match and emit MatchFinished on subsequent sync")
    void shouldUpdateMatchAndEmitFinishedEvent() {
        stubMatches(55504L, "SCHEDULED", null, null);
        syncMatches.syncByCompetition("PL");

        wireMock.resetAll();
        stubMatches(55504L, "FINISHED", 2, 0);
        syncMatches.syncByCompetition("PL");

        var updated = matchRepository.findByExternalId(55504L).orElseThrow();
        assertEquals(MatchStatus.FINISHED, updated.getStatus());
        assertEquals(2, updated.getHomeTeamScore());
        assertEquals(0, updated.getAwayTeamScore());
        verify(eventPublisher).publish(any(MatchFinished.class));
    }

    // --- helpers ---

    private void stubMatches(long id, String status, Integer homeScore, Integer awayScore) {
        String home = homeScore != null ? String.valueOf(homeScore) : "null";
        String away = awayScore != null ? String.valueOf(awayScore) : "null";
        wireMock.stubFor(get(urlPathEqualTo("/v4/competitions/PL/matches"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "count": 1,
                                  "matches": [{
                                    "id": %d,
                                    "status": "%s",
                                    "utcDate": "2024-01-15T20:00:00Z",
                                    "homeTeam": { "id": 1, "name": "Arsenal", "shortName": "Arsenal", "tla": "ARS" },
                                    "awayTeam": { "id": 2, "name": "Chelsea", "shortName": "Chelsea", "tla": "CHE" },
                                    "score": {
                                      "winner": null,
                                      "fullTime":  { "home": %s, "away": %s },
                                      "halfTime":  { "home": null, "away": null }
                                    }
                                  }]
                                }
                                """.formatted(id, status, home, away))));
    }
}
