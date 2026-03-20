package com.betamis.prediction.interfaces.rest;

import com.betamis.prediction.domain.exception.KickOffAlreadyPassedException;
import com.betamis.prediction.domain.exception.PredictionAlreadyClosedException;
import com.betamis.prediction.domain.exception.PredictionAlreadySubmittedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DomainExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof KickOffAlreadyPassedException
                || exception instanceof PredictionAlreadyClosedException) {
            return Response.status(422)
                    .entity(new ErrorResponse(exception.getMessage()))
                    .build();
        }
        if (exception instanceof PredictionAlreadySubmittedException) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(exception.getMessage()))
                    .build();
        }
        if (exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(exception.getMessage()))
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Internal server error"))
                .build();
    }

    public record ErrorResponse(String message) {}
}
