package com.betamis.prediction.infrastructure.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "predictions")
public class PredictionEntity extends PanacheEntity {

    @Column(name = "prediction_id", nullable = false, unique = true, length = 36)
    public String predictionId;

    @Column(name = "match_id", nullable = false, length = 36)
    public String matchId;

    @Column(name = "user_id", nullable = false, length = 255)
    public String userId;

    @Column(name = "home_score", nullable = false)
    public int homeScore;

    @Column(name = "away_score", nullable = false)
    public int awayScore;

    @Column(name = "status", nullable = false, length = 20)
    public String status;

    @Column(name = "submitted_at", nullable = false)
    public Instant submittedAt;
}
