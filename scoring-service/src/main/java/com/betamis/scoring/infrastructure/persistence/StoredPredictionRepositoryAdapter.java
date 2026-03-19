package com.betamis.scoring.infrastructure.persistence;

import com.betamis.scoring.domain.model.FinalScore;
import com.betamis.scoring.domain.model.StoredPrediction;
import com.betamis.scoring.domain.port.out.StoredPredictionRepository;
import com.betamis.scoring.infrastructure.persistence.entity.StoredPredictionEntity;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class StoredPredictionRepositoryAdapter implements StoredPredictionRepository {

    @Override
    @Transactional
    public void save(StoredPrediction prediction) {
        if (StoredPredictionEntity.count("predictionId", prediction.predictionId()) > 0) {
            Log.infof("Prediction %s already stored, skipping", prediction.predictionId());
            return;
        }
        StoredPredictionEntity entity = new StoredPredictionEntity();
        entity.predictionId = prediction.predictionId();
        entity.matchId = prediction.matchId();
        entity.userId = prediction.userId();
        entity.predictedHomeScore = prediction.predictedScore().homeTeamScore();
        entity.predictedAwayScore = prediction.predictedScore().awayTeamScore();
        entity.persist();
    }

    @Override
    public List<StoredPrediction> findByMatchId(String matchId) {
        return StoredPredictionEntity.<StoredPredictionEntity>find("matchId", matchId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private StoredPrediction toDomain(StoredPredictionEntity entity) {
        return new StoredPrediction(
                entity.predictionId,
                entity.matchId,
                entity.userId,
                new FinalScore(entity.predictedHomeScore, entity.predictedAwayScore)
        );
    }
}
