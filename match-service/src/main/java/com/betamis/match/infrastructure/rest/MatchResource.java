package com.betamis.match.infrastructure.rest;

import com.betamis.match.domain.model.match.MatchStatus;
import com.betamis.match.domain.port.in.GetMatches;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;

@Path("/matches")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class MatchResource {

    private static final String USER_ROLE = "betamis-user";

    private final GetMatches getMatches;

    @Inject
    public MatchResource(GetMatches getMatches) {
        this.getMatches = getMatches;
    }

    @GET
    @RolesAllowed(USER_ROLE)
    public List<MatchResponse> getMatches(@QueryParam("status") String statusParam) {
        Optional<MatchStatus> status = parseStatus(statusParam);
        return getMatches.getAll(status).stream()
                .map(MatchResponse::from)
                .toList();
    }

    private Optional<MatchStatus> parseStatus(String statusParam) {
        if (statusParam == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(MatchStatus.valueOf(statusParam.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.status(400)
                    .entity("Invalid status: '" + statusParam + "'. Valid values: PLANNED, STARTED, FINISHED")
                    .build());
        }
    }
}
