package com.betamis.scoring.infrastructure.persistence;

import com.betamis.scoring.domain.port.out.LeagueMembershipRepository;
import com.betamis.scoring.infrastructure.persistence.entity.UserLeagueMembershipEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class LeagueMembershipRepositoryAdapter implements LeagueMembershipRepository {

    @Override
    @Transactional
    public void save(String userId, String leagueId) {
        UserLeagueMembershipEntity entity = new UserLeagueMembershipEntity();
        entity.userId = userId;
        entity.leagueId = leagueId;
        entity.joinedAt = Instant.now();
        entity.persist();
    }

    @Override
    public boolean exists(String userId, String leagueId) {
        return UserLeagueMembershipEntity.count("userId = ?1 and leagueId = ?2", userId, leagueId) > 0;
    }

    @Override
    public List<String> findLeagueIdsByUserId(String userId) {
        return UserLeagueMembershipEntity.<UserLeagueMembershipEntity>find("userId", userId)
                .stream()
                .map(e -> e.leagueId)
                .toList();
    }
}
