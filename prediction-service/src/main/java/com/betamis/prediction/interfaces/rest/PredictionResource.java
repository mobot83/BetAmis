package com.betamis.prediction.interfaces.rest;

import com.betamis.prediction.domain.port.in.SubmitPrediction;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/predictions")
public class PredictionResource {

    private final SubmitPrediction submitPrediction;

    public PredictionResource(SubmitPrediction submitPrediction) {
        this.submitPrediction = submitPrediction;
    }

    @POST
    public Response submitPrediction() {

        return Response.ok().build();
    }

}
