package com.betamis.league.infrastructure.rest;

import com.betamis.league.domain.exception.LeagueNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LeagueNotFoundExceptionMapper implements ExceptionMapper<LeagueNotFoundException> {

    @Override
    public Response toResponse(LeagueNotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new LeagueResource.ErrorResponse(exception.getMessage()))
                .build();
    }
}
