package com.betamis.scoring.infrastructure.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "stored_predictions")
public class StoredPredictionEntity extends PanacheEntity {

    @Column(name = "prediction_id", nullable = false, unique = true)
    public String predictionId;

    @Column(name = "match_id", nullable = false)
    public String matchId;

    @Column(name = "user_id", nullable = false)
    public String userId;

    @Column(name = "predicted_home_score", nullable = false)
    public int predictedHomeScore;

    @Column(name = "predicted_away_score", nullable = false)
    public int predictedAwayScore;
}
