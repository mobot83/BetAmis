package com.betamis.league.application.usecase;

import com.betamis.league.domain.event.LeagueCreated;
import com.betamis.league.domain.model.League;
import com.betamis.league.domain.port.out.LeagueEventPublisher;
import com.betamis.league.domain.port.out.LeagueRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateLeagueUseCaseTest {

    private LeagueRepository repository;
    private LeagueEventPublisher publisher;
    private SimpleMeterRegistry registry;
    private CreateLeagueUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(LeagueRepository.class);
        publisher = mock(LeagueEventPublisher.class);
        registry = new SimpleMeterRegistry();
        useCase = new CreateLeagueUseCase(repository, publisher, registry);
    }

    @Test
    @DisplayName("create() should save league and publish LeagueCreated event")
    void shouldSaveLeagueAndPublishEvent() {
        League league = useCase.create("Champions 2026", "user-42");

        assertNotNull(league.getId());
        assertEquals("Champions 2026", league.getName());
        assertEquals("user-42", league.getOwnerId());

        verify(repository, times(1)).save(league);

        ArgumentCaptor<LeagueCreated> captor = ArgumentCaptor.forClass(LeagueCreated.class);
        verify(publisher, times(1)).publish(captor.capture());
        assertEquals(league.getId(), captor.getValue().leagueId());
        assertEquals("user-42", captor.getValue().ownerId());
    }

    @Test
    @DisplayName("create() should not publish events twice if called again")
    void shouldClearDomainEventsAfterPublishing() {
        League league = useCase.create("Champions 2026", "user-42");
        // Events are consumed during create(); pollDomainEvents() must return empty afterwards
        assertTrue(league.pollDomainEvents().isEmpty());
        verify(publisher, times(1)).publish(any(LeagueCreated.class));
    }

    @Test
    @DisplayName("betamis_leagues_created_total is pre-registered at 0 and increments per successful create()")
    void shouldIncrementLeaguesCreatedCounter() {
        assertEquals(0.0, registry.counter("betamis_leagues_created_total").count());

        useCase.create("League A", "user-1");
        assertEquals(1.0, registry.counter("betamis_leagues_created_total").count());

        useCase.create("League B", "user-2");
        assertEquals(2.0, registry.counter("betamis_leagues_created_total").count());
    }
}

