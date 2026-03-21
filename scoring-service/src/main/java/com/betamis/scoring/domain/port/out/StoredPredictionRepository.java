package com.betamis.scoring.domain.port.out;

import com.betamis.scoring.domain.model.StoredPrediction;

import java.util.List;

public interface StoredPredictionRepository {
    void save(StoredPrediction prediction);
    List<StoredPrediction> findByMatchId(String matchId);
}
