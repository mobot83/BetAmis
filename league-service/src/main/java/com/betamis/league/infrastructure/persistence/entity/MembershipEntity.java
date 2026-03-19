package com.betamis.league.infrastructure.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "memberships")
public class MembershipEntity extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_entity_id", nullable = false)
    public LeagueEntity league;

    @Column(name = "user_id", nullable = false)
    public String userId;

    @Column(name = "joined_at", nullable = false)
    public Instant joinedAt;
}

