package com.betamis.scoring.domain.port.in;

import com.betamis.scoring.domain.model.StoredPrediction;

public interface StorePrediction {
    void store(StoredPrediction prediction);
}
