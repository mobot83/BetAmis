package com.betamis.scoring.contract;

import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.V4Interaction;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.betamis.prediction.event.PredictionSubmitted;
import com.betamis.prediction.event.Score;
import com.betamis.scoring.domain.model.StoredPrediction;
import com.betamis.scoring.domain.port.in.StorePrediction;
import com.betamis.scoring.infrastructure.messaging.KafkaPredictionConsumer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Consumer-side Pact contract test.
 *
 * <p>Defines the message format that scoring-service expects to receive on the
 * {@code prediction.submitted} topic and verifies that {@link KafkaPredictionConsumer}
 * can correctly process a conforming event.  Running this test writes (or overwrites)
 * {@code ../pacts/scoring-service-prediction-service.json} via the {@code pact.rootDir}
 * system property configured in maven-surefire-plugin.
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "prediction-service", providerType = ProviderType.ASYNCH)
class PredictionSubmittedPactConsumerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Pact(consumer = "scoring-service")
    public V4Pact predictionSubmittedPact(PactBuilder builder) {
        PactDslJsonBody content = new PactDslJsonBody()
                .stringType("id", "event-uuid-1")
                .stringType("predictionId", "pred-uuid-1")
                .stringType("matchId", "match-uuid-1")
                .stringType("userId", "user-uuid-1")
                .integerType("occurredAt", 1710000000000L);

        // score is a nested object — configure it separately to keep types explicit
        content.object("score")
                .integerType("homeTeamScore", 2)
                .integerType("awayTeamScore", 1)
                .closeObject();

        return builder.usingLegacyMessageDsl()
                .expectsToReceive("a prediction submitted event")
                .withContent(content)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "predictionSubmittedPact")
    @DisplayName("scoring-service consumer can process a prediction.submitted event from prediction-service")
    void shouldProcessPredictionSubmittedEvent(V4Interaction.AsynchronousMessage message) throws Exception {
        // Pact gives us the message body as JSON bytes; convert to the Avro object
        // that SmallRye would normally deserialize from the Kafka topic.
        byte[] body = message.contentsAsBytes();
        JsonNode node = MAPPER.readTree(body);

        PredictionSubmitted event = PredictionSubmitted.newBuilder()
                .setId(node.get("id").asText())
                .setPredictionId(node.get("predictionId").asText())
                .setMatchId(node.get("matchId").asText())
                .setUserId(node.get("userId").asText())
                .setScore(Score.newBuilder()
                        .setHomeTeamScore(node.get("score").get("homeTeamScore").asInt())
                        .setAwayTeamScore(node.get("score").get("awayTeamScore").asInt())
                        .build())
                .setOccurredAt(Instant.ofEpochMilli(node.get("occurredAt").asLong()))
                .build();

        StorePrediction storePrediction = mock(StorePrediction.class);
        KafkaPredictionConsumer consumer = new KafkaPredictionConsumer(storePrediction);

        consumer.consume(event);

        ArgumentCaptor<StoredPrediction> captor = ArgumentCaptor.forClass(StoredPrediction.class);
        verify(storePrediction).store(captor.capture());
        StoredPrediction stored = captor.getValue();

        assertEquals(node.get("predictionId").asText(), stored.predictionId());
        assertEquals(node.get("matchId").asText(), stored.matchId());
        assertEquals(node.get("userId").asText(), stored.userId());
        assertEquals(node.get("score").get("homeTeamScore").asInt(), stored.predictedScore().homeTeamScore());
        assertEquals(node.get("score").get("awayTeamScore").asInt(), stored.predictedScore().awayTeamScore());
    }
}
