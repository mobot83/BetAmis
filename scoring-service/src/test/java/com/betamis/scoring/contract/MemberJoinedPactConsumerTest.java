package com.betamis.scoring.contract;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;
import com.betamis.league.event.MemberJoined;
import com.betamis.scoring.domain.port.in.TrackLeagueMembership;
import com.betamis.scoring.infrastructure.messaging.KafkaLeagueMemberConsumer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Consumer-side Pact contract test.
 *
 * <p>Defines the message format that scoring-service expects to receive on the
 * {@code league.member-joined} topic and verifies that {@link KafkaLeagueMemberConsumer}
 * can correctly process a conforming event.  Running this test writes (or overwrites)
 * {@code ../pacts/scoring-service-league-service.json} via the {@code pact.rootDir}
 * system property configured in maven-surefire-plugin.
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "league-service", providerType = ProviderType.ASYNCH)
class MemberJoinedPactConsumerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Pact(consumer = "scoring-service")
    MessagePact memberJoinedPact(MessagePactBuilder builder) {
        PactDslJsonBody content = new PactDslJsonBody()
                .stringType("id", "event-uuid-1")
                .stringType("leagueId", "league-uuid-1")
                .stringType("userId", "user-uuid-1")
                .integerType("occurredAt", 1710000000000L);

        return builder
                .expectsToReceive("a member joined event")
                .withContent(content)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "memberJoinedPact")
    @DisplayName("scoring-service consumer can process a league.member-joined event from league-service")
    void shouldProcessMemberJoinedEvent(List<Message> messages) throws Exception {
        byte[] body = messages.get(0).contentsAsBytes();
        JsonNode node = MAPPER.readTree(body);

        MemberJoined event = MemberJoined.newBuilder()
                .setId(node.get("id").asText())
                .setLeagueId(node.get("leagueId").asText())
                .setUserId(node.get("userId").asText())
                .setOccurredAt(Instant.ofEpochMilli(node.get("occurredAt").asLong()))
                .build();

        TrackLeagueMembership trackLeagueMembership = mock(TrackLeagueMembership.class);
        KafkaLeagueMemberConsumer consumer = new KafkaLeagueMemberConsumer(trackLeagueMembership);

        consumer.consume(event);

        verify(trackLeagueMembership).track(
                node.get("userId").asText(),
                node.get("leagueId").asText());
    }
}
