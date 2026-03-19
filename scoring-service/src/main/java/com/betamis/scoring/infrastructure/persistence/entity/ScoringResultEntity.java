package com.betamis.scoring.infrastructure.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "scoring_results")
public class ScoringResultEntity extends PanacheEntity {

    @Column(name = "prediction_id", nullable = false, unique = true)
    public String predictionId;

    @Column(name = "match_id", nullable = false)
    public String matchId;

    @Column(name = "user_id", nullable = false)
    public String userId;

    @Column(name = "points", nullable = false)
    public int points;
}
