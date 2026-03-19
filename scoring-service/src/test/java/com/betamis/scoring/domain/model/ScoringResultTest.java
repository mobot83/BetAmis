package com.betamis.scoring.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoringResultTest {

    private static final String PREDICTION_ID = "pred-1";
    private static final String MATCH_ID = "match-1";
    private static final String USER_ID = "user-1";

    @ParameterizedTest(name = "predicted={0}-{1}, actual={2}-{3} => {4}pts")
    @DisplayName("Should award correct points based on prediction vs actual")
    @CsvSource({
            // exact score
            "2, 1, 2, 1, 3",
            "0, 0, 0, 0, 3",
            // correct winner + correct goal difference (not exact score)
            "3, 1, 4, 2, 2",
            "1, 3, 0, 2, 2",
            // correct draw (not exact score) - same outcome but different diff
            "1, 1, 2, 2, 1",
            // correct winner only
            "2, 0, 1, 0, 1",
            "0, 2, 0, 3, 1",
            // wrong
            "2, 1, 0, 1, 0",
            "1, 0, 0, 1, 0",
    })
    void scoring(int predHome, int predAway, int actualHome, int actualAway, int expectedPoints) {
        StoredPrediction prediction = new StoredPrediction(
                PREDICTION_ID, MATCH_ID, USER_ID,
                new FinalScore(predHome, predAway)
        );
        FinalScore actual = new FinalScore(actualHome, actualAway);

        ScoringResult result = ScoringResult.calculate(prediction, actual);

        assertEquals(expectedPoints, result.points());
        assertEquals(PREDICTION_ID, result.predictionId());
        assertEquals(MATCH_ID, result.matchId());
        assertEquals(USER_ID, result.userId());
    }
}
