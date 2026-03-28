package com.betamis.prediction.infrastructure.sse;

import com.betamis.prediction.domain.model.ranking.RankingEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankingBroadcasterTest {

    private RankingBroadcaster broadcaster;

    @BeforeEach
    void setUp() {
        broadcaster = new RankingBroadcaster();
    }

    @Test
    @DisplayName("Events published before any subscription are not delivered")
    void publishBeforeSubscription_notReceived() {
        broadcaster.publish("league-1", new RankingEntry(1, "user-a", 10L));

        List<RankingEntry> received = new ArrayList<>();
        broadcaster.stream("league-1").subscribe().with(received::add);

        assertTrue(received.isEmpty());
    }

    @Test
    @DisplayName("Events published after subscription are delivered to the subscriber")
    void publishAfterSubscription_received() {
        List<RankingEntry> received = new ArrayList<>();
        broadcaster.stream("league-1").subscribe().with(received::add);

        broadcaster.publish("league-1", new RankingEntry(1, "user-a", 10L));

        assertEquals(1, received.size());
        assertEquals("user-a", received.get(0).userId());
        assertEquals(10L, received.get(0).totalPoints());
    }

    @Test
    @DisplayName("Each subscriber only receives events for its own league")
    void eventsAreIsolatedByLeague() {
        List<RankingEntry> league1 = new ArrayList<>();
        List<RankingEntry> league2 = new ArrayList<>();

        broadcaster.stream("league-1").subscribe().with(league1::add);
        broadcaster.stream("league-2").subscribe().with(league2::add);

        broadcaster.publish("league-1", new RankingEntry(1, "user-a", 10L));

        assertEquals(1, league1.size());
        assertTrue(league2.isEmpty());
    }

    @Test
    @DisplayName("Multiple subscribers on the same league all receive the same event")
    void multipleSubscribers_allReceive() {
        List<RankingEntry> sub1 = new ArrayList<>();
        List<RankingEntry> sub2 = new ArrayList<>();

        broadcaster.stream("league-1").subscribe().with(sub1::add);
        broadcaster.stream("league-1").subscribe().with(sub2::add);

        broadcaster.publish("league-1", new RankingEntry(1, "user-a", 10L));

        assertEquals(1, sub1.size());
        assertEquals(1, sub2.size());
        assertEquals("user-a", sub1.get(0).userId());
        assertEquals("user-a", sub2.get(0).userId());
    }

    @Test
    @DisplayName("Publishing to a league with no subscribers does nothing")
    void publishWithNoSubscribers_noError() {
        broadcaster.publish("no-subscribers", new RankingEntry(1, "user-x", 5L));
        // No exception should be thrown
    }
}
