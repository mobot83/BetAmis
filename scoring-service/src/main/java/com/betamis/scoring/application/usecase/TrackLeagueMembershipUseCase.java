package com.betamis.scoring.application.usecase;

import com.betamis.scoring.domain.port.in.TrackLeagueMembership;
import com.betamis.scoring.domain.port.out.LeagueMembershipRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TrackLeagueMembershipUseCase implements TrackLeagueMembership {

    private final LeagueMembershipRepository leagueMembershipRepository;

    @Inject
    public TrackLeagueMembershipUseCase(LeagueMembershipRepository leagueMembershipRepository) {
        this.leagueMembershipRepository = leagueMembershipRepository;
    }

    @Override
    public void track(String userId, String leagueId) {
        if (!leagueMembershipRepository.exists(userId, leagueId)) {
            leagueMembershipRepository.save(userId, leagueId);
        }
    }
}
