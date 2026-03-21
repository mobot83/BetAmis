package com.betamis.prediction.domain.port.out;

import com.betamis.prediction.domain.event.PredictionClosed;
import com.betamis.prediction.domain.event.PredictionSubmitted;

public interface EventPublisher {
    void publish(PredictionSubmitted event);
    void publish(PredictionClosed event);
}
