package com.betamis.match.infrastructure.client;

import com.betamis.match.infrastructure.client.dto.FootballMatchListResponse;
import com.betamis.match.infrastructure.client.dto.FootballMatchResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "football-data")
@RegisterProvider(FootballDataAuthFilter.class)
@Path("/v4")
public interface FootballDataClient {

    @GET
    @Path("/matches/{id}")
    FootballMatchResponse getMatch(@PathParam("id") long matchId);

    @GET
    @Path("/competitions/{competitionId}/matches")
    FootballMatchListResponse getMatchesByCompetition(
            @PathParam("competitionId") String competitionId,
            @QueryParam("matchday") Integer matchday,
            @QueryParam("status") String status
    );
}
