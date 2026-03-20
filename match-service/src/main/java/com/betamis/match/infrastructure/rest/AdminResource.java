package com.betamis.match.infrastructure.rest;

import com.betamis.match.domain.port.in.SyncMatches;
import io.quarkus.logging.Log;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class AdminResource {

    private static final String ADMIN_ROLE = "betamis-admin";

    private final SyncMatches syncMatches;
    private final List<String> competitionIds;

    @Inject
    public AdminResource(SyncMatches syncMatches,
                         @ConfigProperty(name = "match.sync.competition-ids") List<String> competitionIds) {
        this.syncMatches = syncMatches;
        this.competitionIds = competitionIds;
    }

    @POST
    @Path("/sync")
    @RolesAllowed(ADMIN_ROLE)
    public Response triggerSync() {
        List<String> synced = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (String competitionId : competitionIds) {
            try {
                syncMatches.syncByCompetition(competitionId.strip());
                synced.add(competitionId.strip());
            } catch (Exception e) {
                Log.errorf(e, "Manual sync failed for competition %s", competitionId);
                failed.add(competitionId.strip());
            }
        }

        var body = new SyncResult(synced, failed);
        return failed.isEmpty() ? Response.ok(body).build() : Response.status(207).entity(body).build();
    }

    public record SyncResult(List<String> synced, List<String> failed) {}
}
