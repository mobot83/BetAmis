package com.betamis.match.application.usecase;

import com.betamis.match.domain.model.match.Match;
import com.betamis.match.domain.model.match.MatchStatus;
import com.betamis.match.domain.port.in.GetMatches;
import com.betamis.match.domain.port.out.MatchRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GetMatchesUseCase implements GetMatches {

    private final MatchRepository matchRepository;

    @Inject
    public GetMatchesUseCase(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Override
    public List<Match> getAll(Optional<MatchStatus> status) {
        return matchRepository.findAll(status);
    }
}
