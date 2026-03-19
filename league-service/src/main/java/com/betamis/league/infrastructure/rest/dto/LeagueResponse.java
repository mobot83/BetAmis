package com.betamis.league.infrastructure.rest.dto;

import com.betamis.league.domain.model.Invitation;
import com.betamis.league.domain.model.League;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record LeagueResponse(
        String id,
        String name,
        String ownerId,
        String invitationCode,
        Instant invitationExpiresAt,
        Instant createdAt,
        List<MembershipResponse> members) {

    public static LeagueResponse from(League league) {
        Instant now = Instant.now();
        Optional<Invitation> validInv = league.getInvitations().stream()
                .filter(inv -> inv.isValid(now))
                .findFirst();
        List<MembershipResponse> members = league.getMemberships().stream()
                .map(MembershipResponse::from)
                .toList();
        return new LeagueResponse(
                league.getId(),
                league.getName(),
                league.getOwnerId(),
                validInv.map(Invitation::code).orElse(null),
                validInv.map(Invitation::expiresAt).orElse(null),
                league.getCreatedAt(),
                members);
    }
}
