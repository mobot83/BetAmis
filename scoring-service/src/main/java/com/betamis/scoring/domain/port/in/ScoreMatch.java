package com.betamis.scoring.domain.port.in;

import com.betamis.scoring.domain.model.FinalScore;

public interface ScoreMatch {
    void score(String matchId, FinalScore finalScore);
}
