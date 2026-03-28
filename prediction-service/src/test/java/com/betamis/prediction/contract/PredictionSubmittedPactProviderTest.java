package com.betamis.prediction.contract;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Consumer;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
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
@Consumer("scoring-service")
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
     * Fields are serialised via Jackson with {@code occurredAt} as epoch milliseconds (long),
     * matching the {@code timestamp-millis} Avro logical type expected by the consumer.
     */
    @PactVerifyProvider("a prediction submitted event")
    String producePredictionSubmittedEvent() throws JsonProcessingException {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", UUID.randomUUID().toString());
        event.put("predictionId", UUID.randomUUID().toString());
        event.put("matchId", UUID.randomUUID().toString());
        event.put("userId", UUID.randomUUID().toString());
        event.put("score", Map.of("homeTeamScore", 2, "awayTeamScore", 1));
        event.put("occurredAt", Instant.now().toEpochMilli());
        return new ObjectMapper().writeValueAsString(event);
    }
}
