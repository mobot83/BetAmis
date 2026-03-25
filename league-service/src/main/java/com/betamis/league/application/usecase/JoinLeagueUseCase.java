package com.betamis.league.application.usecase;

import com.betamis.league.domain.event.MemberJoined;
import com.betamis.league.domain.exception.LeagueNotFoundException;
import com.betamis.league.domain.model.League;
import com.betamis.league.domain.port.in.JoinLeague;
import com.betamis.league.domain.port.out.LeagueEventPublisher;
import com.betamis.league.domain.port.out.LeagueRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;

@ApplicationScoped
public class JoinLeagueUseCase implements JoinLeague {

    private final LeagueRepository leagueRepository;
    private final LeagueEventPublisher eventPublisher;
    private final Counter leagueJoinsCounter;

    @Inject
    public JoinLeagueUseCase(LeagueRepository leagueRepository,
                             LeagueEventPublisher eventPublisher,
                             MeterRegistry registry) {
        this.leagueRepository = leagueRepository;
        this.eventPublisher = eventPublisher;
        this.leagueJoinsCounter = registry.counter("betamis_league_joins_total");
    }

    @Override
    @Transactional
    public void join(String leagueId, String userId, String invitationCode) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException(leagueId));

        Instant now = Instant.now();
        league.join(userId, invitationCode, now);
        leagueRepository.save(league);

        league.pollDomainEvents().forEach(event -> {
            if (event instanceof MemberJoined e) {
                eventPublisher.publish(e);
            }
        });

        leagueJoinsCounter.increment();
    }
}

