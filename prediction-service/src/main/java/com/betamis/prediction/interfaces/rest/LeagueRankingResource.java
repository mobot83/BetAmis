package com.betamis.prediction.interfaces.rest;

import com.betamis.prediction.domain.model.ranking.RankingEntry;
import com.betamis.prediction.domain.port.in.StreamLeagueRanking;
import com.betamis.prediction.domain.port.out.RankingReader;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Multi;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestSseElementType;

import java.util.List;

@Path("/leagues")
@Authenticated
public class LeagueRankingResource {

    private static final String USER_ROLE = "betamis-user";
    private static final int MAX_LEAGUE_ID_LENGTH = 100;

    private final RankingReader rankingReader;
    private final StreamLeagueRanking streamLeagueRanking;

    @Inject
    public LeagueRankingResource(RankingReader rankingReader, StreamLeagueRanking streamLeagueRanking) {
        this.rankingReader = rankingReader;
        this.streamLeagueRanking = streamLeagueRanking;
    }

    @GET
    @Path("/{id}/ranking")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(USER_ROLE)
    public Response getLeagueRanking(@PathParam("id") String leagueId) {
        if (isInvalidLeagueId(leagueId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new DomainExceptionMapper.ErrorResponse("leagueId must be between 1 and 100 characters"))
                    .build();
        }
        List<RankingEntry> entries = rankingReader.findLeagueRanking(leagueId);
        return Response.ok(entries).build();
    }

    @GET
    @Path("/{id}/ranking/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestSseElementType(MediaType.APPLICATION_JSON)
    @RolesAllowed(USER_ROLE)
    public Multi<RankingEntry> getLeagueRankingStream(@PathParam("id") String leagueId) {
        if (isInvalidLeagueId(leagueId)) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("leagueId must be between 1 and 100 characters")
                            .build());
        }
        return Multi.createFrom().publisher(streamLeagueRanking.stream(leagueId));
    }

    private boolean isInvalidLeagueId(String leagueId) {
        return leagueId == null || leagueId.isBlank() || leagueId.length() > MAX_LEAGUE_ID_LENGTH;
    }
}
