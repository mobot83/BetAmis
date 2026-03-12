package com.betamis.match.infrastructure.persistence.entity;

import com.betamis.match.domain.model.match.MatchStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "matches")
public class MatchEntity extends PanacheEntity {

    @Column(name = "match_id", unique = true, nullable = false)
    public String matchId;

    @Column(name = "external_id", unique = true)
    public Long externalId;

    @Column(name = "home_team_id", nullable = false)
    public String homeTeamId;

    @Column(name = "away_team_id", nullable = false)
    public String awayTeamId;

    @Column(name = "home_team_score", nullable = false)
    public int homeTeamScore;

    @Column(name = "away_team_score", nullable = false)
    public int awayTeamScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public MatchStatus status;
}
