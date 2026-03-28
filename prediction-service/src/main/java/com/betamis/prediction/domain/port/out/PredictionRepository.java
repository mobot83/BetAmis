package com.betamis.prediction.domain.port.out;

import com.betamis.prediction.domain.model.prediction.Prediction;

import java.util.List;
import java.util.Optional;

public interface PredictionRepository {
    Prediction findById(String id);
    Optional<Prediction> findByUserIdAndMatchId(String userId, String matchId);
    List<Prediction> findByMatchId(String matchId);
    void save(Prediction prediction);
    void update(Prediction prediction);
    boolean existsByUserIdAndMatchId(String userId, String matchId);
}
