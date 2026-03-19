package com.betamis.league.infrastructure.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "leagues")
public class LeagueEntity extends PanacheEntity {

    @Column(name = "league_id", unique = true, nullable = false)
    public String leagueId;

    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "owner_id", nullable = false)
    public String ownerId;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<MembershipEntity> memberships = new ArrayList<>();

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<InvitationEntity> invitations = new ArrayList<>();
}

