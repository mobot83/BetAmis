package com.betamis.league.infrastructure.rest;

import com.betamis.league.domain.exception.InvalidInvitationCodeException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InvalidInvitationCodeExceptionMapper implements ExceptionMapper<InvalidInvitationCodeException> {

    @Override
    public Response toResponse(InvalidInvitationCodeException exception) {
        return Response.status(Response.Status.UNPROCESSABLE_ENTITY)
                .entity(new LeagueResource.ErrorResponse(exception.getMessage()))
                .build();
    }
}
