package com.betamis.prediction.interfaces.rest;

import com.betamis.prediction.domain.model.score.Score;
import com.betamis.prediction.domain.port.in.SubmitPrediction;
import com.betamis.prediction.interfaces.rest.dto.SubmitPredictionRequest;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("/predictions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class PredictionResource {

    private static final String USER_ROLE = "betamis-user";

    private final SubmitPrediction submitPrediction;
    private final SecurityIdentity identity;

    @Inject
    public PredictionResource(SubmitPrediction submitPrediction, SecurityIdentity identity) {
        this.submitPrediction = submitPrediction;
        this.identity = identity;
    }

    @POST
    @RolesAllowed(USER_ROLE)
    public Response submitPrediction(SubmitPredictionRequest request) {
        if (request == null || request.matchId() == null || request.matchId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new DomainExceptionMapper.ErrorResponse("matchId must not be blank"))
                    .build();
        }
        if (request.kickOffTime() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new DomainExceptionMapper.ErrorResponse("kickOffTime must not be null"))
                    .build();
        }
        String userId = identity.getPrincipal().getName();
        Score score = new Score(request.homeScore(), request.awayScore());
        String predictionId = submitPrediction.execute(request.matchId(), userId, score, request.kickOffTime());
        return Response.created(URI.create("/predictions/" + predictionId)).build();
    }
}
