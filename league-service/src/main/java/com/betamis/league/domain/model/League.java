package com.betamis.league.domain.model;

import com.betamis.league.domain.event.LeagueCreated;
import com.betamis.league.domain.event.MemberJoined;
import com.betamis.league.domain.exception.AlreadyMemberException;
import com.betamis.league.domain.exception.InvalidInvitationCodeException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * League aggregate root.
 * Encapsulates all business rules for league membership and invitations.
 */
public class League {

    private final String id;
    private final String name;
    private final String ownerId;
    private final List<Membership> memberships;
    private final List<Invitation> invitations;
    private final Instant createdAt;

    /** Events raised during the current operation — cleared after being consumed. */
    private final List<Object> domainEvents = new ArrayList<>();

    private League(String id, String name, String ownerId,
                   List<Membership> memberships, List<Invitation> invitations,
                   Instant createdAt) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("League id cannot be null or blank");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("League name cannot be null or blank");
        if (name.length() > 255) throw new IllegalArgumentException("League name must not exceed 255 characters");
        if (ownerId == null || ownerId.isBlank()) throw new IllegalArgumentException("ownerId cannot be null or blank");
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.memberships = new ArrayList<>(memberships);
        this.invitations = new ArrayList<>(invitations);
        this.createdAt = createdAt;
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    /**
     * Create a new league. The owner is automatically added as the first member
     * and an initial invitation code is generated.
     */
    public static League create(String name, String ownerId) {
        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Invitation invitation = Invitation.generate(now);
        Membership ownerMembership = new Membership(ownerId, now);
        League league = new League(id, name, ownerId,
                List.of(ownerMembership), List.of(invitation), now);
        league.domainEvents.add(LeagueCreated.of(league.id, league.name, league.ownerId, invitation.code()));
        return league;
    }

    /**
     * Reconstruct a League from persisted state (no domain events raised).
     */
    public static League reconstitute(String id, String name, String ownerId,
                                      List<Membership> memberships, List<Invitation> invitations,
                                      Instant createdAt) {
        return new League(id, name, ownerId, memberships, invitations, createdAt);
    }

    // ── Business logic ────────────────────────────────────────────────────────

    /**
     * Join the league using an invitation code.
     * Raises a {@link MemberJoined} domain event.
     *
     * @param userId the user joining
     * @param code   the 6-character invitation code
     * @throws AlreadyMemberException        if the user is already a member
     * @throws InvalidInvitationCodeException if the code is unknown or expired
     */
    public void join(String userId, String code) {
        if (memberships.stream().anyMatch(m -> m.userId().equals(userId))) {
            throw new AlreadyMemberException(userId, id);
        }
        Instant now = Instant.now();
        invitations.stream()
                .filter(inv -> inv.code().equals(code) && inv.isValid(now))
                .findFirst()
                .orElseThrow(() -> new InvalidInvitationCodeException(code));

        memberships.add(new Membership(userId, Instant.now()));
        domainEvents.add(MemberJoined.of(id, userId));
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String getId() { return id; }
    public String getName() { return name; }
    public String getOwnerId() { return ownerId; }
    public List<Membership> getMemberships() { return Collections.unmodifiableList(memberships); }
    public List<Invitation> getInvitations() { return Collections.unmodifiableList(invitations); }
    public Instant getCreatedAt() { return createdAt; }

    /**
     * Pop and clear all raised domain events.
     */
    public List<Object> pollDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }
}

