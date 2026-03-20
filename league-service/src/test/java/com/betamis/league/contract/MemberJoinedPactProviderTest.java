package com.betamis.league.contract;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.betamis.league.event.MemberJoined;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
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
     * Serialisation is delegated to Avro's built-in {@code toString()} to avoid
     * manual string formatting and the JSON-injection risk it carries.
     */
    @PactVerifyProvider("a member joined event")
    String produceMemberJoinedEvent() {
        MemberJoined event = MemberJoined.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setLeagueId(UUID.randomUUID().toString())
                .setUserId(UUID.randomUUID().toString())
                .setOccurredAt(Instant.now())
                .build();

        return event.toString();
    }
}
