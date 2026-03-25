package com.betamis.league.application.usecase;

import com.betamis.league.domain.event.LeagueCreated;
import com.betamis.league.domain.model.League;
import com.betamis.league.domain.port.in.CreateLeague;
import com.betamis.league.domain.port.out.LeagueEventPublisher;
import com.betamis.league.domain.port.out.LeagueRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CreateLeagueUseCase implements CreateLeague {

    private final LeagueRepository leagueRepository;
    private final LeagueEventPublisher eventPublisher;
    private final Counter leaguesCreatedCounter;

    @Inject
    public CreateLeagueUseCase(LeagueRepository leagueRepository,
                               LeagueEventPublisher eventPublisher,
                               MeterRegistry registry) {
        this.leagueRepository = leagueRepository;
        this.eventPublisher = eventPublisher;
        this.leaguesCreatedCounter = registry.counter("betamis_leagues_created_total");
    }

    @Override
    @Transactional
    public League create(String name, String ownerId) {
        League league = League.create(name, ownerId);
        leagueRepository.save(league);

        league.pollDomainEvents().forEach(event -> {
            if (event instanceof LeagueCreated e) {
                eventPublisher.publish(e);
            }
        });

        leaguesCreatedCounter.increment();
        return league;
    }
}

