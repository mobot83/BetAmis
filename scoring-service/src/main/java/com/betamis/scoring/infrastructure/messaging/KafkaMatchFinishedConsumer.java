package com.betamis.scoring.infrastructure.messaging;

import com.betamis.match.event.MatchFinished;
import com.betamis.scoring.domain.model.FinalScore;
import com.betamis.scoring.domain.port.in.ScoreMatch;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class KafkaMatchFinishedConsumer {

    private final ScoreMatch scoreMatch;

    @Inject
    public KafkaMatchFinishedConsumer(ScoreMatch scoreMatch) {
        this.scoreMatch = scoreMatch;
    }

    @Incoming("match-finished")
    public void consume(MatchFinished event) {
        Log.infof("Received MatchFinished for match %s", event.getMatchId());
        try {
            FinalScore finalScore = new FinalScore(
                    event.getFinalScore().getHomeTeamScore(),
                    event.getFinalScore().getAwayTeamScore()
            );
            scoreMatch.score(event.getMatchId(), finalScore);
        } catch (Exception e) {
            Log.errorf(e, "Failed to score match %s — skipping", event.getMatchId());
        }
    }
}
