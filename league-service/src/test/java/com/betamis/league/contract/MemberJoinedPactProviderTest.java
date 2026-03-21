package com.betamis.league.contract;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
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
 * <p>Reads {@code ../pacts/scoring-service-league-service.json} (committed to the
 * repository) and verifies that league-service produces a {@code league.member-joined}
 * message whose structure satisfies every matching rule defined by the scoring-service
 * consumer.  The build fails if the provider breaks the contract.
 */
@Provider("league-service")
@PactFolder("../pacts")
class MemberJoinedPactProviderTest {

    @BeforeEach
    void setUp(PactVerificationContext context) {
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
     * Produces a sample {@code league.member-joined} message as league-service would.
     * The returned JSON is compared against the consumer contract's matching rules.
     * Fields are serialised via Jackson with {@code occurredAt} as epoch milliseconds (long),
     * matching the {@code timestamp-millis} Avro logical type expected by the consumer.
     */
    @PactVerifyProvider("a member joined event")
    String produceMemberJoinedEvent() throws JsonProcessingException {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", UUID.randomUUID().toString());
        event.put("leagueId", UUID.randomUUID().toString());
        event.put("userId", UUID.randomUUID().toString());
        event.put("occurredAt", Instant.now().toEpochMilli());
        return new ObjectMapper().writeValueAsString(event);
    }
}
