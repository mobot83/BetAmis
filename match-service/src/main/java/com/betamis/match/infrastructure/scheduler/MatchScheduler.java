package com.betamis.match.infrastructure.scheduler;

import com.betamis.match.domain.port.in.SyncMatches;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@ApplicationScoped
public class MatchScheduler {

    private final SyncMatches syncMatches;
    private final List<String> competitionIds;

    public MatchScheduler(
            SyncMatches syncMatches,
            @ConfigProperty(name = "match.sync.competition-ids") List<String> competitionIds
    ) {
        this.syncMatches = syncMatches;
        this.competitionIds = competitionIds;
    }

    @Scheduled(cron = "{match.sync.cron}")
    void sync() {
        competitionIds.forEach(syncMatches::syncByCompetition);
    }
}
