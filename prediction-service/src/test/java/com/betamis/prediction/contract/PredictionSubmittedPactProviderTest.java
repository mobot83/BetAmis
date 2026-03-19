package com.betamis.prediction.contract;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.betamis.prediction.event.PredictionSubmitted;
import com.betamis.prediction.event.Score;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.UUID;

/**
 * Provider-side Pact contract test.
 *
 * <p>Reads {@code ../pacts/scoring-service-prediction-service.json} (committed to the
 * repository) and verifies that prediction-service produces a {@code prediction.submitted}
 * message whose structure satisfies every matching rule defined by the scoring-service
 * consumer.  The build fails if the provider breaks the contract.
 */
@Provider("prediction-service")
@PactFolder("../pacts")
class PredictionSubmittedPactProviderTest {

    @BeforeEach
    void setUp(PactVerificationContext context) {
        // context is null when the pact file cannot be loaded (e.g. file not found);
        // guard here so the root cause surfaces instead of a NullPointerException.
        if (context != null) {
            context.setTarget(new MessageTestTarget());
        }
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    /**
     * Produces a sample {@code prediction.submitted} message as prediction-service would.
     * The returned JSON is compared against the consumer contract's matching rules.
     * Serialisation is delegated to Avro's built-in {@code toString()} to avoid
     * manual string formatting and the JSON-injection risk it carries.
     */
    @PactVerifyProvider("a prediction submitted event")
    String producePredictionSubmittedEvent() {
        PredictionSubmitted event = PredictionSubmitted.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setPredictionId(UUID.randomUUID().toString())
                .setMatchId(UUID.randomUUID().toString())
                .setUserId(UUID.randomUUID().toString())
                .setScore(Score.newBuilder()
                        .setHomeTeamScore(2)
                        .setAwayTeamScore(1)
                        .build())
                .setOccurredAt(Instant.now())
                .build();

        return event.toString();
    }
}
