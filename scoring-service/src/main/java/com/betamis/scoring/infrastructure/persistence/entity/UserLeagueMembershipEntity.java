package com.betamis.scoring.infrastructure.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "user_league_memberships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "league_id"}))
public class UserLeagueMembershipEntity extends PanacheEntity {

    @Column(name = "user_id", nullable = false, length = 255)
    public String userId;

    @Column(name = "league_id", nullable = false, length = 255)
    public String leagueId;

    @Column(name = "joined_at", nullable = false)
    public Instant joinedAt;
}
