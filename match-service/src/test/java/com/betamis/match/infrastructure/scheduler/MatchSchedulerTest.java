package com.betamis.match.infrastructure.scheduler;

import com.betamis.match.domain.port.in.SyncMatches;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class MatchSchedulerTest {

    @Mock
    SyncMatches syncMatches;

    @Test
    @DisplayName("Should call syncByCompetition for each configured competition id")
    void shouldSyncEachCompetition() {
        var scheduler = new MatchScheduler(syncMatches, List.of("PL", "FL1", "BL1"));

        scheduler.sync();

        verify(syncMatches).syncByCompetition("PL");
        verify(syncMatches).syncByCompetition("FL1");
        verify(syncMatches).syncByCompetition("BL1");
        verifyNoMoreInteractions(syncMatches);
    }

    @Test
    @DisplayName("Should not call sync when competition list is empty")
    void shouldNotSyncWhenNoCompetitionsConfigured() {
        var scheduler = new MatchScheduler(syncMatches, List.of());

        scheduler.sync();

        verifyNoMoreInteractions(syncMatches);
    }
}
