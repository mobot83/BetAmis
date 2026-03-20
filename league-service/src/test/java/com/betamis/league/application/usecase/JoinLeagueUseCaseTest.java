package com.betamis.league.application.usecase;

import com.betamis.league.domain.event.MemberJoined;
import com.betamis.league.domain.exception.AlreadyMemberException;
import com.betamis.league.domain.exception.InvalidInvitationCodeException;
import com.betamis.league.domain.exception.LeagueNotFoundException;
import com.betamis.league.domain.model.Invitation;
import com.betamis.league.domain.model.League;
import com.betamis.league.domain.model.Membership;
import com.betamis.league.domain.port.out.LeagueEventPublisher;
import com.betamis.league.domain.port.out.LeagueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JoinLeagueUseCaseTest {

    private LeagueRepository repository;
    private LeagueEventPublisher publisher;
    private JoinLeagueUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(LeagueRepository.class);
        publisher = mock(LeagueEventPublisher.class);
        useCase = new JoinLeagueUseCase(repository, publisher);
    }

    @Test
    @DisplayName("join() should add member, save league and publish MemberJoined event")
    void shouldAddMemberAndPublishEvent() {
        Invitation inv = Invitation.generate(Instant.now());
        League league = League.reconstitute("league-1", "Test", "user-1",
                List.of(new Membership("user-1", Instant.now())),
                List.of(inv),
                Instant.now());
        when(repository.findById("league-1")).thenReturn(Optional.of(league));

        useCase.join("league-1", "user-2", inv.code());

        verify(repository, times(1)).save(league);
        ArgumentCaptor<MemberJoined> captor = ArgumentCaptor.forClass(MemberJoined.class);
        verify(publisher, times(1)).publish(captor.capture());
        assertEquals("league-1", captor.getValue().leagueId());
        assertEquals("user-2", captor.getValue().userId());
    }

    @Test
    @DisplayName("join() should throw LeagueNotFoundException when league does not exist")
    void shouldThrowWhenLeagueNotFound() {
        when(repository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(LeagueNotFoundException.class,
                () -> useCase.join("unknown", "user-2", "ABC123"));
        verify(publisher, never()).publish(any(MemberJoined.class));
    }

    @Test
    @DisplayName("join() should throw InvalidInvitationCodeException for bad code")
    void shouldThrowForInvalidCode() {
        League league = League.reconstitute("league-1", "Test", "user-1",
                List.of(new Membership("user-1", Instant.now())),
                List.of(Invitation.generate(Instant.now())),
                Instant.now());
        when(repository.findById("league-1")).thenReturn(Optional.of(league));

        assertThrows(InvalidInvitationCodeException.class,
                () -> useCase.join("league-1", "user-2", "BADCOD"));
        verify(publisher, never()).publish(any(MemberJoined.class));
    }

    @Test
    @DisplayName("join() should throw AlreadyMemberException when user is already a member")
    void shouldThrowWhenAlreadyMember() {
        Invitation inv = Invitation.generate(Instant.now());
        League league = League.reconstitute("league-1", "Test", "user-1",
                List.of(new Membership("user-1", Instant.now())),
                List.of(inv),
                Instant.now());
        when(repository.findById("league-1")).thenReturn(Optional.of(league));

        assertThrows(AlreadyMemberException.class,
                () -> useCase.join("league-1", "user-1", inv.code()));
        verify(repository, never()).save(any());
        verify(publisher, never()).publish(any(MemberJoined.class));
    }
}

