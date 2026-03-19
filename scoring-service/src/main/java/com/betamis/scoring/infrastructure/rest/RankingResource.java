package com.betamis.scoring.infrastructure.rest;

import com.betamis.scoring.domain.model.UserRanking;
import com.betamis.scoring.domain.port.out.RankingRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/rankings")
@Produces(MediaType.APPLICATION_JSON)
public class RankingResource {

    @Inject
    RankingRepository rankingRepository;

    @GET
    @Path("/{leagueId}")
    public Response getLeagueRanking(@PathParam("leagueId") String leagueId) {
        if (leagueId == null || leagueId.isBlank() || leagueId.length() > 100) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("leagueId must be between 1 and 100 characters")
                    .build();
        }
        List<RankingEntry> entries = rankingRepository.findLeagueRanking(leagueId).stream()
                .map(r -> new RankingEntry(r.rank(), r.userId(), r.totalPoints()))
                .toList();
        return Response.ok(entries).build();
    }

    public record RankingEntry(int rank, String userId, long totalPoints) {}
}
