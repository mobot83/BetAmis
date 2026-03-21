package com.betamis.scoring.domain.port.out;

import com.betamis.scoring.domain.model.ScoringResult;
import com.betamis.scoring.domain.model.UserRanking;

public interface ScoringEventPublisher {
    void publishPointsCalculated(ScoringResult result);
    void publishRankingUpdated(UserRanking ranking);
}
