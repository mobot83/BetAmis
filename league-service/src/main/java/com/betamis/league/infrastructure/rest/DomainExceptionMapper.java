package com.betamis.league.infrastructure.rest;

import com.betamis.league.domain.exception.AlreadyMemberException;
import com.betamis.league.domain.exception.InvalidInvitationCodeException;
import com.betamis.league.domain.exception.LeagueNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DomainExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof LeagueNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new LeagueResource.ErrorResponse(exception.getMessage()))
                    .build();
        }
        if (exception instanceof InvalidInvitationCodeException) {
            return Response.status(422)
                    .entity(new LeagueResource.ErrorResponse(exception.getMessage()))
                    .build();
        }
        if (exception instanceof AlreadyMemberException) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new LeagueResource.ErrorResponse(exception.getMessage()))
                    .build();
        }
        // Unknown runtime exception — return 500 rather than re-throwing.
        // Re-throwing from ExceptionMapper.toResponse() produces an empty response
        // per the JAX-RS spec and bypasses Quarkus's default error handling.
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new LeagueResource.ErrorResponse("Internal server error"))
                .build();
    }
}


