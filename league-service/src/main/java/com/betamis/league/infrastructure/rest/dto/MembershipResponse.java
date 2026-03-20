package com.betamis.league.infrastructure.rest.dto;

import com.betamis.league.domain.model.Membership;

import java.time.Instant;

public record MembershipResponse(String userId, Instant joinedAt) {
    public static MembershipResponse from(Membership m) {
        return new MembershipResponse(m.userId(), m.joinedAt());
    }
}

