package com.betamis.league.domain.model;

import com.betamis.league.domain.event.LeagueCreated;
import com.betamis.league.domain.event.MemberJoined;
import com.betamis.league.domain.exception.AlreadyMemberException;
import com.betamis.league.domain.exception.InvalidInvitationCodeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeagueTest {

    @Test
    @DisplayName("create() should produce a league with the owner as first member and raise LeagueCreated event")
    void createShouldAddOwnerAsMemberAndRaiseEvent() {
        League league = League.create("World Cup 2026", "user-1");

        assertNotNull(league.getId());
        assertEquals("World Cup 2026", league.getName());
        assertEquals("user-1", league.getOwnerId());
        assertEquals(1, league.getMemberships().size());
        assertEquals("user-1", league.getMemberships().get(0).userId());
        assertEquals(1, league.getInvitations().size());

        List<Object> events = league.pollDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(LeagueCreated.class, events.get(0));
        LeagueCreated event = (LeagueCreated) events.get(0);
        assertEquals(league.getId(), event.leagueId());
        assertEquals("user-1", event.ownerId());
        assertEquals(6, event.invitationCode().length());
    }

    @Test
    @DisplayName("pollDomainEvents() should clear events after collection")
    void pollShouldClearEvents() {
        League league = League.create("My League", "owner");
        assertFalse(league.pollDomainEvents().isEmpty());
        assertTrue(league.pollDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("join() should add a member and raise MemberJoined event")
    void joinShouldAddMemberAndRaiseEvent() {
        League league = League.create("World Cup 2026", "user-1");
        league.pollDomainEvents(); // consume creation event

        String code = league.getInvitations().get(0).code();
        league.join("user-2", code);

        assertEquals(2, league.getMemberships().size());
        assertTrue(league.getMemberships().stream().anyMatch(m -> m.userId().equals("user-2")));

        List<Object> events = league.pollDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(MemberJoined.class, events.get(0));
        MemberJoined event = (MemberJoined) events.get(0);
        assertEquals(league.getId(), event.leagueId());
        assertEquals("user-2", event.userId());
    }

    @Test
    @DisplayName("join() should throw AlreadyMemberException when user is already a member")
    void joinShouldThrowWhenAlreadyMember() {
        League league = League.create("World Cup 2026", "user-1");
        String code = league.getInvitations().get(0).code();

        assertThrows(AlreadyMemberException.class, () -> league.join("user-1", code));
    }

    @Test
    @DisplayName("join() should throw InvalidInvitationCodeException for unknown code")
    void joinShouldThrowForUnknownCode() {
        League league = League.create("World Cup 2026", "user-1");

        assertThrows(InvalidInvitationCodeException.class, () -> league.join("user-2", "BADCOD"));
    }

    @Test
    @DisplayName("join() should throw InvalidInvitationCodeException for expired invitation")
    void joinShouldThrowForExpiredInvitation() {
        Instant past = Instant.now().minus(8, ChronoUnit.DAYS);
        Invitation expired = Invitation.of("EXP001", past, past.plus(7, ChronoUnit.DAYS));
        League league = League.reconstitute("id-1", "Test", "user-1",
                List.of(new Membership("user-1", Instant.now())),
                List.of(expired),
                Instant.now());

        assertThrows(InvalidInvitationCodeException.class, () -> league.join("user-2", "EXP001"));
    }

    @Test
    @DisplayName("create() should throw IllegalArgumentException for blank name")
    void createShouldThrowForBlankName() {
        assertThrows(IllegalArgumentException.class, () -> League.create("  ", "user-1"));
    }

    @Test
    @DisplayName("Invitation should be valid within 7 days")
    void invitationShouldBeValidWithin7Days() {
        Invitation inv = Invitation.generate(Instant.now());
        assertTrue(inv.isValid(Instant.now()));
    }

    @Test
    @DisplayName("Invitation should be invalid after 7 days")
    void invitationShouldBeInvalidAfter7Days() {
        Instant past = Instant.now().minus(8, ChronoUnit.DAYS);
        Invitation expired = Invitation.of("ABC123", past, past.plus(7, ChronoUnit.DAYS));
        assertFalse(expired.isValid(Instant.now()));
    }
}

