package com.betamis.league.infrastructure.rest;

import com.betamis.league.domain.exception.AlreadyMemberException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AlreadyMemberExceptionMapper implements ExceptionMapper<AlreadyMemberException> {

    @Override
    public Response toResponse(AlreadyMemberException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new LeagueResource.ErrorResponse(exception.getMessage()))
                .build();
    }
}
