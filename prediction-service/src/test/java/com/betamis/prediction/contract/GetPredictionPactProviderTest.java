package com.betamis.prediction.contract;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Consumer;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.betamis.prediction.domain.model.score.Score;
import com.betamis.prediction.domain.port.in.SubmitPrediction;
import com.betamis.prediction.domain.port.out.PredictionRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;
import java.time.Instant;

/**
 * Provider-side Pact contract test for GET /predictions (HTTP).
 *
 * <p>Reads {@code ../pacts/frontend-prediction-service.json} and verifies that
 * prediction-service satisfies the frontend consumer's expectations.
 * {@code @TestSecurity} makes the running test server accept all requests as
 * {@code user-pact} with the {@code betamis-user} role — matching the user
 * identity used in the pact state definitions.
 */
@QuarkusTest
@TestSecurity(user = "user-pact", roles = "betamis-user")
@Provider("prediction-service")
@Consumer("frontend")
@PactFolder("../pacts")
class GetPredictionPactProviderTest {

    @Inject
    SubmitPrediction submitPrediction;

    @Inject
    PredictionRepository predictionRepository;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        if (context != null) {
            context.setTarget(new HttpTestTarget("localhost", RestAssured.port));
        }
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("user user-pact has a prediction for match match-pact-1")
    void setupUserPrediction() {
        if (predictionRepository.findByUserIdAndMatchId("user-pact", "match-pact-1").isEmpty()) {
            submitPrediction.execute("match-pact-1", "user-pact",
                    new Score(2, 1), Instant.parse("2099-06-15T15:00:00Z"));
        }
    }

    @State("user user-pact has no prediction for match match-pact-none")
    void noUserPrediction() {
        // no data to insert — user has made no prediction for this match
    }
}
