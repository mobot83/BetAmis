package com.betamis.scoring.domain.port.out;

import com.betamis.scoring.domain.model.ScoringResult;

public interface ScoringResultRepository {
    /**
     * Persist the scoring result. Implementations must be idempotent:
     * a second call with the same predictionId must be a no-op.
     */
    void save(ScoringResult result);

    boolean existsByPredictionId(String predictionId);
}
