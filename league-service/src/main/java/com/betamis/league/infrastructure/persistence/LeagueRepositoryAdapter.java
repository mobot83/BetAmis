package com.betamis.league.infrastructure.persistence;

import com.betamis.league.domain.model.Invitation;
import com.betamis.league.domain.model.League;
import com.betamis.league.domain.model.Membership;
import com.betamis.league.domain.port.out.LeagueRepository;
import com.betamis.league.infrastructure.persistence.entity.InvitationEntity;
import com.betamis.league.infrastructure.persistence.entity.LeagueEntity;
import com.betamis.league.infrastructure.persistence.entity.MembershipEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class LeagueRepositoryAdapter implements LeagueRepository {

    @Override
    @Transactional
    public void save(League league) {
        LeagueEntity entity = LeagueEntity.<LeagueEntity>find("leagueId", league.getId())
                .firstResultOptional()
                .orElse(new LeagueEntity());

        entity.leagueId = league.getId();
        entity.name = league.getName();
        entity.ownerId = league.getOwnerId();
        entity.createdAt = league.getCreatedAt();

        // Sync memberships — only ADD new ones (members are never removed in this domain)
        Set<String> existingUserIds = entity.memberships.stream()
                .map(m -> m.userId)
                .collect(Collectors.toSet());
        for (Membership m : league.getMemberships()) {
            if (!existingUserIds.contains(m.userId())) {
                MembershipEntity me = new MembershipEntity();
                me.league = entity;
                me.userId = m.userId();
                me.joinedAt = m.joinedAt();
                entity.memberships.add(me);
            }
        }

        // Sync invitations — only ADD new ones
        Set<String> existingCodes = entity.invitations.stream()
                .map(i -> i.code)
                .collect(Collectors.toSet());
        for (Invitation inv : league.getInvitations()) {
            if (!existingCodes.contains(inv.code())) {
                InvitationEntity ie = new InvitationEntity();
                ie.league = entity;
                ie.code = inv.code();
                ie.createdAt = inv.createdAt();
                ie.expiresAt = inv.expiresAt();
                entity.invitations.add(ie);
            }
        }

        entity.persist();
    }

    @Override
    @Transactional
    public Optional<League> findById(String id) {
        // @Transactional keeps the session open while toDomain() accesses the
        // LAZY collections, preventing LazyInitializationException.
        return LeagueEntity.<LeagueEntity>find("leagueId", id)
                .firstResultOptional()
                .map(LeagueRepositoryAdapter::toDomain);
    }

    @Override
    @Transactional
    public Optional<List<Membership>> findMembersByLeagueId(String leagueId) {
        if (LeagueEntity.count("leagueId", leagueId) == 0) {
            return Optional.empty();
        }
        List<Membership> members = MembershipEntity.<MembershipEntity>find("league.leagueId", leagueId)
                .stream()
                .map(m -> new Membership(m.userId, m.joinedAt))
                .toList();
        return Optional.of(members);
    }

    private static League toDomain(LeagueEntity e) {
        var memberships = e.memberships.stream()
                .map(m -> new Membership(m.userId, m.joinedAt))
                .toList();
        var invitations = e.invitations.stream()
                .map(i -> Invitation.of(i.code, i.createdAt, i.expiresAt))
                .toList();
        return League.reconstitute(e.leagueId, e.name, e.ownerId, memberships, invitations, e.createdAt);
    }
}
