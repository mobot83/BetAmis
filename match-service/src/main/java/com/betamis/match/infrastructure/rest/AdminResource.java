package com.betamis.match.infrastructure.rest;

import com.betamis.match.domain.port.in.SyncMatches;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;

@Path("/admin")
public class AdminResource {

    @Inject
    SyncMatches syncMatches;

    @ConfigProperty(name = "match.sync.competition-ids")
    List<String> competitionIds;

    @POST
    @Path("/sync")
    public Response triggerSync() {
        List<String> synced = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (String competitionId : competitionIds) {
            try {
                syncMatches.syncByCompetition(competitionId);
                synced.add(competitionId);
            } catch (Exception e) {
                Log.errorf(e, "Manual sync failed for competition %s", competitionId);
                failed.add(competitionId);
            }
        }

        var body = new SyncResult(synced, failed);
        return failed.isEmpty() ? Response.ok(body).build() : Response.status(207).entity(body).build();
    }

    public record SyncResult(List<String> synced, List<String> failed) {}
}
