package com.betamis.league.application.usecase;

import com.betamis.league.domain.exception.LeagueNotFoundException;
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

class ListMembersUseCaseTest {

    private LeagueRepository repository;
    private ListMembersUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(LeagueRepository.class);
        useCase = new ListMembersUseCase(repository);
    }

    @Test
    @DisplayName("list() should return members when league exists")
    void shouldReturnMembersWhenLeagueFound() {
        List<Membership> members = List.of(
                new Membership("user-1", Instant.now()),
                new Membership("user-2", Instant.now()));
        when(repository.findMembersByLeagueId("league-1")).thenReturn(Optional.of(members));

        List<Membership> result = useCase.list("league-1");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(m -> m.userId().equals("user-1")));
        assertTrue(result.stream().anyMatch(m -> m.userId().equals("user-2")));
    }

    @Test
    @DisplayName("list() should throw LeagueNotFoundException when league does not exist")
    void shouldThrowWhenLeagueNotFound() {
        when(repository.findMembersByLeagueId("unknown")).thenReturn(Optional.empty());

        assertThrows(LeagueNotFoundException.class, () -> useCase.list("unknown"));
    }
}
