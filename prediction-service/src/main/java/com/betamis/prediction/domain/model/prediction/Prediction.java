package com.betamis.prediction.domain.model.prediction;

import com.betamis.prediction.domain.event.PredictionSubmitted;
import com.betamis.prediction.domain.exception.KickOffAlreadyPassedException;
import com.betamis.prediction.domain.exception.MatchAlreadyStartedException;
import com.betamis.prediction.domain.exception.PredictionAlreadyClosedException;
import com.betamis.prediction.domain.model.score.Score;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Prediction {
    private String id;
    private String matchId;
    private String userId;
    private Score score;
    private PredictionStatus status;
    private Instant submittedAt;
    private final List<Object> domainEvents = new ArrayList<>();

    public Prediction(String id, String matchId, String userId, Score score, PredictionStatus status, Instant submittedAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Prediction id cannot be null or blank");
        }
        if (matchId == null || matchId.isBlank()) {
            throw new IllegalArgumentException("Match id cannot be null or blank");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User id cannot be null or blank");
        }
        if (score == null) {
            throw new IllegalArgumentException("Score cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Prediction status cannot be null");
        }
        if (submittedAt == null) {
            throw new IllegalArgumentException("Submitted At cannot be null");
        }
        this.id = id;
        this.matchId = matchId;
        this.userId = userId;
        this.score = score;
        this.status = status;
        this.submittedAt = submittedAt;
    }

    public Prediction() {

    }

    public String getId() {
        return id;
    }

    public String getMatchId() {
        return matchId;
    }

    public String getUserId() {
        return userId;
    }

    public Score getScore() {
        return score;
    }

    public PredictionStatus getStatus() {
        return status;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Prediction that = (Prediction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public void close() {
        if (this.status == PredictionStatus.CLOSED) {
            throw new PredictionAlreadyClosedException(
                    "Prediction %s is already closed".formatted(id));
        }
        this.status = PredictionStatus.CLOSED;
    }

    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    public void update(Score newScore, Instant kickoffAt, Instant now) {
        if (this.status == PredictionStatus.CLOSED) {
            throw new MatchAlreadyStartedException(
                    "Cannot update prediction %s: match has already started".formatted(id));
        }
        if (!now.isBefore(kickoffAt)) {
            throw new MatchAlreadyStartedException(
                    "Cannot update prediction %s: kick-off time has passed".formatted(id));
        }
        this.score = newScore;
    }

    public static Prediction submit(String matchId, String userId, Score score, Instant kickOffTime) {
        if (!Instant.now().isBefore(kickOffTime)) {
            throw new KickOffAlreadyPassedException("Cannot submit a prediction after kick-off");
        }
        String id = UUID.randomUUID().toString();
        PredictionStatus status = PredictionStatus.SUBMITTED;
        Instant submittedAt = Instant.now();
        var prediction = new Prediction(id, matchId, userId, score, status, submittedAt);
        prediction.domainEvents.add(PredictionSubmitted.of(prediction));
        return prediction;
    }
}
