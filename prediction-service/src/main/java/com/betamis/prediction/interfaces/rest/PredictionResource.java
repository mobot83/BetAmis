package com.betamis.prediction.interfaces.rest;

import com.betamis.prediction.domain.model.prediction.Prediction;
import com.betamis.prediction.domain.model.score.Score;
import com.betamis.prediction.domain.port.in.GetPrediction;
import com.betamis.prediction.domain.port.in.SubmitPrediction;
import com.betamis.prediction.domain.port.in.UpdatePrediction;
import com.betamis.prediction.interfaces.rest.dto.PredictionResponse;
import com.betamis.prediction.interfaces.rest.dto.SubmitPredictionRequest;
import com.betamis.prediction.interfaces.rest.dto.UpdatePredictionRequest;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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
    private final UpdatePrediction updatePrediction;
    private final GetPrediction getPrediction;
    private final SecurityIdentity identity;

    @Inject
    public PredictionResource(SubmitPrediction submitPrediction, UpdatePrediction updatePrediction,
                               GetPrediction getPrediction, SecurityIdentity identity) {
        this.submitPrediction = submitPrediction;
        this.updatePrediction = updatePrediction;
        this.getPrediction = getPrediction;
        this.identity = identity;
    }

    @GET
    @RolesAllowed(USER_ROLE)
    public Response getPrediction(@QueryParam("matchId") String matchId) {
        if (matchId == null || matchId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new DomainExceptionMapper.ErrorResponse("matchId must not be blank"))
                    .build();
        }
        String userId = identity.getPrincipal().getName();
        return Response.ok(PredictionResponse.from(getPrediction.execute(matchId, userId))).build();
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

    @PATCH
    @Path("/{id}")
    @RolesAllowed(USER_ROLE)
    public Response updatePrediction(@PathParam("id") String id, UpdatePredictionRequest request) {
        if (request == null || request.kickOffTime() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new DomainExceptionMapper.ErrorResponse("kickOffTime must not be null"))
                    .build();
        }
        String userId = identity.getPrincipal().getName();
        Score newScore = new Score(request.homeScore(), request.awayScore());
        Prediction updated = updatePrediction.execute(id, userId, newScore, request.kickOffTime());
        return Response.ok(PredictionResponse.from(updated)).build();
    }
}
