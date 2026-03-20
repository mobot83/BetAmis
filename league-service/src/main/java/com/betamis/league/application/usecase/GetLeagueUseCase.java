package com.betamis.league.application.usecase;

import com.betamis.league.domain.exception.LeagueNotFoundException;
import com.betamis.league.domain.model.League;
import com.betamis.league.domain.port.in.GetLeague;
import com.betamis.league.domain.port.out.LeagueRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GetLeagueUseCase implements GetLeague {

    private final LeagueRepository leagueRepository;

    @Inject
    public GetLeagueUseCase(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    @Override
    public League get(String leagueId) {
        return leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException(leagueId));
    }
}
