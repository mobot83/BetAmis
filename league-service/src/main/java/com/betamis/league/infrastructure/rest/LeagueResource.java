package com.betamis.league.infrastructure.rest;

import com.betamis.league.domain.model.League;
import com.betamis.league.domain.model.Membership;
import com.betamis.league.domain.port.in.CreateLeague;
import com.betamis.league.domain.port.in.GetLeague;
import com.betamis.league.domain.port.in.JoinLeague;
import com.betamis.league.domain.port.in.ListMembers;
import com.betamis.league.infrastructure.rest.dto.CreateLeagueRequest;
import com.betamis.league.infrastructure.rest.dto.JoinLeagueRequest;
import com.betamis.league.infrastructure.rest.dto.LeagueResponse;
import com.betamis.league.infrastructure.rest.dto.MembershipResponse;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/leagues")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class LeagueResource {

    private static final String USER_ROLE = "betamis-user";
    private static final int MAX_NAME_LENGTH = 255;

    private final CreateLeague createLeague;
    private final GetLeague getLeague;
    private final JoinLeague joinLeague;
    private final ListMembers listMembers;
    private final SecurityIdentity identity;

    @Inject
    public LeagueResource(CreateLeague createLeague, GetLeague getLeague, JoinLeague joinLeague,
                          ListMembers listMembers, SecurityIdentity identity) {
        this.createLeague = createLeague;
        this.getLeague = getLeague;
        this.joinLeague = joinLeague;
        this.listMembers = listMembers;
        this.identity = identity;
    }

    /**
     * POST /leagues — Create a new league.
     * The authenticated user becomes the owner.
     */
    @POST
    @RolesAllowed(USER_ROLE)
    public Response createLeague(CreateLeagueRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("League name must not be blank"))
                    .build();
        }
        if (request.name().length() > MAX_NAME_LENGTH) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("League name must not exceed " + MAX_NAME_LENGTH + " characters"))
                    .build();
        }
        String ownerId = identity.getPrincipal().getName();
        League league = createLeague.create(request.name(), ownerId);
        return Response.created(URI.create("/leagues/" + league.getId()))
                .entity(LeagueResponse.from(league))
                .build();
    }

    /**
     * GET /leagues/{id} — Retrieve a league by id (includes current invitation code).
     */
    @GET
    @Path("/{id}")
    @RolesAllowed(USER_ROLE)
    public Response getLeague(@PathParam("id") String leagueId) {
        League league = getLeague.get(leagueId);
        return Response.ok(LeagueResponse.from(league)).build();
    }

    /**
     * POST /leagues/{id}/join — Join a league via invitation code.
     */
    @POST
    @Path("/{id}/join")
    @RolesAllowed(USER_ROLE)
    public Response joinLeague(@PathParam("id") String leagueId, JoinLeagueRequest request) {
        if (request == null || request.code() == null || request.code().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invitation code must not be blank"))
                    .build();
        }
        String userId = identity.getPrincipal().getName();
        joinLeague.join(leagueId, userId, request.code());
        return Response.noContent().build();
    }

    /**
     * GET /leagues/{id}/members — List members of a league.
     */
    @GET
    @Path("/{id}/members")
    @RolesAllowed(USER_ROLE)
    public Response listMembers(@PathParam("id") String leagueId) {
        List<Membership> members = listMembers.list(leagueId);
        List<MembershipResponse> body = members.stream()
                .map(MembershipResponse::from)
                .toList();
        return Response.ok(body).build();
    }

    public record ErrorResponse(String message) {}
}
