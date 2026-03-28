package com.betamis.notification.contract;

import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.V4Interaction;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.betamis.match.event.MatchScheduled;
import com.betamis.notification.application.usecase.ScheduleMatchNotificationsUseCase;
import com.betamis.notification.domain.port.out.ScheduledNotificationRepository;
import com.betamis.notification.infrastructure.messaging.KafkaMatchScheduledConsumer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Consumer-side Pact contract test.
 *
 * <p>Defines the message format that notification-service expects to receive on the
 * {@code match.scheduled} topic and verifies that {@link KafkaMatchScheduledConsumer}
 * correctly forwards the event to the scheduling use case. Running this test writes
 * {@code ../pacts/notification-service-match-service.json} via the {@code pact.rootDir}
 * system property configured in maven-surefire-plugin.
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "match-service", providerType = ProviderType.ASYNCH)
class MatchScheduledPactConsumerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Pact(consumer = "notification-service")
    public V4Pact matchScheduledPact(PactBuilder builder) {
        PactDslJsonBody content = new PactDslJsonBody()
                .stringType("id", "event-uuid-1")
                .stringType("matchId", "match-uuid-1")
                .stringType("homeTeamId", "home-team-uuid-1")
                .stringType("awayTeamId", "away-team-uuid-1")
                .integerType("kickoffAt", 1710000000000L)
                .integerType("occurredAt", 1709990000000L);

        return builder.usingLegacyMessageDsl()
                .expectsToReceive("a match scheduled event")
                .withContent(content)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "matchScheduledPact")
    @DisplayName("notification-service consumer can process a match.scheduled event from match-service")
    void shouldProcessMatchScheduledEvent(V4Interaction.AsynchronousMessage message) throws Exception {
        byte[] body = message.contentsAsBytes();
        JsonNode node = MAPPER.readTree(body);

        MatchScheduled event = MatchScheduled.newBuilder()
                .setId(node.get("id").asText())
                .setMatchId(node.get("matchId").asText())
                .setHomeTeamId(node.get("homeTeamId").asText())
                .setAwayTeamId(node.get("awayTeamId").asText())
                .setKickoffAt(Instant.ofEpochMilli(node.get("kickoffAt").asLong()))
                .setOccurredAt(Instant.ofEpochMilli(node.get("occurredAt").asLong()))
                .build();

        ScheduledNotificationRepository repo = mock(ScheduledNotificationRepository.class);
        var scheduleUseCase = new ScheduleMatchNotificationsUseCase(repo);
        KafkaMatchScheduledConsumer consumer = new KafkaMatchScheduledConsumer(scheduleUseCase);

        consumer.consume(event);

        // use case checks deduplication once per reminder (J-1 and H-1) = 2 invocations
        verify(repo, times(2)).existsByMatchIdAndNotifyAt(eq(node.get("matchId").asText()), any(Instant.class));
    }
}
