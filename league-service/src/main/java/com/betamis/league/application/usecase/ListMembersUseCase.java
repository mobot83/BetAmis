package com.betamis.league.application.usecase;

import com.betamis.league.domain.exception.LeagueNotFoundException;
import com.betamis.league.domain.model.Membership;
import com.betamis.league.domain.port.in.ListMembers;
import com.betamis.league.domain.port.out.LeagueRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class ListMembersUseCase implements ListMembers {

    private final LeagueRepository leagueRepository;

    @Inject
    public ListMembersUseCase(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    @Override
    public List<Membership> list(String leagueId) {
        return leagueRepository.findMembersByLeagueId(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException(leagueId));
    }
}

