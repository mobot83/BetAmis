package com.betamis.league.application.usecase;

import com.betamis.league.domain.exception.LeagueNotFoundException;
import com.betamis.league.domain.model.Invitation;
import com.betamis.league.domain.model.League;
import com.betamis.league.domain.model.Membership;
import com.betamis.league.domain.port.out.LeagueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetLeagueUseCaseTest {

    private LeagueRepository repository;
    private GetLeagueUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(LeagueRepository.class);
        useCase = new GetLeagueUseCase(repository);
    }

    @Test
    @DisplayName("get() should return the league when it exists")
    void shouldReturnLeagueWhenFound() {
        League league = League.reconstitute("league-1", "Test League", "owner-1",
                List.of(new Membership("owner-1", Instant.now())),
                List.of(Invitation.generate(Instant.now())),
                Instant.now());
        when(repository.findById("league-1")).thenReturn(Optional.of(league));

        League result = useCase.get("league-1");

        assertEquals("league-1", result.getId());
        assertEquals("Test League", result.getName());
    }

    @Test
    @DisplayName("get() should throw LeagueNotFoundException when league does not exist")
    void shouldThrowWhenLeagueNotFound() {
        when(repository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(LeagueNotFoundException.class, () -> useCase.get("unknown"));
    }
}
