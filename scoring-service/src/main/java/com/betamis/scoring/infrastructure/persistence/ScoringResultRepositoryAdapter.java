package com.betamis.scoring.infrastructure.persistence;

import com.betamis.scoring.domain.model.ScoringResult;
import com.betamis.scoring.domain.port.out.ScoringResultRepository;
import com.betamis.scoring.infrastructure.persistence.entity.ScoringResultEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ScoringResultRepositoryAdapter implements ScoringResultRepository {

    @Override
    @Transactional
    public void save(ScoringResult result) {
        ScoringResultEntity entity = new ScoringResultEntity();
        entity.predictionId = result.predictionId();
        entity.matchId = result.matchId();
        entity.userId = result.userId();
        entity.points = result.points();
        entity.persist();
    }

    @Override
    public boolean existsByPredictionId(String predictionId) {
        return ScoringResultEntity.count("predictionId", predictionId) > 0;
    }
}
