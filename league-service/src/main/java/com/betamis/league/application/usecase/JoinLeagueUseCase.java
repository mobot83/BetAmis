package com.betamis.league.application.usecase;

import com.betamis.league.domain.event.MemberJoined;
import com.betamis.league.domain.exception.LeagueNotFoundException;
import com.betamis.league.domain.model.League;
import com.betamis.league.domain.port.in.JoinLeague;
import com.betamis.league.domain.port.out.LeagueEventPublisher;
import com.betamis.league.domain.port.out.LeagueRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class JoinLeagueUseCase implements JoinLeague {

    private final LeagueRepository leagueRepository;
    private final LeagueEventPublisher eventPublisher;

    public JoinLeagueUseCase(LeagueRepository leagueRepository,
                             LeagueEventPublisher eventPublisher) {
        this.leagueRepository = leagueRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void join(String leagueId, String userId, String invitationCode) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException(leagueId));

        league.join(userId, invitationCode);
        leagueRepository.save(league);

        league.pollDomainEvents().forEach(event -> {
            if (event instanceof MemberJoined e) {
                eventPublisher.publish(e);
            }
        });
    }
}

