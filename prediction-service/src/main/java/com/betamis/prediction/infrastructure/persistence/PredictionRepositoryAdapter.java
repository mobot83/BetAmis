package com.betamis.prediction.infrastructure.persistence;

import com.betamis.prediction.domain.model.prediction.Prediction;
import com.betamis.prediction.domain.model.prediction.PredictionStatus;
import com.betamis.prediction.domain.model.score.Score;
import com.betamis.prediction.domain.port.out.PredictionRepository;
import com.betamis.prediction.infrastructure.persistence.entity.PredictionEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PredictionRepositoryAdapter implements PredictionRepository {

    @Override
    @Transactional
    public void save(Prediction prediction) {
        PredictionEntity entity = new PredictionEntity();
        entity.predictionId = prediction.getId();
        entity.matchId = prediction.getMatchId();
        entity.userId = prediction.getUserId();
        entity.homeScore = prediction.getScore().homeTeamScore();
        entity.awayScore = prediction.getScore().awayTeamScore();
        entity.status = prediction.getStatus().name();
        entity.submittedAt = prediction.getSubmittedAt();
        entity.persist();
    }

    @Override
    @Transactional
    public Prediction findById(String id) {
        return PredictionEntity.<PredictionEntity>find("predictionId", id)
                .firstResultOptional()
                .map(this::toDomain)
                .orElse(null);
    }

    @Override
    @Transactional
    public List<Prediction> findByMatchId(String matchId) {
        return PredictionEntity.<PredictionEntity>find("matchId", matchId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void update(Prediction prediction) {
        PredictionEntity.<PredictionEntity>find("predictionId", prediction.getId())
                .firstResultOptional()
                .ifPresent(entity -> entity.status = prediction.getStatus().name());
    }

    @Override
    public boolean existsByUserIdAndMatchId(String userId, String matchId) {
        return PredictionEntity.count("userId = ?1 and matchId = ?2", userId, matchId) > 0;
    }

    private Prediction toDomain(PredictionEntity entity) {
        return new Prediction(
                entity.predictionId,
                entity.matchId,
                entity.userId,
                new Score(entity.homeScore, entity.awayScore),
                PredictionStatus.valueOf(entity.status),
                entity.submittedAt
        );
    }
}
